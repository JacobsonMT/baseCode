package baseCodeTest.io.reader;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.TestCase;
import baseCode.dataStructure.matrix.SparseRaggedDoubleMatrix2DNamed;
import baseCode.io.reader.SparseRaggedDouble2DNamedMatrixReader;
import baseCode.util.RegressionTesting;

/**
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004 Columbia University
 * @author pavlidis
 * @version $Id$
 */
public class TestSparseRaggedDouble2DNamedMatrixReader extends TestCase {

   SparseRaggedDoubleMatrix2DNamed matrix = null;
   InputStream is = null;
   SparseRaggedDouble2DNamedMatrixReader reader = null;
   
   /*
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
      super.setUp();
      reader = new SparseRaggedDouble2DNamedMatrixReader();
      is = TestSparseRaggedDouble2DNamedMatrixReader.class
            .getResourceAsStream( "/data/JW-testmatrix.txt" );
      
   }

   /*
    * Class under test for NamedMatrix read(InputStream)
    */
   public void testReadInputStream()   {
      
      try {
         matrix = ( SparseRaggedDoubleMatrix2DNamed ) reader.read ( is, 1  );
         String actualReturn = matrix.toString();
         String expectedReturn = RegressionTesting.readTestResult(TestSparseDoubleMatrixReader.class
               .getResourceAsStream("/data/JW-testoutput.txt"));
          System.err.println(actualReturn);
          System.err.println("Expected:\n" + expectedReturn);
         assertEquals( "return value", expectedReturn, actualReturn );
         
      } catch ( IOException e ) {
         e.printStackTrace();
      } 
      
   }

}
