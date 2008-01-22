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
package ubic.basecode.io.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.NamedMatrix;

/**
 * Abstract class representing an object that can read in a {@link NamedMatrix}from a file.
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public abstract class AbstractNamedMatrixReader {

    public abstract NamedMatrix<String, String> read( String filename ) throws IOException;

    public abstract NamedMatrix<String, String> read( InputStream stream ) throws IOException;

    public abstract NamedMatrix<String, String> readOneRow( BufferedReader dis ) throws IOException;

    protected static final Log log = LogFactory.getLog( AbstractNamedMatrixReader.class );

    protected List<String> readHeader( BufferedReader dis ) throws IOException {
        List<String> headerVec = new Vector<String>();
        String header = dis.readLine();
        StringTokenizer st = new StringTokenizer( header, "\t", true ); // return
        // delims.

        String previousToken = "";
        int columnNumber = 0;
        while ( st.hasMoreTokens() ) {
            String s = st.nextToken();
            boolean missing = false;

            if ( s.compareTo( "\t" ) == 0 ) {
                /* two tabs in a row */
                if ( previousToken.compareTo( "\t" ) == 0 ) {
                    missing = true;
                } else if ( !st.hasMoreTokens() ) { // at end of line.
                    missing = true;
                } else {
                    previousToken = s;
                    continue;
                }
            } else if ( s.compareTo( " " ) == 0 ) {
                if ( previousToken.compareTo( "\t" ) == 0 ) {
                    missing = true;
                }
            }

            if ( missing ) {
                throw new IOException( "Warning: Missing values not allowed in the header (column " + columnNumber
                        + ")" );
            } else if ( columnNumber > 0 ) {
                headerVec.add( s );
            }
            // otherwise, just the corner string.
            columnNumber++;
            previousToken = s;
        }

        // return columnNumber - 1;
        if ( headerVec.size() == 0 ) {
            log.warn( "No headings found" );
        }

        return headerVec;

    }

}