/*
 * The baseCode project
 * 
 * Copyright (c) 2006 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ubic.basecode.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import ubic.basecode.bio.GOEntry;
import ubic.basecode.dataStructure.graph.DirectedGraph;
import ubic.basecode.dataStructure.graph.DirectedGraphNode;

/**
 * Read in the GO XML file provided by the Gene Ontology Consortium.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class GOParser {

    private DirectedGraph<String, GOEntry> m;

    /**
     * Get the graph that was created.
     * 
     * @return a DirectedGraph. Nodes contain OntologyEntry instances.
     */
    public DirectedGraph<String, GOEntry> getGraph() {
        return m;
    }

    /**
     * Get a simple Map that contains keys that are the GO ids, values are the names. This can replace the functionality
     * of the GONameReader in classScore.
     * 
     * @return Map
     */
    public Map<String, String> getGONameMap() {
        Map<String, DirectedGraphNode<String, GOEntry>> nodes = m.getItems();
        Map<String, String> result = new HashMap<String, String>();
        for ( Iterator<String> it = nodes.keySet().iterator(); it.hasNext(); ) {
            DirectedGraphNode<String, GOEntry> node = nodes.get( it.next() );
            GOEntry e = node.getItem();
            result.put( e.getId().intern(), e.getName().intern() );
        }
        return result;
    }

    public GOParser( InputStream i ) throws IOException, SAXException {

        if ( i.available() == 0 ) {
            throw new IOException( "XML stream contains no data." );
        }

        System.setProperty( "org.xml.sax.driver", "org.apache.xerces.parsers.SAXParser" );

        XMLReader xr = XMLReaderFactory.createXMLReader();
        GOHandler handler = new GOHandler();
        xr.setFeature( "http://xml.org/sax/features/validation", false );
        xr.setFeature( "http://xml.org/sax/features/external-general-entities", false );
        xr.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );
        xr.setContentHandler( handler );
        xr.setErrorHandler( handler );
        xr.setEntityResolver( handler );
        xr.setDTDHandler( handler );
        xr.parse( new InputSource( i ) );

        m = handler.getResults();

    }

}

class GOHandler extends DefaultHandler {
    private DirectedGraph<String, GOEntry> m;

    public DirectedGraph<String, GOEntry> getResults() {
        return m;
    }

    public GOHandler() {
        super();
        m = new DirectedGraph<String, GOEntry>();

        /*
         * This is a workaround for a change in GO: the terms obsolete_molecular_function etc. are never defined. See
         * bug
         */
        initializeNewNode( "all" );
        initializeNewNode( "obsolete_molecular_function" );
        m.addParentTo( "obsolete_molecular_function", "all" );
        initializeNewNode( "obsolete_biological_process" );
        m.addParentTo( "obsolete_biological_process", "all" );
        initializeNewNode( "obsolete_cellullar_component" );
        m.addParentTo( "obsolete_cellullar_component", "all" );

        forbiddenParents.add( "obsolete_molecular_function" );
        forbiddenParents.add( "obsolete_biological_process" );
        forbiddenParents.add( "obsolete_cellular_component" );

    }

    private Collection<String> forbiddenParents = new HashSet<String>();
    private boolean inTerm = false;
    private boolean inDef = false;
    private boolean inAcc = false;
    private boolean inName = false;
    // private boolean inPartOf = false;
    // private boolean inIsa = false;
    // private boolean inSyn = false;

    private String currentAspect;
    private StringBuffer nameBuf;
    private StringBuffer accBuf;
    private StringBuffer defBuf;

    @Override
    public void startElement( String uri, String name, String qName, Attributes atts ) {

        if ( name.equals( "term" ) ) {
            inTerm = true;
        } else if ( name.equals( "accession" ) ) {
            accBuf = new StringBuffer();
            inAcc = true;
        } else if ( name.equals( "definition" ) ) {
            defBuf = new StringBuffer();
            inDef = true;
        } else if ( name.equals( "is_a" ) ) {
            // inIsa = true;
            String res = atts.getValue( "rdf:resource" );
            String parent = res.substring( res.lastIndexOf( '#' ) + 1, res.length() );

            if ( !m.containsKey( parent ) ) {
                initializeNewNode( parent );
            }
            String currentTerm = accBuf.toString();

            if ( !forbiddenParents.contains( parent ) ) {
                m.addParentTo( currentTerm, parent );
            }

        } else if ( name.equals( "part_of" ) ) {
            // inPartOf = true;
            String res = atts.getValue( "rdf:resource" );
            String parent = res.substring( res.lastIndexOf( '#' ) + 1, res.length() );

            if ( !m.containsKey( parent ) ) {
                initializeNewNode( parent );
            }
            String currentTerm = accBuf.toString();

            if ( !forbiddenParents.contains( parent ) ) {
                m.addParentTo( currentTerm, parent );
            }

        } else if ( name.equals( "synonym" ) ) {
            // inSyn = true;
        } else if ( name.equals( "name" ) ) {
            nameBuf = new StringBuffer();
            inName = true;
        }
    }

    /**
     * @param parent
     */
    private void initializeNewNode( String parent ) {
        m.addNode( parent, new GOEntry( parent, "(No name provided)", "(No definition)", null ) );
    }

    @Override
    public void endElement( String uri, String name, String qName ) {
        if ( name.equals( "term" ) ) {
            inTerm = false;
        } else if ( name.equals( "accession" ) ) {
            inAcc = false;
            String currentTerm = accBuf.toString();
            initializeNewNode( currentTerm );
        } else if ( name.equals( "definition" ) ) {
            String currentTerm = accBuf.toString();
            m.getNodeContents( currentTerm ).setDefinition( defBuf.toString().intern() );
            inDef = false;
        } else if ( name.equals( "is_a" ) ) {
            // inIsa = false;
        } else if ( name.equals( "part_of" ) ) {
            // inPartOf = false;
        } else if ( name.equals( "synonym" ) ) {
            // inSyn = false;
        } else if ( name.equals( "name" ) ) {
            inName = false;
            String currentTerm = accBuf.toString();

            String currentName = nameBuf.toString().intern();

            m.getNodeContents( currentTerm ).setName( currentName );

            if ( currentName.equalsIgnoreCase( "molecular_function" )
                    || currentName.equalsIgnoreCase( "biological_process" )
                    || currentName.equalsIgnoreCase( "cellular_component" )
                    || currentName.equalsIgnoreCase( "obsolete_molecular_function" )
                    || currentName.equalsIgnoreCase( "obsolete_biological_process" )
                    || currentName.equalsIgnoreCase( "obsolete_cellullar_component" ) ) {
                currentAspect = currentName;
                m.getNodeContents( currentTerm ).setAspect( currentAspect );
            }

        }
    }

    @Override
    public void characters( char ch[], int start, int length ) {

        if ( inTerm ) {
            if ( inAcc ) {
                accBuf.append( ch, start, length );
            } else if ( inDef ) {
                defBuf.append( ch, start, length );
            } else if ( inName ) {
                nameBuf.append( ch, start, length );
            }
        }
    }

}