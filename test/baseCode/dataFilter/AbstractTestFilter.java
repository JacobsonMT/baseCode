package baseCode.dataFilter;

import junit.framework.TestCase;
import baseCode.dataStructure.matrix.DenseDoubleMatrix2DNamed;
import baseCode.dataStructure.matrix.StringMatrix2DNamed;
import baseCode.io.reader.DoubleMatrixReader;
import baseCode.io.reader.StringMatrixReader;

/**
 * Fixture for testing filtering of matrices.
 * 
 * @author Pavlidis
 * @version $Id$
 *  
 */

public abstract class AbstractTestFilter extends TestCase {

   protected DenseDoubleMatrix2DNamed testdata = null;
   protected StringMatrix2DNamed teststringdata = null;
   protected DenseDoubleMatrix2DNamed testmissingdata = null;
   protected StringMatrix2DNamed teststringmissingdata = null;

   public AbstractTestFilter() {
      super();
   }

   protected void setUp() throws Exception {
      super.setUp();
      DoubleMatrixReader f = new DoubleMatrixReader();
      StringMatrixReader s = new StringMatrixReader();

      testdata = ( DenseDoubleMatrix2DNamed ) f.read( AbstractTestFilter.class
            .getResourceAsStream( "/data/testdata.txt" ) );

      testmissingdata = ( DenseDoubleMatrix2DNamed ) f
            .read( AbstractTestFilter.class
                  .getResourceAsStream( "/data/testdatamissing.txt" ) );

      teststringdata = ( StringMatrix2DNamed ) s.read( AbstractTestFilter.class
            .getResourceAsStream( "/data/testdata.txt" ) );

      teststringmissingdata = ( StringMatrix2DNamed ) s
            .read( AbstractTestFilter.class
                  .getResourceAsStream( "/data/testdatamissing.txt" ) );

   }

   protected void tearDown() throws Exception {
      super.tearDown();
      testdata = null;
      testmissingdata = null;
      teststringdata = null;
      teststringmissingdata = null;
   }

}