package baseCodeTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {

   public AllTests( String s ) {
      super( s );
   }

   public static Test suite() {
      TestSuite suite = new TestSuite();
      suite
            .addTestSuite( baseCodeTest.io.reader.TestStringMatrixReader.class );
      suite
            .addTestSuite( baseCodeTest.io.reader.TestDoubleMatrixReader.class );
      
      suite
      .addTestSuite( baseCodeTest.io.reader.TestSparseDoubleMatrixReader.class );
      
      suite
      .addTestSuite( baseCodeTest.io.reader.TestSparseRaggedDouble2DNamedMatrixReader.class );
      

      suite
            .addTestSuite( baseCodeTest.io.writer.TestHistogramWriter.class );

      
      suite.addTestSuite(baseCodeTest.dataStructure.matrix.TestSparseRaggedDoubleMatrix2DNamed.class);
      
      suite.addTestSuite( baseCodeTest.dataStructure.TestQueue.class );
      suite.addTestSuite( baseCodeTest.dataStructure.TestStack.class );

      suite.addTestSuite( baseCodeTest.dataFilter.TestRowAffyNameFilter.class );
      suite.addTestSuite( baseCodeTest.dataFilter.TestRowNameFilter.class );
      suite.addTestSuite( baseCodeTest.dataFilter.TestRowAbsentFilter.class );
      suite.addTestSuite( baseCodeTest.dataFilter.TestRowMissingFilter.class );
      suite.addTestSuite( baseCodeTest.dataFilter.TestRowLevelFilter.class );
      suite.addTestSuite( baseCodeTest.dataFilter.TestItemLevelFilter.class );
      
      suite.addTestSuite( baseCodeTest.math.TestDescriptiveWithMissing.class );
      suite.addTestSuite( baseCodeTest.math.TestRank.class );
      suite.addTestSuite( baseCodeTest.math.TestMatrixRowStats.class );
      suite.addTestSuite( baseCodeTest.math.TestMatrixStats.class );
      suite.addTestSuite( baseCodeTest.math.TestStats.class );
      suite.addTestSuite( baseCodeTest.math.TestSpecFunc.class );
      
      suite.addTestSuite( baseCodeTest.xml.TestGOParser.class );

      return suite;
   }

}