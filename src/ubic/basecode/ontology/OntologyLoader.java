/*
 * The baseCode project
 * 
 * Copyright (c) 2010 University of British Columbia
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
package ubic.basecode.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

import ubic.basecode.ontology.model.OntologyIndividual;
import ubic.basecode.ontology.model.OntologyIndividualImpl;
import ubic.basecode.ontology.model.OntologyProperty;
import ubic.basecode.ontology.model.OntologyResource;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.model.OntologyTermImpl;
import ubic.basecode.ontology.model.PropertyFactory;
import ubic.basecode.util.Configuration;

/**
 * Reads ontologies from OWL resources
 * 
 * @author paul
 * @version $Id$
 */
public class OntologyLoader {

    private static Logger log = LoggerFactory.getLogger( OntologyLoader.class );
    private static final int MAX_CONNECTION_TRIES = 3;
    private static final String OLD_CACHE_SUFFIX = ".old";
    private static final String TMP_CACHE_SUFFIX = ".tmp";

    /**
     * @param url
     * @param model
     * @return
     */
    public static Collection<OntologyResource> initialize( String url, OntModel model ) {

        Collection<OntologyResource> result = new HashSet<OntologyResource>();

        ExtendedIterator<OntClass> classIt = model.listClasses();
        int count = 0;
        log.debug( "Reading classes for ontology: " + url );
        while ( classIt.hasNext() ) {
            OntClass element = classIt.next();
            if ( element.isAnon() ) continue;
            OntologyTerm ontologyTerm = new OntologyTermImpl( element );
            result.add( ontologyTerm );
            if ( ++count % 1000 == 0 ) {
                log.debug( "Loaded " + count + " terms, last was " + ontologyTerm );
            }
        }

        log.debug( "Loaded " + count + " terms" );

        ExtendedIterator<com.hp.hpl.jena.ontology.ObjectProperty> propIt = model.listObjectProperties();
        count = 0;
        log.debug( "Reading object properties..." );
        while ( propIt.hasNext() ) {
            com.hp.hpl.jena.ontology.ObjectProperty element = propIt.next();
            OntologyProperty ontologyTerm = PropertyFactory.asProperty( element );
            if ( ontologyTerm == null ) continue; // couldn't be converted for some reason.
            result.add( ontologyTerm );
            if ( ++count % 1000 == 0 ) {
                log.debug( "Loaded " + count + " object properties, last was " + ontologyTerm );
            }
        }

        ExtendedIterator<com.hp.hpl.jena.ontology.DatatypeProperty> dtPropIt = model.listDatatypeProperties();
        log.debug( "Reading datatype properties..." );
        while ( dtPropIt.hasNext() ) {
            com.hp.hpl.jena.ontology.DatatypeProperty element = dtPropIt.next();
            OntologyProperty ontologyTerm = PropertyFactory.asProperty( element );
            if ( ontologyTerm == null ) continue; // couldn't be converted for some reason.
            result.add( ontologyTerm );
            if ( ++count % 1000 == 0 ) {
                log.debug( "Loaded " + count + " datatype properties, last was " + ontologyTerm );
            }
        }

        log.debug( "Loaded " + count + " properties" );

        ExtendedIterator<Individual> indiIt = model.listIndividuals();
        count = 0;
        log.debug( "Reading individuals..." );
        while ( indiIt.hasNext() ) {
            Individual element = indiIt.next();
            if ( element.isAnon() ) continue;
            OntologyIndividual ontologyTerm = new OntologyIndividualImpl( element );
            result.add( ontologyTerm );
            if ( ++count % 1000 == 0 ) {
                log.debug( "Loaded " + count + " individuals, last was " + ontologyTerm );
            }
        }
        log.debug( "Loaded " + count + " individuals" );
        return result;
    }

    /**
     * Load an ontology into memory. Use this type of model when fast access is critical and memory is available.
     * 
     * @param is
     * @param url, used as a key
     * @param spec
     * @return
     */
    public static OntModel loadMemoryModel( InputStream is, String url, OntModelSpec spec ) {
        OntModel model = getMemoryModel( url, spec );
        model.read( is, null );
        return model;
    }

