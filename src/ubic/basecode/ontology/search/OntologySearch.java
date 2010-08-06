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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.ontology.model.OntologyIndividual;
import ubic.basecode.ontology.model.OntologyIndividualImpl;
import ubic.basecode.ontology.model.OntologyResource;
import ubic.basecode.ontology.model.OntologyTerm;
import ubic.basecode.ontology.model.OntologyTermImpl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.larq.ARQLuceneException;
import com.hp.hpl.jena.query.larq.IndexLARQ;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.shared.JenaException;

/**
 * @author pavlidis
 * @version $Id$
 */
public class OntologySearch {

    private static Log log = LogFactory.getLog( OntologySearch.class.getName() );

    // Lucene cannot properly parse these characters... gives a query parse error.
    // OntologyTerms don't contain them anyway
    private final static char[] INVALID_CHARS = { ':', '(', ')', '?', '^', '[', ']', '{', '}', '!', '~', '"', '\'' };

    /**
     * Find classes that match the query string
     * 
     * @param model that goes with the index
     * @param index to search
     * @param queryString
     * @return Collection of OntologyTerm objects
     */
    public static Collection<OntologyTerm> matchClasses( OntModel model, IndexLARQ index, String queryString ) {

        Set<OntologyTerm> results = new HashSet<OntologyTerm>();
        NodeIterator iterator = runSearch( model, index, queryString );

        while ( iterator != null && iterator.hasNext() ) {
            RDFNode r = iterator.next();
            r = r.inModel( model );
            if ( log.isDebugEnabled() ) log.debug( "Search results: " + r );
            if ( r.isURIResource() ) {
                try {

                    if ( !r.canAs( OntClass.class ) ) {
                        if ( log.isDebugEnabled() )
                            log.debug( "Unable to convert jena resource " + r
                                    + " to OntClass.class, skipping. Resource: " + r.toString() );
                        continue;
                    }

                    OntClass cl = r.as( OntClass.class );
                    OntologyTermImpl impl2 = new OntologyTermImpl( cl );
                    results.add( impl2 );
                    if ( log.isDebugEnabled() ) log.debug( impl2 );
                } catch ( ARQLuceneException e ) {
                    throw new RuntimeException( e.getCause() );
                } catch ( JenaException e ) {
                    throw new RuntimeException( e.getCause() );
                } catch ( Exception e ) {
                    log.error( e.getMessage(), e );
                }
            }

        }
        return results;
    }

    /**
     * Find individuals that match the query string
     * 
     * @param model that goes with the index
     * @param index to search
     * @param queryString
     * @return Collection of OntologyTerm objects
     */
    public static Collection<OntologyIndividual> matchIndividuals( OntModel model, IndexLARQ index, String queryString ) {

        Set<OntologyIndividual> results = new HashSet<OntologyIndividual>();
        NodeIterator iterator = null;

        queryString = queryString.trim();        

        // Add wildcard only if the last word is longer than one character. This is to prevent lucene from
        // blowing up. See bug#1145
        String[] words = queryString.split("\\s+");
        int lastWordLength = words[words.length - 1].length();
        if ( lastWordLength > 1) { 
            try { // Use wildcard search.
            	iterator = runSearch( model, index, queryString+"*" );
            } catch (ARQLuceneException e) { // retry without wildcard           	
                log.info("Caught "+ e +" caused by "+e.getCause()+" reason "+e.getMessage()+". Retrying search without wildcard.");
                iterator = runSearch( model, index, queryString );            }            
        } else {
        	iterator = runSearch( model, index, queryString );
        }

        while ( iterator != null && iterator.hasNext() ) {
            RDFNode r = iterator.next();
            r = r.inModel( model );
            if ( log.isDebugEnabled() ) log.debug( "Search results: " + r );
            if ( r.isResource() ) {
                try {

                    if ( !r.canAs( Individual.class ) ) {
                        if ( log.isDebugEnabled() )
                            log.debug( "Unable to convert jena resource " + r
                                    + " to Individual.class, skipping. Resource: " + r.toString() );
                        continue;
                    }

                    Individual cl = r.as( Individual.class );
                    OntologyIndividual impl2 = new OntologyIndividualImpl( cl );
                    results.add( impl2 );
                    if ( log.isDebugEnabled() ) log.debug( impl2 );
                } catch ( ARQLuceneException e ) {
                    throw new RuntimeException( e.getCause() );
                } catch ( JenaException je ) {

                    // If there is a jena DB connection exception should try again
                    // before bailing for good (have noticed this happens intermitently on production)
                    // The keep alive thread should stop this from happening but it doesn't gaurantee it

                    // As this method is static it can be called from alot of
                    // places. Making the call recursive with a max attempts to
                    // retry could end up in a race condition.

                    log.error( "Trying again: " + je, je );
                    try {
                        Individual cl = r.as( Individual.class );
                        OntologyIndividual impl2 = new OntologyIndividualImpl( cl );
                        results.add( impl2 );

                    } catch ( Exception e ) {
                        log.error( "Second attempt failed: " + e, e );
                    }

                } catch ( Exception e ) {
                    log.error( e.getMessage(), e );
                }
            }

        }
        return results;

    }

