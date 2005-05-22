package baseCode.bio.geneset;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.DefaultTreeModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import baseCode.bio.GOEntry;
import baseCode.dataStructure.graph.DirectedGraph;
import baseCode.dataStructure.graph.DirectedGraphNode;
import baseCode.xml.GOParser;

/**
 * Read data from GO XML file, store in easy-to-use data structure.
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author Paul Pavlidis
 * @author Homin Lee
 * @version $Id$
 */
public class GONames {

    /**
     * 
     */
    private static final String NO_DESCRIPTION_AVAILABLE = "<no description available>";

    protected static final Log log = LogFactory.getLog( GONames.class );

    /**
     * Name for root of tree representing user-defined gene sets.
     */
    public static final String USER_DEFINED = "User-defined";

    /**
     * Name for aspect when none is defined.
     */
    static public final String NO_ASPECT_AVAILABLE = "[No aspect available]";

    private Map goNameMap;
    private Set newGeneSets = new HashSet();
    private GOParser parser;

    private String NO_DEFINITION_AVAILABLE = "[No definition available]";

    private String USER_DEFINED_ASPECT = "User-defined";

    /**
     * @param filename <code>String</code> The XML file containing class to name mappings. First column is the class
     *        id, second is a description that will be used int program output.
     * @throws IOException
     * @throws SAXException
     */
    public GONames( String filename ) throws SAXException, IOException {
        if ( filename == null || filename.length() == 0 ) {
            throw new IllegalArgumentException( "Invalid filename " + filename + " or no filename was given" );
        }

        InputStream i = new FileInputStream( filename );
        this.initialize( i );
    }

    /**
     * @param goIds
     * @param goTerms
     */
    public GONames( List goIds, List goTerms ) {
        if ( goIds == null || goTerms == null || goIds.size() != goTerms.size() ) {
            throw new IllegalArgumentException( "Invalid arguments" );
        }
        goNameMap = new HashMap();
        for ( int i = 0; i < goIds.size(); i++ ) {
            goNameMap.put( goIds.get( i ), goTerms.get( i ) );
        }
    }

    /**
     * @param inputStream
     * @throws IOException
     * @throws SAXException
     */
    public GONames( InputStream inputStream ) throws IOException, SAXException {
        if ( inputStream == null ) {
            throw new IOException( "Input stream was null" );
        }
        this.initialize( inputStream );
    }

    /**
     * 
     */
    private void initialize( InputStream inputStream ) throws IOException, SAXException {
        this.parser = new GOParser( inputStream );
        goNameMap = parser.getGONameMap();
        if ( this.getGraph() == null ) return;
        DirectedGraphNode root = this.getGraph().getRoot();
        this.getGraph().addChildTo(
                root.getKey(),
                USER_DEFINED,
                new DirectedGraphNode( USER_DEFINED,
                        new GOEntry( USER_DEFINED, "", USER_DEFINED, NO_ASPECT_AVAILABLE ), this.getGraph() ) );
    }

    /**
     * @return graph representation of the GO hierarchy
     */
    public DirectedGraph getGraph() {
        if ( parser == null ) return null;
        return parser.getGraph();
    }

    /*
     * 
     */
    public DefaultTreeModel getTreeModel() {
        if ( getGraph() == null ) return null;
        return getGraph().getTreeModel();
    }

    /**
     * @param id
     * @return a Set containing the ids of geneSets which are immediately below the selected one in the hierarchy.
     */
    public Set getChildren( String id ) {
        if ( getGraph() == null ) return null;
        Set returnVal = new HashSet();
        Set children = ( ( DirectedGraphNode ) getGraph().get( id ) ).getChildNodes();
        for ( Iterator it = children.iterator(); it.hasNext(); ) {
            DirectedGraphNode child = ( DirectedGraphNode ) it.next();
            String childKey = ( ( GOEntry ) child.getItem() ).getId().intern();
            returnVal.add( childKey.intern() );
        }
        return returnVal;
    }