    /**
     * Load an ontology into memory. Use this type of model when fast access is critical and memory is available. Uses
     * OWL_MEM_TRANS_INF
     * 
     * @param url
     * @return
     */
    public static OntModel loadMemoryModel( String url ) {
        return loadMemoryModel( url, OntModelSpec.OWL_MEM_TRANS_INF );
    }

    /**
     * Load an ontology into memory. Use this type of model when fast access is critical and memory is available. Uses
     * OWL_MEM_TRANS_INF
     * If load from URL fails, attempt to load from disk cache under @cacheName.
     * 
     * @param url
     * @return
     */
    public static OntModel loadMemoryModel( String url, String cacheName ) {
        return loadMemoryModel( url, OntModelSpec.OWL_MEM_TRANS_INF, cacheName );
    }

    /**
     * Load an ontology into memory. Use this type of model when fast access is critical and memory is available.
     * 
     * @param url
     * @return
     */
    public static OntModel loadMemoryModel( String url, OntModelSpec spec ) {
        return loadMemoryModel( url, spec, null );
    }

    /**
     * Load an ontology into memory. Use this type of model when fast access is critical and memory is available.
     * If load from URL fails, attempt to load from disk cache under @cacheName.
     * 
     * @param url
     * @param spec e.g. OWL_MEM_TRANS_INF
     * @param cacheName unique name of this ontology, will be used to load from disk in case of failed url connection
     * @return
     */
    public static OntModel loadMemoryModel( String url, OntModelSpec spec, String cacheName ) {
        StopWatch timer = new StopWatch();
        timer.start();
        OntModel model = getMemoryModel( url, spec );

        URLConnection urlc = null;
        int tries = 0;
        while ( tries < MAX_CONNECTION_TRIES ) {
            try {
                urlc = new URL( url ).openConnection();
                // help ensure mis-configured web servers aren't causing trouble.
                urlc.setRequestProperty( "Accept", "application/rdf+xml" );

                if ( tries > 0 ) {
                    log.info( "Retrying connecting to " + url + " [" + tries + "/" + MAX_CONNECTION_TRIES
                            + " of max tries" );
                } else {
                    log.info( "Connecting to ontology from " + url );
                }

                urlc.connect(); // Will error here on bad URL

                break;
            } catch ( IOException e ) {
                // try to recover.
                log.error( e + " retrying?" );
                tries++;
            }
        }

        if ( urlc != null ) {
            try (InputStream in = urlc.getInputStream();) {
                Reader reader;
                if ( cacheName != null ) {
                    // write tmp to disk
                    File tempFile = getTmpDiskCachePath( cacheName );
                    tempFile.getParentFile().mkdirs();
                    Files.copy( in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING );

                    reader = new FileReader( tempFile );
                } else {
                    // Skip the cache
                    reader = new InputStreamReader( in );
                }

                try (BufferedReader buf = new BufferedReader( reader );) {
                    model.read( buf, url );
                }

                log.info( "Load model: " + timer.getTime() + "ms" );
            } catch ( IOException e ) {
                log.error( e.getMessage(), e );
            }
        }

        if ( cacheName != null ) {

            File f = getDiskCachePath( cacheName );
            File tempFile = getTmpDiskCachePath( cacheName );
            File oldFile = getOldDiskCachePath( cacheName );

            if ( model.isEmpty() ) {
                // Attempt to load from disk cache

                if ( f == null ) {
                    throw new RuntimeException(
                            "Ontology cache directory required to load from disk: ontology.cache.dir" );
                }

                if ( f.exists() && !f.isDirectory() ) {
                    try (BufferedReader buf = new BufferedReader( new FileReader( f ) );) {
                        model.read( buf, url );
                        // We successfully loaded the cached ontology. Copy the loaded ontology to oldFile
                        // so that we don't recreate indices during initialization based on a false change in
                        // the ontology.
                        Files.copy( f.toPath(), oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
                        log.info( "Load model from disk: " + timer.getTime() + "ms" );
                    } catch ( IOException e ) {
                        log.error( e.getMessage(), e );
                        throw new RuntimeException(
                                "Ontology failed load from URL (" + url + ") and disk cache: " + cacheName );
                    }
                } else {
                    throw new RuntimeException(
                            "Ontology failed load from URL (" + url + ") and disk cache does not exist: " + cacheName );
                }

            } else {
                // Model was successfully loaded into memory from URL with given cacheName
                // Save cache to disk (rename temp file)
                log.info( "Caching ontology to disk: " + cacheName );
                if ( f != null ) {
                    try {
                        // Need to compare previous to current so instead of overwriting we'll move the old file
                        f.createNewFile();
                        Files.move( f.toPath(), oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING );
                        Files.move( tempFile.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING );
                    } catch ( IOException e ) {
                        log.error( e.getMessage(), e );
                    }
                } else {
                    log.warn( "Ontology cache directory required to save to disk: ontology.cache.dir" );
                }
            }

        }

        assert !model.isEmpty();

        return model;
    }

    public static boolean hasChanged( String cacheName ) {
        boolean changed = false; // default
        if ( StringUtils.isBlank( cacheName ) ) {
            return changed;
        }

        File newFile = getDiskCachePath( cacheName );
        File oldFile = getOldDiskCachePath( cacheName );

        try {
            // This might be slow considering it calls IOUtils.contentsEquals which compares byte-by-byte
            // in the worst case scenario.
            // In this case consider using NIO for higher-performance IO using Channels and Buffers.
            // Ex. Use a 4MB Memory-Mapped IO operation.
            changed = !FileUtils.contentEquals( newFile, oldFile );
        } catch ( IOException e ) {
            log.error( e.getMessage() );
        }

        return changed;

    }

    public static boolean deleteOldCache( String cacheName ) {
        File f = getOldDiskCachePath( cacheName );
        return f.delete();
    }

    /**
     * Get model that is entirely in memory with default OntModelSpec.OWL_MEM_RDFS_INF.
     * 
     * @param url
     * @return
     */
    static OntModel getMemoryModel( String url ) {
        return getMemoryModel( url, OntModelSpec.OWL_MEM_RDFS_INF );
    }

    /**
     * Get model that is entirely in memory.
     * 
     * @param url
     * @param specification
     * @return
     */
    static OntModel getMemoryModel( String url, OntModelSpec specification ) {
        OntModelSpec spec = new OntModelSpec( specification );
        ModelMaker maker = ModelFactory.createMemModelMaker();
        Model base = maker.createModel( url, false );
        spec.setImportModelMaker( maker );
        spec.getDocumentManager().setProcessImports( false );

        OntModel model = ModelFactory.createOntologyModel( spec, base );
        model.setStrictMode( false ); // fix for owl2 files
        return model;
    }

    /**
     * @param name
     * @return
     */
    public static File getDiskCachePath( String name ) {
        String ontologyDir = Configuration.getString( "ontology.cache.dir" ); // e.g., /something/gemmaData/ontologyCache
        if ( StringUtils.isBlank( ontologyDir ) || StringUtils.isBlank( name ) ) {
            return null;
        }

        assert ontologyDir != null;

        String path = ontologyDir + File.separator + "ontology" + File.separator + name;

        File indexFile = new File( path );
        return indexFile;
    }

    static File getOldDiskCachePath( String name ) {
        File indexFile = getDiskCachePath( name );
        if ( indexFile == null ) {
            return null;
        }
        return new File( indexFile.getAbsolutePath() + OLD_CACHE_SUFFIX );

    }

    static File getTmpDiskCachePath( String name ) {
        File indexFile = getDiskCachePath( name );
        if ( indexFile == null ) {
            return null;
        }
        return new File( indexFile.getAbsolutePath() + TMP_CACHE_SUFFIX );

    }

}
