package baseCode.io.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A line parser that produces a Map instead of a Collection. Subclasses must provide a method to generate keys.
 * <hr>
 * <p>
 * Copyright (c) 2004-2005 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public abstract class BasicLineMapParser extends BasicLineParser {

    private Map results;

    public BasicLineMapParser() {
        results = new HashMap();
    }

    /**
     * @param probeSetId
     * @see Map
     */
    public Object get( String probeSetId ) {
        return results.get( probeSetId );
    }

    /**
     * Returns a keyset iterator for the Map.
     */
    public Iterator iterator() {
        return results.keySet().iterator();
    }

    /* (non-Javadoc)
     * @see baseCode.io.reader.LineParser#parse(java.io.InputStream)
     */
    public void parse( InputStream is ) throws IOException {
        BufferedReader br = new BufferedReader( new InputStreamReader( is ) );

        String line = null;
        int count = 0;
        while ( ( line = br.readLine() ) != null ) {
            Object newItem = parseOneLine( line );

            if ( newItem != null ) {
                String key = getKey( newItem );
                results.put( key, newItem );
                count++;
            }
            if ( count % ALERT_FREQUENCY == 0 ) log.debug( "Read in " + count + " items..." );

        }
        log.info( "Read in " + count + " items..." );
    }

    /*
     * (non-Javadoc)
     * 
     * @see baseCode.io.reader.BasicLineParser#parseOneLine(java.lang.String)
     */
    public abstract Object parseOneLine( String line );

    /**
     * @param newItem
     * @return
     */
    protected abstract String getKey( Object newItem );

}
