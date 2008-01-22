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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import ubic.basecode.dataStructure.matrix.NamedMatrix;
import ubic.basecode.dataStructure.matrix.SparseDoubleMatrix2DNamed;

import com.braju.beta.format.Format;
import com.braju.beta.format.FormatReader;
import com.braju.beta.format.FormatString;
import com.braju.beta.format.Parameters;
import com.braju.beta.lang.DoubleVariable;
import com.braju.beta.lang.IntegerVariable;
import com.braju.beta.lang.NumberVariable;

/**
 * @author pavlidis
 * @version $Id$
 */
public class SparseDoubleMatrixReader extends AbstractNamedMatrixReader {

    /**
     * @param filename data file to read from
     * @return NamedMatrix object constructed from the data file
     * @throws IOException
     */
    public NamedMatrix<String, String> read( String filename ) throws IOException {
        return read( filename, null );
    }

    /**
     * @param stream InputStream stream to read from
     * @return NamedMatrix object constructed from the data file
     * @throws IOException
     */
    public NamedMatrix<String, String> read( InputStream stream ) throws IOException {
        return read( stream, null );
    }

    /**
     * Read a matrix from a file, subject to filtering criteria.
     * 
     * @param filename data file to read from
     * @param wantedRowNames contains names of rows we want to get
     * @return NamedMatrix object constructed from the data file
     * @throws IOException
     */
    public NamedMatrix<String, String> read( String filename, Set<String> wantedRowNames ) throws IOException {
        File infile = new File( filename );
        if ( !infile.exists() || !infile.canRead() ) {
            throw new IOException( "Could not read from file " + filename );
        }
        FileInputStream stream = new FileInputStream( infile );
        return read( stream, wantedRowNames );
    } // end read

    /**
     * Read a sparse matrix in "JW" (Jason Weston) format. The format is like this:
     * 
     * <pre>
     * 2          &lt;--- number of items - the first line of the file only. NOTE - this line is often blank or not present.
     * 1 2        &lt;--- items 1 has 2 edges
     * 1 2        &lt;--- edge indices are to items 1 &amp; 2
     * 0.1 100    &lt;--- with the following weights
     * 2 2        &lt;--- items 2 also has 2 edges
     * 1 2        &lt;--- edge indices are also to items 1 &amp; 2 (fully connected)
     * 100 0.1    &lt;--- with the following weights
     * </pre>
     * 
     * <p>
     * Note that the item numbering starts at 1. This is a requirement.
     * <p>
     * Note that this cannot handle very large matrices - the limit to rows x columns is the number Integer.MAX_VALUE.
     * This is an implementation problem for colt's sparse matrix.
     * 
     * @param stream
     * @param wantedRowNames
     * @return
     * @throws IOException
     */
    public NamedMatrix<String, String> readJW( InputStream stream ) throws IOException, IllegalAccessException,
            NoSuchFieldException {

        BufferedReader dis = new BufferedReader( new InputStreamReader( stream ) );
        FormatReader ff = new FormatReader( dis );
        Parameters p = new Parameters();
        FormatString fmtdd = ff.compileFormatString( "%d %d" );
        FormatString fmtd = ff.compileFormatString( "%d" );
        FormatString fmtg = ff.compileFormatString( "%g" );

        NumberVariable index = new IntegerVariable();
        NumberVariable amount = new IntegerVariable();
        DoubleVariable eval = new DoubleVariable();

        int dim = 10;

        dim = new Integer( Integer.parseInt( dis.readLine() ) ).intValue();
        SparseDoubleMatrix2DNamed<String, String> returnVal = new SparseDoubleMatrix2DNamed<String, String>( dim, dim );

        for ( int k = 1; k <= dim; k++ ) {

            returnVal.addColumnName( new Integer( k ).toString(), k - 1 );
            returnVal.addRowName( new Integer( k ).toString(), k - 1 );

            ff.read( fmtdd, p.add( index ).add( amount ) ); // "item 1 has 2 edges"

            if ( ( index.intValue() % 500 ) == 0 ) {
                log.debug( Format.sprintf( "loading %2.1f%% complete (%dth entry)... \n", p.add(
                        100.0 * ( ( index.intValue() ) / ( ( float ) dim ) ) ).add( index ) ) );
            }

            int[] rowind = new int[amount.intValue()];
            for ( int i = 0; i < amount.intValue(); i++ ) { // "edge indices are to 1 and 2"

                Format.fscanf( ff, fmtd, p.add( index ) );
                int ind = index.intValue();

                if ( ind > dim || ind < 1 ) {
                    throw new IllegalStateException( "Illegal value " + ind + " found in index list for item " + k );
                }
                rowind[i] = ind;
            }

            for ( int i = 0; i < amount.intValue(); i++ ) { // "with the following weights"
                Format.fscanf( ff, fmtg, p.add( eval ) );
                returnVal.setQuick( k - 1, rowind[i] - 1, eval.doubleValue() );
                // returnVal.setQuick( rowind[i] - 1, k - 1, eval.doubleValue() );
            }

        }
        ff.close();
        return returnVal;
    }