    /**
     * Find OntologyIndividuals and OntologyTerms that match the query string. 
     * Search with a wildcard is attempted whenever possible.
     * 
     * @param model that goes with the index
     * @param index to search
     * @param queryString
     * @return Collection of OntologyResource objects
     */
    public static Collection<OntologyResource> matchResources( OntModel model, IndexLARQ index, String queryString ) {

        Set<OntologyResource> results = new HashSet<OntologyResource>();
        NodeIterator iterator = null;
        
        queryString = queryString.trim();        

        // Add wildcard only if the last word is longer than one character. This is to prevent lucene from
        // blowing up. See bug#1145
        String[] words = queryString.split("\\s+");
        int lastWordLength = words[words.length - 1].length();
        if ( lastWordLength > 1) { 
            try { // Use wildcard search.
            	iterator = runSearch( model, index, queryString+"*" );
            } catch (ARQLuceneException e) { // retry without wildcard           	
                log.info("Caught "+ e +" caused by "+e.getCause()+" reason "+e.getMessage()+". Retrying search without wildcard.");
                iterator = runSearch( model, index, queryString );            }            
        } else {
        	iterator = runSearch( model, index, queryString );
        }

        while ( iterator != null && iterator.hasNext() ) {
            RDFNode r = iterator.next();
            r = r.inModel( model );
            if ( log.isDebugEnabled() ) log.debug( "Search results: " + r );
            if ( r.isURIResource() ) {
                try {

                    if ( !r.canAs( OntClass.class ) ) {
                        if ( log.isDebugEnabled() )
                            log.debug( "Unable to convert jena resource resource " + r
                                    + " to OntClass.class, skipping. Resource: " + r.toString() );
                        continue;
                    }

                    OntClass cl = r.as( OntClass.class );
                    OntologyTermImpl impl2 = new OntologyTermImpl( cl );
                    results.add( impl2 );
                    if ( log.isDebugEnabled() ) log.debug( impl2 );
                } catch ( JenaException e ) {
                    throw new RuntimeException( e.getCause() );
                } catch ( Exception e ) {
                    log.error( e, e );
                }
            } else if ( r.isResource() ) {
                try {

                    if ( !r.canAs( Individual.class ) ) {
                        if ( log.isDebugEnabled() )
                            log.debug( "Unable to convert jena resource resource " + r
                                    + "  to Individual.class, skipping. Resource: " + r.toString() );
                        continue;
                    }

                    Individual cl = r.as( Individual.class );
                    OntologyIndividual impl2 = new OntologyIndividualImpl( cl );
                    results.add( impl2 );
                    if ( log.isDebugEnabled() ) log.debug( impl2 );
                } catch ( JenaException e ) {
                    throw new RuntimeException( e.getCause() );
                } catch ( Exception e ) {
                    log.error( e.getMessage(), e );
                }
            } else if ( log.isDebugEnabled() ) log.debug( "This search term not included in the results: " + r );

        }
        return results;

    }

    /**
     * Will remove characters that jena is unable to parse. Will also escape and remove leading and trailing white space
     * (which also causes jena to die)
     * 
     * @param toStrip the string to clean
     * @return
     */
    public static String stripInvalidCharacters( String toStrip ) {
        String result = StringUtils.strip( toStrip );
        for ( char badChar : INVALID_CHARS ) {
            result = StringUtils.remove( result, badChar );
        }
        /*
         * Queries cannot start with '*' or ?
         */
        result = result.replaceAll( "^\\**", "" );
        result = result.replaceAll( "^\\?*", "" );

        return StringEscapeUtils.escapeJava( result ).trim();
    }

    /**
     * @param model
     * @param index
     * @param queryString
     * @return
     */
    private static NodeIterator runSearch( OntModel model, IndexLARQ index, String queryString ) {
        String strippedQuery = StringUtils.strip( queryString );
        if ( StringUtils.isBlank( strippedQuery ) ) {
            throw new IllegalArgumentException( "Query cannot be blank" );
        }
//        try {
            StopWatch timer = new StopWatch();
            timer.start();
            NodeIterator iterator = index.searchModelByIndex( model, strippedQuery );

            if ( timer.getTime() > 100 ) {
                log.info( "Ontology resource search for: " + queryString + ": " + timer.getTime() + "ms" );
            }

            return iterator;
/*
        } catch (ARQLuceneException e) {
        	// We assume this is lucene's TooManyClauses exception. (see bug#145)
        	// TODO: maybe we should check. .getCause()? 
        	throw e;   
        } catch ( Exception e ) {
            log.error( "Failed Search for query: " + queryString + " Error was: " + e + " Caused by: "
                    + e.getCause().getMessage() );
        }
        return null;
        */
    }
    
}
