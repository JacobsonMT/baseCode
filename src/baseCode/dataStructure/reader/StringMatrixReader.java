package baseCode.dataStructure.reader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

import baseCode.dataStructure.NamedMatrix;
import baseCode.dataStructure.StringMatrix2DNamed;

/**
 * Reader for {@link baseCode.dataStructure.StringMatrix2DNamed}
 * <p> Copyright (c) 2004</p>
 * <p>Institution:: Columbia University</p>
 * @author Paul Pavlidis
 * @version $Id$
 */
public class StringMatrixReader
    extends MatrixReader {

   public NamedMatrix read( String filename ) throws IOException {
      StringMatrix2DNamed matrix = null;
      Vector MTemp = new Vector();
      Vector rowNames = new Vector();
      Vector columnNames;

      BufferedReader dis = new BufferedReader( new FileReader( filename ) );
      int columnNumber = 0;
      int rowNumber = 0;
      String row;

      columnNames = readHeader( dis );
      int numHeadings = columnNames.size();

      while ( ( row = dis.readLine() ) != null ) {
         StringTokenizer st = new StringTokenizer( row, "\t", true );
         Vector rowTemp = new Vector();
         columnNumber = 0;
         String previousToken = "";

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
            }

            if ( columnNumber > 0 ) {
               if ( missing ) {
                  rowTemp.add( Double.toString( Double.NaN ) );
               } else {
                  rowTemp.add( s );
               }
            } else {
               if ( missing ) {
                  throw new IOException(
                      "Missing values not allow for row labels" );
                  // bad, no missing values allowed for row labels.
               } else {
                  rowNames.add( s );
               }
            }

            columnNumber++;
            previousToken = s;
         }
         MTemp.add( rowTemp );
         if ( rowTemp.size() > numHeadings ) {
            throw new IOException( "Warning: too many values (" + rowTemp.size() +
                                   ") in row " +
                                   rowNumber + " (based on headings count of " +
                                   numHeadings +
                                   ")" );
         }
         rowNumber++;
      }

      matrix = new StringMatrix2DNamed( rowNumber, numHeadings );
      matrix.setColumnNames( columnNames );

      matrix.setRowNames( rowNames );

      for ( int i = 0; i < matrix.rows(); i++ ) {
         for ( int j = 0; j < matrix.columns(); j++ ) {
            if ( ( ( Vector ) MTemp.get( i ) ).size() < j + 1 ) {
               matrix.set( i, j, "" ); // this allows the input file to have ragged ends.
            } else {
               matrix.set( i, j,
                           ( String ) ( ( Vector ) MTemp.get( i ) ).get( j ) );
            }
         }
      }

      return matrix;

   }
}