    /**
     * Read a sparse matrix that is expressed as an adjacency list in a tab-delimited file:
     * 
     * <pre>
     *  item1 item2 weight
     *  item1 item5 weight
     * </pre>
     * 
     * <p>
     * By definition the resulting matrix is square and symmetric.
     * </p>
     * <p>
     * Note that the ordering of the items will be as they are encountered in the file.
     * 
     * @param stream InputStream
     * @param wantedRowNames Set
     * @return NamedMatrix
     * @throws IOException
     */
    public NamedMatrix<String, String> read( InputStream stream, Set<String> wantedRowNames ) throws IOException {

        Set<String> itemNames = new HashSet<String>();
        Map<String, Collection<IndexScoreDyad>> rows = new HashMap<String, Collection<IndexScoreDyad>>();

        BufferedReader dis = new BufferedReader( new InputStreamReader( stream ) );

        String row;
        int index = 0;
        Map<String, Integer> nameIndexMap = new HashMap<String, Integer>(); // name --> eventual row index
        while ( ( row = dis.readLine() ) != null ) {
            StringTokenizer st = new StringTokenizer( row, " \t", false );

            String itemA = "";

            if ( st.hasMoreTokens() ) {
                itemA = st.nextToken();

                if ( !itemNames.contains( itemA ) ) {
                    rows.put( itemA, new HashSet<IndexScoreDyad>() );
                    itemNames.add( itemA );
                    nameIndexMap.put( itemA, new Integer( index ) );
                    index++;
                }
            } else {
                // continue;
            }

            String itemB = "";
            if ( st.hasMoreTokens() ) {
                itemB = st.nextToken();
                if ( !itemNames.contains( itemB ) ) {
                    rows.put( itemB, new HashSet<IndexScoreDyad>() );
                    itemNames.add( itemB );
                    nameIndexMap.put( itemB, new Integer( index ) );
                    index++;
                }
            } else {
                // continue;
            }

            double weight;
            if ( st.hasMoreTokens() ) {
                weight = Double.parseDouble( st.nextToken() );
            } else {
                weight = 1.0; // just make it a binary matrix.
            }

            rows.get( itemA ).add( new IndexScoreDyad( nameIndexMap.get( itemB ).intValue(), weight ) );
            rows.get( itemB ).add( new IndexScoreDyad( nameIndexMap.get( itemA ).intValue(), weight ) );
        }

        SparseDoubleMatrix2DNamed<String, String> matrix = new SparseDoubleMatrix2DNamed<String, String>( itemNames
                .size(), itemNames.size() );

        List<String> itemVec = new Vector<String>( itemNames );
        Collections.sort( itemVec );

        matrix.setColumnNames( itemVec );
        matrix.setRowNames( itemVec );
        for ( Iterator iter = itemNames.iterator(); iter.hasNext(); ) {
            String itemA = ( String ) iter.next();
            int rowIndex = matrix.getRowIndexByName( itemA );
            Set arow = ( Set ) rows.get( itemA );
            for ( Iterator iterator = arow.iterator(); iterator.hasNext(); ) {
                IndexScoreDyad element = ( IndexScoreDyad ) iterator.next();
                int ind = element.getKey();
                double weight = element.getValue();

                matrix.setQuick( rowIndex, ind, weight );
                matrix.setQuick( ind, rowIndex, weight );
            }

        }

        dis.close();
        return matrix;
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.io.reader.AbstractNamedMatrixReader#readOneRow(java.io.BufferedReader)
     */
    public NamedMatrix<String, String> readOneRow( BufferedReader dis ) {
        // this is impossible for the pair method.
        throw new UnsupportedOperationException();
    }

}