    /**
     * @param id
     * @return a Set containing the ids of geneSets which are immediately above the selected one in the hierarchy.
     */
    public Set getParents( String id ) {
        if ( getGraph() == null ) return null;
        Set returnVal = new HashSet();

        if ( !getGraph().containsKey( id ) ) {
            log.debug( "GeneSet " + id + " doesn't exist in graph" ); // this is not really a problem.
            return returnVal;
        }

        Set parents = ( ( DirectedGraphNode ) getGraph().get( id ) ).getParentNodes();
        for ( Iterator it = parents.iterator(); it.hasNext(); ) {
            DirectedGraphNode parent = ( DirectedGraphNode ) it.next();
            String parentKey = ( ( GOEntry ) parent.getItem() ).getId().intern();
            returnVal.add( parentKey.intern() );
        }
        return returnVal;
    }

    /**
     * @return Map representation of the GO id - name associations.
     */
    public Map getMap() {
        return goNameMap;
    }

    /**
     * @param go_ID
     * @return name of gene set
     */
    public String getNameForId( String go_ID ) {
        if ( !goNameMap.containsKey( go_ID ) ) {
            return NO_DESCRIPTION_AVAILABLE;
        }

        return ( ( String ) ( goNameMap.get( go_ID ) ) ).intern();
    }

    /**
     * @param go_ID
     * @return the aspect (molecular_function etc) for an id.
     */
    public String getAspectForId( String go_ID ) {
        if ( !goNameMap.containsKey( go_ID ) ) {
            return NO_ASPECT_AVAILABLE;
        }

        if ( getGraph() == null ) return NO_ASPECT_AVAILABLE;
        if ( getGraph().getNodeContents( go_ID ) == null ) {
            log.debug( "No node for " + go_ID );
            return NO_ASPECT_AVAILABLE;
        }

        GOEntry node = ( GOEntry ) getGraph().getNodeContents( go_ID );
        if ( node.getAspect() == null ) {
            Set parents = getParents( go_ID );
            for ( Iterator iter = parents.iterator(); iter.hasNext(); ) {
                String parent = ( String ) iter.next();
                return getAspectForId( parent );
            }
        }
        return node.getAspect();
    }

    /**
     * @param id String
     * @param name String
     */
    public void addClass( String id, String name ) {
        goNameMap.put( id, name );
        newGeneSets.add( id );
        addClassToUserDefined( id, name );
    }

    /**
     * @param id
     * @param name
     */
    private void addClassToUserDefined( String id, String name ) {
        if ( getGraph() == null ) return;
        log.debug( "Adding user-defined gene set to graph" );
        if ( this.getGraph().get( USER_DEFINED ) == null ) log.error( "No user-defined root node!" );
        this.getGraph().addChildTo( USER_DEFINED, id, new GOEntry( id, name, name, USER_DEFINED_ASPECT ) );
    }

    /**
     * @param id String
     * @param name String
     */
    public void modifyClass( String id, String name ) {
        if ( newGeneSets.contains( id ) ) return;
        goNameMap.put( id, name );
        newGeneSets.add( id );
        addClassToUserDefined( id, name );
    }

    /**
     * Check if a gene set is already defined.
     * 
     * @param id
     * @return
     */
    public boolean isUserDefined( String id ) {
        return newGeneSets.contains( id );
    }

    /**
     * Return the Set of all new gene sets (ones which were added/modified after loading the file)
     * 
     * @return
     */
    public Set getUserDefinedGeneSets() {
        return newGeneSets;
    }

    /**
     * @param classID
     */
    public void deleteGeneSet( String classID ) {
        newGeneSets.remove( classID );
        goNameMap.remove( classID );
        if ( this.getGraph() == null ) return;
        this.getGraph().deleteChildFrom( USER_DEFINED, classID );
    }

    /**
     * @param classid
     * @return
     */
    public String getDefinitionForId( String classid ) {
        if ( !goNameMap.containsKey( classid ) ) {
            return NO_DEFINITION_AVAILABLE;
        }
        if ( getGraph() == null ) return NO_DEFINITION_AVAILABLE;
        if ( getGraph().getNodeContents( classid ) == null ) {
            log.debug( "No node for " + classid );
            return NO_DEFINITION_AVAILABLE;
        }
        GOEntry node = ( GOEntry ) getGraph().getNodeContents( classid );
        return node.getDefinition();
    }

}