package baseCode.dataStructure.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.NamedMatrix;
import java.util.Set;

/**
 *
 * Reader for {@link baseCode.dataStructure.DenseDoubleMatrix2DNamed}.
 * <p>Copyright (c) 2004</p>
 * <p>Institution: Columbia University</p>
 * @author Paul Pavlidis
 * @version $Id$
 */
public class DoubleMatrixReader extends AbstractNamedMatrixReader {

   /**
    * @param   filename     data file to read from
    * @return  NamedMatrix  object constructed from the data file
    */
   public NamedMatrix read(String filename) throws IOException {
      return read(filename, null);
   }

   /**
   	* @param  stream InputStream stream to read from
   	* @return  NamedMatrix  object constructed from the data file
   	*/
   public NamedMatrix read(InputStream stream) throws IOException {
      return read(stream, null);
   }

   /**
    *
    * @param stream
    * @param wantedRowNames
    * @return
    * @throws IOException
    * @todo should take a set, not an array.
    */
   public NamedMatrix read(InputStream stream, Set wantedRowNames)
      throws IOException {

      BufferedReader dis = new BufferedReader(new InputStreamReader(stream));

      Vector MTemp = new Vector();
      Vector colNames;
      Vector rowNames = new Vector();

      //BufferedReader dis = new BufferedReader( new FileReader( filename ) );
      int columnNumber = 0;
      int rowNumber = 0;
      String row;

      colNames = readHeader(dis);
      int numHeadings = colNames.size();

      while ((row = dis.readLine()) != null) {
         StringTokenizer st = new StringTokenizer(row, "\t", true);

         //
         // Skip rows in which we are not interested
         //
         String s = null;
         if (st.hasMoreTokens()) {
            s = st.nextToken();
         } else {
            continue;
         }
         if (wantedRowNames != null) {

            // if we already have all the rows we want, then bail out
            if (rowNumber >= wantedRowNames.size()) {
               return createMatrix(
                  MTemp,
                  rowNumber,
                  numHeadings,
                  rowNames,
                  colNames);
            }
            // skip this row if it's not in wantedRowNames
            else if ( ! wantedRowNames.contains( s ) ) {
               continue;
            }
         }

         //
         // If we're here, we don't want to skip this row, so parse it
         //
         Vector rowTemp = new Vector();
         columnNumber = 0;
         String previousToken = "";

         while (st.hasMoreTokens()) {
            // Iterate through the row, parsing it into row name and values
            if (columnNumber > 0) {
               // if columnNumber == 0, then we just entered this loop and
               // have already called st.nextToken() above to get the first
               // string, so we don't want to call it twice, or we'll skip
               // the row name
               s = st.nextToken();
            }
            boolean missing = false;

            if (s.compareTo("\t") == 0) {
               /* two tabs in a row */
               if (previousToken.compareTo("\t") == 0) {
                  missing = true;
               } else if (!st.hasMoreTokens()) { // at end of line.
                  missing = true;
               } else {
                  previousToken = s;
                  continue;
               }
            } else if (s.compareTo(" ") == 0) {
               if (previousToken.compareTo("\t") == 0) {
                  missing = true;
               } else {
                  throw new IOException("Spaces not allowed after values");
                  // bad, not allowed.
               }
            } else if (s.compareToIgnoreCase("NaN") == 0) {
               if (previousToken.compareTo("\t") == 0) {
                  missing = true;
               } else {
                  throw new IOException("NaN found where it isn't supposed to be");
                  // bad, not allowed - missing a tab?
               }
            }

            if (columnNumber > 0) {
               if (missing) {
                  rowTemp.add(Double.toString(Double.NaN));
               } else {
                  rowTemp.add(s);
               }
            } else {
               if (missing) {
                  throw new IOException("Missing values not allowed for row labels");
               } else {
                  rowNames.add(s);
               }
            }

            columnNumber++;
            previousToken = s;
         } // end while (st.hasMoreTokens())
         // done parsing one row -- no more tokens

         MTemp.add(rowTemp);
         if (rowTemp.size() > numHeadings) {
            throw new IOException(
               "Too many values ("
                  + rowTemp.size()
                  + ") in row "
                  + rowNumber
                  + " (based on headings count of "
                  + numHeadings
                  + ")");
         }
         rowNumber++;
      }

      return createMatrix(MTemp, rowNumber, numHeadings, rowNames, colNames);

   }

   /**
    * Read a matrix from a file, subject to filtering criteria.
    * @param   filename        data file to read from
    * @param   wantedRowNames  contains names or rows we want to get
    * @return  NamedMatrix     object constructed from the data file
    * @todo make this take a set instead of a String[] for wantedRowNames.
    */
   public NamedMatrix read(String filename, Set wantedRowNames)
      throws IOException {
      File infile = new File(filename);
      if (!infile.exists() || !infile.canRead()) {
         throw new IllegalArgumentException("Could not read from " + filename);
      }
      FileInputStream stream = new FileInputStream(infile);
      return read(stream, wantedRowNames);
   } // end read

   protected DenseDoubleMatrix2DNamed createMatrix(
      Vector MTemp,
      int rowCount,
      int colCount,
      Vector rowNames,
      Vector colNames) {

      DenseDoubleMatrix2DNamed matrix =
         new DenseDoubleMatrix2DNamed(rowCount, colCount);
      matrix.setRowNames(rowNames);
      matrix.setColumnNames(colNames);

      for (int i = 0; i < matrix.rows(); i++) {
         for (int j = 0; j < matrix.columns(); j++) {
            if (((Vector)MTemp.get(i)).size() < j + 1) {
               matrix.set(i, j, Double.NaN);
               // this allows the input file to have ragged ends.
            } else {
               matrix.set(
                  i,
                  j,
                  Double.parseDouble((String) ((Vector)MTemp.get(i)).get(j)));
            }
         }
      }
      return matrix;

   } // end createMatrix

} // end class DoubleMatrixReader
