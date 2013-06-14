/*
 * The Gemma project
 * 
 * Copyright (c) 2007 University of British Columbia
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
package ubic.basecode.ontology.search;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import ubic.basecode.ontology.Configuration;
import ubic.basecode.util.FileTools;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.larq.IndexBuilderSubject;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * @author pavlidis
 * @version $Id$
 */
public class OntologyIndexer {

    private static Log log = LogFactory.getLog( OntologyIndexer.class.getName() );

    /**
     * @param name of the ontology e.g. fmaOntology
     */
    public static void eraseIndex( String name ) {
        File indexdir = getIndexPath( name );

        if ( indexdir == null || !indexdir.canRead() ) {
            log.warn( "No index directory for " + name );
            return;
        }

        for ( File f : indexdir.listFiles() ) {
            f.delete();
        }
    }

    /**
     * @param name
     * @return
     */
    public static IndexLARQ getSubjectIndex( String name ) {
        log.debug( "Loading index: " + name );
        File indexdir = getIndexPath( name );
        try {
            FSDirectory directory = FSDirectory.getDirectory( indexdir );
            if ( IndexReader.indexExists( directory ) ) {
                IndexReader reader = IndexReader.open( directory );
                return new IndexLARQ( reader );
            }
            throw new IllegalArgumentException( "No index with name " + name );

        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * @param name
     * @param model
     * @return
     */
    public static IndexLARQ indexOntology( String name, OntModel model ) {
        return indexOntology( name, model, false );
    }

    /**
     * Loads or creates an index from an existing OntModel. Any existing index will loaded unless force=true.
     * 
     * @param name
     * @param model
     * @param force if true, the index will be redone
     * @return
     */
    public static IndexLARQ indexOntology( String name, OntModel model, boolean force ) {

        if ( force ) {
            return index( name, model );
        }

        try {
            return getSubjectIndex( name );
        } catch ( Exception e ) {
            log.info( "Error loading index from disk, re-indexing " + name );
            return index( name, model );
        }
    }

    /**
     * @param name
     * @return
     */
    private static File getIndexPath( String name ) {
        String ontologyDir = Configuration.getString( "ontology.index.dir" ); // e.g., /something/gemmaData/compass
        if ( StringUtils.isBlank( ontologyDir ) ) {
            ontologyDir = System.getProperty( "java.io.tmpdir" );
        }

        assert ontologyDir != null;

        String path = ontologyDir + File.separator + "ontology" + File.separator + name;

        File indexdir = new File( path );
        return indexdir;
    }

    /**
     * Create an on-disk index from an existing OntModel. Any existing index will be deleted/overwritten.
     * 
     * @see {@link http://jena.sourceforge.net/ARQ/lucene-arq.html}
     * @param datafile or uri
     * @param name used to refer to this index later
     * @param model
     * @return
     */
    private static IndexLARQ index( String name, OntModel model ) {
        eraseIndex( name );

        // double-check.
        File indexdir = getIndexPath( name );

        if ( indexdir.exists() ) {
            FileTools.deleteFiles( Arrays.asList( indexdir.listFiles() ) );
            FileTools.deleteDir( indexdir );
        }

        log.info( "Index to: " + indexdir );
        IndexBuilderSubject larqSubjectBuilder = new IndexBuilderSubject( indexdir );

        StmtIterator listStatements = model.listStatements( new IndexerSelector() );

        larqSubjectBuilder.indexStatements( listStatements );

        larqSubjectBuilder.closeWriter();

        IndexLARQ index = larqSubjectBuilder.getIndex();

        return index;
    }
}
