package baseCodeTest.dataStructure.writer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.Writer;

import baseCode.dataStructure.writer.HistogramWriter;
import baseCode.util.RegressionTesting;
import junit.framework.TestCase;
import hep.aida.ref.Histogram1D;

/**
 * 
 *
 * <hr>
 * <p>Copyright (c) 2004 Columbia University
 * @author pavlidis
 * @version $Id$
 */
public class TestHistogramWriter extends TestCase {

   
   HistogramWriter w = new HistogramWriter();
   Histogram1D m = new Histogram1D("test", 10, 1, 10);
   /*
    * @see TestCase#setUp()
    */
   protected void setUp() throws Exception {
      super.setUp();
      m.fill(1.0);
      m.fill(1.0);
      m.fill(2.0);
      m.fill(1.0);
      m.fill(5.0);
      m.fill(11.0);
      m.fill(6.0);
      m.fill(6.0);
      m.fill(4.0);
      m.fill(4.0);
      m.fill(4.0);
      m.fill(4.0);
      
   }

   /*
    * @see TestCase#tearDown()
    */
   protected void tearDown() throws Exception {
      super.tearDown();
   }

   public final void testWrite() {
      String expectedReturn = "";
      String actualReturn = "";
      Writer k;
      try {
         k = new StringWriter();
          w.write(m, k);
         k.close();
        actualReturn = k.toString();
      } catch ( Exception e ) {
         e.printStackTrace();
      }
      

      try {
         expectedReturn = RegressionTesting.readTestResult( "/data/histogramwritertestoutput.txt" );
      } catch ( IOException e1 ) {
         e1.printStackTrace();
      }
     
      assertEquals( "return value", expectedReturn, actualReturn );
   }

}