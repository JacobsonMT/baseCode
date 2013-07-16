/*
 * The baseCode project
 * 
 * Copyright (c) 2011 University of British Columbia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package ubic.basecode.math;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.distribution.FDistribution;
import org.apache.commons.math.distribution.FDistributionImpl;
import org.junit.Test;

import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix1D;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.dataStructure.matrix.ObjectMatrix;
import ubic.basecode.dataStructure.matrix.ObjectMatrixImpl;
import ubic.basecode.dataStructure.matrix.StringMatrix;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.basecode.io.reader.StringMatrixReader;
import ubic.basecode.util.r.type.GenericAnovaResult;
import ubic.basecode.util.r.type.LinearModelSummary;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;

/**
 * @author paul
 * @version $Id$
 */
public class LeastSquaresFitTest {

    @SuppressWarnings("unused")
    private static Log log = LogFactory.getLog( LeastSquaresFitTest.class );

    /**
     * @throws Exception
     */
    @Test
    public void testLSFOneContinuousWithMissing3() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/example.madata.withmissing.small.txt" ) );

        ObjectMatrix<String, String, Object> design = new ObjectMatrixImpl<String, String, Object>( 9, 1 );
        design.set( 0, 0, 0.12 );
        design.set( 1, 0, 0.24 );
        design.set( 2, 0, 0.48 );
        design.set( 3, 0, 0.96 );
        design.set( 4, 0, 0.12 );
        design.set( 5, 0, 0.24 );
        design.set( 6, 0, 0.48 );
        design.set( 7, 0, 0.96 );
        design.set( 8, 0, 0.96 );

        design.setRowNames( testMatrix.getColNames() );
        design.addColumnName( "Value" );

        LeastSquaresFit fit = new LeastSquaresFit( design, testMatrix );

        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );

        LinearModelSummary s = sums.get( "228980_at" ); // has missing
        assertNotNull( s );
        assertEquals( 0.1495, s.getF(), 0.01 );
        assertEquals( 0.7123, s.getP(), 0.001 );
        assertEquals( 10.9180, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.712, s.getCoefficients().get( 1, 3 ), 0.001 );
        assertEquals( 6, s.getResidualDof().intValue() );
        GenericAnovaResult a = s.getAnova();
        assertEquals( 0.1495, a.getMainEffectF( "Value" ), 0.0001 );
        assertEquals( 1, a.getMainEffectDof( "Value" ).intValue() );
        assertEquals( 6, a.getResidualDf().intValue() );

        FDistribution fd = new FDistributionImpl( 1, 6 );
        double p = 1.0 - fd.cumulativeProbability( 0.1495 );
        assertEquals( 0.7123, p, 0.0001 );
        assertEquals( 0.7123, a.getMainEffectP( "Value" ), 0.0001 );

        s = sums.get( "1553129_at" );
        assertNotNull( s );
        assertEquals( 2.095, s.getF(), 0.01 );
        assertEquals( 0.1911, s.getP(), 0.001 );
        assertEquals( 3.78719, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.191, s.getCoefficients().get( 1, 3 ), 0.001 ); // this ordering might change?
        a = s.getAnova();
        assertNotNull( a );
        assertEquals( 0.1911, a.getMainEffectP( "Value" ), 0.0001 );

        s = fit.summarize( 14 );
        assertNotNull( s );
        assertEquals( "214502_at", s.getKey() );// has missing
        assertEquals( 1.992, s.getF(), 0.01 );
        assertEquals( 0.2172, s.getP(), 0.001 );
        assertEquals( 4.2871, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.217, s.getCoefficients().get( 1, 3 ), 0.001 );

        s = sums.get( "232018_at" );
        assertNotNull( s );
        assertEquals( 1.381, s.getF(), 0.01 );
        assertEquals( 0.2783, s.getP(), 0.001 );
        assertEquals( 6.6537, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.278, s.getCoefficients().get( 1, 3 ), 0.001 );
        a = s.getAnova();
        assertNotNull( a );
        assertEquals( 0.2783, a.getMainEffectP( "Value" ), 0.0001 );
        s = sums.get( "228980_at" ); // has missing
        assertNotNull( s );
        assertEquals( 0.1495, s.getF(), 0.01 );
        assertEquals( 0.7123, s.getP(), 0.001 );
        assertEquals( 10.9180, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.712, s.getCoefficients().get( 1, 3 ), 0.001 );
        a = s.getAnova();
        assertNotNull( a );
        assertEquals( 0.7123, a.getMainEffectP( "Value" ), 0.0001 );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testLSFThreeLevelsOnecontinous2() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/example.madata.small.txt" ) );

        ObjectMatrix<String, String, Object> design = new ObjectMatrixImpl<String, String, Object>( 9, 2 );

        design.set( 0, 0, "A" );
        design.set( 1, 0, "A" );
        design.set( 2, 0, "A" );
        design.set( 3, 0, "B" );
        design.set( 4, 0, "B" );
        design.set( 5, 0, "B" );
        design.set( 6, 0, "C" );
        design.set( 7, 0, "C" );
        design.set( 8, 0, "C" );
        design.set( 0, 1, 0.12 );
        design.set( 1, 1, 0.24 );
        design.set( 2, 1, 0.48 );
        design.set( 3, 1, 0.96 );
        design.set( 4, 1, 0.12 );
        design.set( 5, 1, 0.24 );
        design.set( 6, 1, 0.48 );
        design.set( 7, 1, 0.96 );
        design.set( 8, 1, 0.96 );

        design.setRowNames( testMatrix.getColNames() );
        design.addColumnName( "Factor" );
        design.addColumnName( "Value" );

        LeastSquaresFit fit = new LeastSquaresFit( design, testMatrix );

        DoubleMatrix2D coeffs = fit.getCoefficients();
        assertEquals( 3.7458080, coeffs.get( 0, 0 ), 0.0001 );
        assertEquals( -0.4388889, coeffs.get( 1, 2 ), 0.0001 );
        assertEquals( 0.5709091, coeffs.get( 2, 10 ), 0.0001 );
        assertEquals( 0.04856061, coeffs.get( 2, 18 ), 0.0001 );
        assertEquals( -1.1363636, coeffs.get( 3, 10 ), 0.0001 );
        assertEquals( 0.11174242, coeffs.get( 3, 18 ), 0.0001 );

        DoubleMatrix2D fitted = fit.getFitted();

        assertEquals( 3.764747, fitted.get( 0, 0 ), 0.0001 );
        assertEquals( 6.043990, fitted.get( 1, 3 ), 0.0001 );
        assertEquals( 10.858586, fitted.get( 7, 2 ), 0.0001 );
        assertEquals( 6.307879, fitted.get( 18, 8 ), 0.0001 );

        List<GenericAnovaResult> anova = fit.anova();
        assertEquals( 19, anova.size() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testLSFThreeLevelsOneContinuousWithMissing3() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/example.madata.withmissing.small.txt" ) );

        ObjectMatrix<String, String, Object> design = new ObjectMatrixImpl<String, String, Object>( 9, 2 );

        design.set( 0, 0, "A" );
        design.set( 1, 0, "A" );
        design.set( 2, 0, "A" );
        design.set( 3, 0, "B" );
        design.set( 4, 0, "B" );
        design.set( 5, 0, "B" );
        design.set( 6, 0, "C" );
        design.set( 7, 0, "C" );
        design.set( 8, 0, "C" );
        design.set( 0, 1, 0.12 );
        design.set( 1, 1, 0.24 );
        design.set( 2, 1, 0.48 );
        design.set( 3, 1, 0.96 );
        design.set( 4, 1, 0.12 );
        design.set( 5, 1, 0.24 );
        design.set( 6, 1, 0.48 );
        design.set( 7, 1, 0.96 );
        design.set( 8, 1, 0.96 );

        design.setRowNames( testMatrix.getColNames() );
        design.addColumnName( "Factor" );
        design.addColumnName( "Value" );

        LeastSquaresFit fit = new LeastSquaresFit( design, new DenseDoubleMatrix2D( testMatrix.asArray() ) );

        DoubleMatrix2D coeffs = fit.getCoefficients();
        assertEquals( 3.7458080, coeffs.get( 0, 0 ), 0.0001 );
        assertEquals( -0.4388889, coeffs.get( 1, 2 ), 0.0001 );
        assertEquals( 0.5709091, coeffs.get( 2, 10 ), 0.0001 );
        assertEquals( 0.04856061, coeffs.get( 2, 18 ), 0.0001 );
        assertEquals( -1.1363636, coeffs.get( 3, 10 ), 0.0001 );
        assertEquals( 0.11174242, coeffs.get( 3, 18 ), 0.0001 );

        DoubleMatrix2D fitted = fit.getFitted();

        assertEquals( 3.764747, fitted.get( 0, 0 ), 0.0001 );
        assertEquals( 6.043990, fitted.get( 1, 3 ), 0.0001 );
        assertEquals( 10.8333, fitted.get( 7, 2 ), 0.0001 );
        assertEquals( 6.307879, fitted.get( 18, 8 ), 0.0001 );

        List<GenericAnovaResult> anova = fit.anova();

        assertEquals( 19, anova.size() );
    }

    /**
     * @throws Exception
     */
    @Test
    public void testLSFTwoLevels() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/example.madata.small.txt" ) );

        ObjectMatrix<String, String, Object> design = new ObjectMatrixImpl<String, String, Object>( 9, 1 );

        design.set( 0, 0, "A" );
        design.set( 1, 0, "A" );
        design.set( 2, 0, "A" );
        design.set( 3, 0, "A" );
        design.set( 4, 0, "B" );
        design.set( 5, 0, "B" );
        design.set( 6, 0, "B" );
        design.set( 7, 0, "B" );
        design.set( 8, 0, "B" );
        design.setRowNames( testMatrix.getColNames() );
        design.addColumnName( "Factor" );

        LeastSquaresFit fit = new LeastSquaresFit( design, testMatrix );

        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );

        LinearModelSummary s = sums.get( "1553129_at" );
        assertEquals( 0.183, s.getF(), 0.01 );
        assertEquals( 0.6817, s.getP(), 0.001 );
        assertEquals( 3.84250, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.682, s.getCoefficients().get( 1, 3 ), 0.001 );

        s = sums.get( "232018_at" );
        assertEquals( 0.07879, s.getF(), 0.01 );
        assertEquals( 0.787, s.getP(), 0.001 );
        assertEquals( 6.2650, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.787, s.getCoefficients().get( 1, 3 ), 0.001 );
    }

    /**
     * Many missing values; Two factors, two levels + interaction.
     * 
     * @throws Exception
     */
    @Test
    public void testLSFTwoLevels2() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/lmtest1.dat.manymissing.txt" ) );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass()
                .getResourceAsStream( "/data/lmtest1.des.txt" ) );

        DesignMatrix d = new DesignMatrix( sampleInfo, true );
        d.addInteraction();

        LeastSquaresFit fit = new LeastSquaresFit( d, testMatrix );
        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );
        assertEquals( 100, sums.size() );

        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult a = lms.getAnova();
            assertNotNull( a );
            Double interactionEffectP = a.getInteractionEffectP();
            assertNotNull( interactionEffectP );
        }

    }

    /**
     * @throws Exception
     */
    @Test
    public void testLSFTwoLevels3() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/anova-test-data.txt" ) );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass().getResourceAsStream(
                "/data/anova-test-des.txt" ) );

        DesignMatrix d = new DesignMatrix( sampleInfo, true );
        d.addInteraction( "factor1", "factor2" );

        LeastSquaresFit fit = new LeastSquaresFit( d, testMatrix );
        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );
        assertEquals( 100, sums.size() );

        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult a = lms.getAnova();
            assertNotNull( a );
            Double interactionEffectP = a.getInteractionEffectP();
            assertNotNull( interactionEffectP );
        }

        assertEquals( 0.0048, sums.get( "probe_4" ).getMainEffectP( "factor1" ), 0.0001 );
        assertEquals( 5.158e-10, sums.get( "probe_10" ).getMainEffectP( "factor1" ), 1e-12 );
        assertEquals( 0.6888, sums.get( "probe_98" ).getMainEffectP( "factor2" ), 1e-4 );
        assertEquals( 0.07970, sums.get( "probe_10" ).getMainEffectP( "factor2" ), 1e-4 );

    }

    /**
     * @throws Exception
     */
    @Test
    public void testLSFTwoLevelsOneContinuous() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/example.madata.small.txt" ) );

        ObjectMatrix<String, String, Object> design = new ObjectMatrixImpl<String, String, Object>( 9, 2 );

        design.set( 0, 0, "A" );
        design.set( 1, 0, "A" );
        design.set( 2, 0, "A" );
        design.set( 3, 0, "A" );
        design.set( 4, 0, "B" );
        design.set( 5, 0, "B" );
        design.set( 6, 0, "B" );
        design.set( 7, 0, "B" );
        design.set( 8, 0, "B" );
        design.set( 0, 1, 0.12 );
        design.set( 1, 1, 0.24 );
        design.set( 2, 1, 0.48 );
        design.set( 3, 1, 0.96 );
        design.set( 4, 1, 0.12 );
        design.set( 5, 1, 0.24 );
        design.set( 6, 1, 0.48 );
        design.set( 7, 1, 0.96 );
        design.set( 8, 1, 0.96 );

        design.setRowNames( testMatrix.getColNames() );
        design.addColumnName( "Factor" );
        design.addColumnName( "Value" );

        LeastSquaresFit fit = new LeastSquaresFit( design, testMatrix );

        DoubleMatrix2D coeffs = fit.getCoefficients();
        assertEquals( 3.77868, coeffs.get( 0, 0 ), 0.0001 );
        assertEquals( 0.24476, coeffs.get( 1, 2 ), 0.0001 );
        assertEquals( -0.680449, coeffs.get( 2, 10 ), 0.0001 );
        assertEquals( 0.114084, coeffs.get( 2, 18 ), 0.0001 );

        DoubleMatrix2D fitted = fit.getFitted();

        assertEquals( 3.795698, fitted.get( 0, 0 ), 0.0001 );
        assertEquals( 5.497165, fitted.get( 1, 3 ), 0.0001 );
        assertEquals( 10.879917, fitted.get( 7, 2 ), 0.0001 );
        assertEquals( 6.346546, fitted.get( 18, 8 ), 0.0001 );

        List<GenericAnovaResult> anova = fit.anova();
        assertEquals( 19, anova.size() );
        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );

        LinearModelSummary s = sums.get( "1553129_at" );
        assertEquals( 0.9389, s.getF(), 0.01 );
        assertEquals( 0.4418, s.getP(), 0.001 );
        assertEquals( 3.77868, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.810, s.getCoefficients().get( 1, 3 ), 0.001 ); // this ordering might change?
        GenericAnovaResult a = s.getAnova();
        assertEquals( 0.2429, a.getMainEffectP( "Value" ), 0.0001 );

        s = sums.get( "232018_at" );
        assertEquals( 0.7167, s.getF(), 0.01 );
        assertEquals( 0.5259, s.getP(), 0.001 );
        assertEquals( 6.5712, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.664, s.getCoefficients().get( 1, 3 ), 0.001 );
        a = s.getAnova();
        assertEquals( 0.2893, a.getMainEffectP( "Value" ), 0.0001 );

        // 232018_at // based on rstudent() in R.
        DoubleMatrix2D studentizedResiduals = fit.getStudentizedResiduals();
        // log.info( studentizedResiduals.viewRow( 10 ) );
        double[] expectedStudentizedResiduals = new double[] { -0.34655041, 1.46251738, -0.61403124, -0.34663812,
                -1.51245468, 0.06875469, 1.45818880, 1.02811044, -1.31696150 };

        for ( int i = 0; i < 9; i++ ) {
            assertEquals( expectedStudentizedResiduals[i], studentizedResiduals.viewRow( 10 ).get( i ), 0.001 );
        }

        // assertEquals( 1.1, DescriptiveWithMissing.variance( new DoubleArrayList( expectedStudentizedResiduals ) ),
        // 0.1 );

        assertEquals( 1.1,
                DescriptiveWithMissing.variance( new DoubleArrayList( studentizedResiduals.viewRow( 10 ).toArray() ) ),
                0.1 );

        // 1553129_at // based on rstudent() in R.
        studentizedResiduals = fit.getStudentizedResiduals();
        expectedStudentizedResiduals = new double[] { 0.46128657, -5.49429390, 0.84157385, 1.10053286, 1.10538546,
                -0.01706794, -0.05318259, -0.56926585, -0.35107932 };
        for ( int i = 0; i < 9; i++ ) {
            assertEquals( expectedStudentizedResiduals[i], studentizedResiduals.viewRow( 0 ).get( i ), 0.001 );
        }

    }

    /**
     * Test data with many missing values, two factors
     * 
     * @throws Exception
     */
    @Test
    public void testMatrixWeightedMeanVariance() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f
                .read( this.getClass().getResourceAsStream( "/data/lmtest2.dat.txt" ) );
        DoubleMatrix1D librarySize = MatrixStats.colSums( testMatrix );
        MatrixStats.convertToLog2Cpm( testMatrix, librarySize );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass()
                .getResourceAsStream( "/data/lmtest3.des.txt" ) );

        DesignMatrix d = new DesignMatrix( sampleInfo, true );

        // Coefficients not estimable: y$fact.8095fv_60796
        // fit$rank < ncol(design)
        MeanVarianceEstimator est = new MeanVarianceEstimator( d, testMatrix, librarySize );
        DoubleMatrix2D w2D = est.getWeights();
        LeastSquaresFit fit = new LeastSquaresFit( d.getDoubleMatrix(), est.getNormalizedValue(), w2D );
        DoubleMatrix2D actuals = fit.getCoefficients().viewDice();

        // note: column 5 has 0's and 1's reversed compared to des.txt!
        // so signs in column five from R's output have been reversed
        int[] expectedIndices = new int[] { 0, 40, 80 };
        double[][] expected = new double[][] {
                { 13.32, 0.1264, -0.1402, -0.1102, -0.1437, -0.07242, Double.NaN, 0.2211, 0.05124 },
                { 13.08, 0.2029, 0.1464, 0.03702, 0.342, -0.09954, Double.NaN, 0.02198, 0.1536 },
                { 13.4, 0.1718, 0.1385, 0.1185, -0.273, 0.0379, Double.NaN, -0.004468, 0.04201 } };

        for ( int i = 0; i < expectedIndices.length; i++ ) {
            assertArrayEquals( actuals.viewRow( expectedIndices[i] ).toArray(), expected[i], 0.1 );
        }
    }

    /**
     * Weighted least squares test for 2D matrices
     * 
     * @throws Exception
     */
    @Test
    public void testMatrixWeightedRegress() throws Exception {

        DoubleMatrix2D dat = new DenseDoubleMatrix2D( new double[][] { { 1, 2, 3, 4, 5 }, { 1, 1, 6, 3, 2 } } );
        DoubleMatrix2D des = new DenseDoubleMatrix2D( new double[][] { { 1, 1, 1, 1, 1 }, { 1, 2, 2, 3, 3 },
                { 2, 1, 5, 3, 4 } } );

        DoubleMatrix2D w = dat.copy();
        w.assign( dat );
        Algebra solver = new Algebra();
        des = solver.transpose( des );
        LeastSquaresFit fit = new LeastSquaresFit( des, dat, w );

        // coefficients
        DoubleMatrix2D actuals = solver.transpose( fit.getCoefficients() );
        double[][] expected = new double[][] { { -1.7070, 1.7110, 0.3054 }, { 0.2092, -0.6642, 1.3640 } };
        for ( int i = 0; i < expected.length; i++ ) {
            assertArrayEquals( actuals.viewRow( i ).toArray(), expected[i], 0.1 );
        }

        // fitted
        actuals = fit.getFitted();
        expected = new double[][] { { 0.6151, 2.0210, 3.2430, 4.3430, 4.6490 }, { 2.273, 0.245, 5.701, 2.309, 3.673 } };
        for ( int i = 0; i < expected.length; i++ ) {
            assertArrayEquals( actuals.viewRow( i ).toArray(), expected[i], 0.1 );
        }

        // residuals
        actuals = fit.getResiduals();
        expected = new double[][] { { 0.38490, -0.02092, -0.24270, -0.34310, 0.35150 },
                { -1.2730, 0.7550, 0.2986, 0.6910, -1.6730 } };
        for ( int i = 0; i < expected.length; i++ ) {
            assertArrayEquals( actuals.viewRow( i ).toArray(), expected[i], 0.1 );
        }

    }

    /**
     * Has a lot of missing values.
     * 
     * @throws Exception
     */
    @Test
    public void testOneWayAnova() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/anova-test-data.txt" ) );

        ObjectMatrix<String, String, Object> design = new ObjectMatrixImpl<String, String, Object>( 8, 1 );
        for ( int i = 0; i < 8; i++ ) {
            design.set( i, 0, "A" + i % 3 );
        }
        design.addColumnName( "Factor1" );

        DesignMatrix d = new DesignMatrix( design, true );

        LeastSquaresFit fit = new LeastSquaresFit( d, testMatrix );
        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );
        assertEquals( 100, sums.size() );

        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult a = lms.getAnova();
            assertNotNull( a );
            Double interactionEffectP = a.getInteractionEffectP();
            assertNull( interactionEffectP );
        }

        LinearModelSummary sum4 = sums.get( "probe_4" );
        assertNotNull( sum4.getCoefficients() );
        assertEquals( 0.6531, sum4.getP(), 0.0001 );
        assertEquals( 0.2735, sum4.getF(), 0.0001 );
        assertEquals( 0.2735, sum4.getAnova().getMainEffectF( "Factor1" ), 0.0001 );
        assertEquals( 2, sum4.getAnova().getResidualDf().intValue() );
        assertEquals( 1, sum4.getAnova().getMainEffectDof( "Factor1" ).intValue() );
        assertEquals( 0.6531, sum4.getMainEffectP( "Factor1" ), 0.0001 );

        LinearModelSummary sum21 = sums.get( "probe_21" );
        assertNotNull( sum21.getCoefficients() );
        assertEquals( 0.6492, sum21.getP(), 0.0001 );
        assertEquals( 0.4821, sum21.getF(), 0.0001 );
        assertEquals( 0.4821, sum21.getAnova().getMainEffectF( "Factor1" ), 0.0001 );
        assertEquals( 4, sum21.getAnova().getResidualDf().intValue() );
        assertEquals( 2, sum21.getAnova().getMainEffectDof( "Factor1" ).intValue() );
        assertEquals( 0.6492, sum21.getMainEffectP( "Factor1" ), 0.0001 );

        LinearModelSummary sum98 = sums.get( "probe_98" );
        assertNotNull( sum98.getCoefficients() );
        assertEquals( 0.1604, sum98.getP(), 0.0001 );
        assertEquals( 2.993, sum98.getF(), 0.0001 );
        assertEquals( 4, sum98.getAnova().getResidualDf().intValue() );
        assertEquals( 2, sum98.getAnova().getMainEffectDof( "Factor1" ).intValue() );
        assertEquals( 2.9931, sum98.getAnova().getMainEffectF( "Factor1" ).doubleValue(), 0.0001 );
        assertEquals( 0.1604, sum98.getMainEffectP( "Factor1" ), 1e-4 );

        LinearModelSummary sum10 = sums.get( "probe_10" );
        assertNotNull( sum10.getCoefficients() );
        assertEquals( 0.8014, sum10.getP(), 0.0001 );
        assertEquals( 0.2314, sum10.getF(), 0.0001 );
        assertEquals( 5, sum10.getAnova().getResidualDf().intValue() );
        assertEquals( 2, sum10.getAnova().getMainEffectDof( "Factor1" ).intValue() );
        assertEquals( 0.8014, sum10.getMainEffectP( "Factor1" ), 1e-4 );

        LinearModelSummary sum60 = sums.get( "probe_60" );
        assertNotNull( sum60.getCoefficients() );
        assertEquals( 9.001, sum60.getCoefficients().get( 0, 0 ), 0.01 ); // R gives 8.9913
        assertEquals( -0.0102, sum60.getCoefficients().get( 1, 0 ), 0.0001 ); // R gives +0.0102
        assertEquals( 2, sum60.getAnova().getResidualDf().intValue() );
        assertEquals( 1, sum60.getAnova().getMainEffectDof( "Factor1" ).intValue() );
        assertEquals( 0.0004715, sum60.getF(), 1e-7 );
        assertEquals( 0.98465, sum60.getP(), 1e-5 );
        assertEquals( 0.98465, sum60.getMainEffectP( "Factor1" ), 1e-5 );

    }

    /**
     * @throws Exception
     */
    @Test
    public void testSingular() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f
                .read( this.getClass().getResourceAsStream( "/data/lmtest2.dat.txt" ) );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass()
                .getResourceAsStream( "/data/lmtest2.des.txt" ) );
        DesignMatrix d = new DesignMatrix( sampleInfo, true );

        assertEquals( 9, d.getMatrix().columns() );

        LeastSquaresFit fit = new LeastSquaresFit( d, testMatrix );

        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );
        assertEquals( 81, sums.size() );

        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult a = lms.getAnova();
            assertNotNull( a );
        }

        LinearModelSummary s = sums.get( "A01157cds_s_at" );

        assertNotNull( s.getCoefficients() );

        assertEquals( 7.3740000, s.getCoefficients().get( 0, 0 ), 0.001 );
        // assertEquals( 0.1147667, s.getCoefficients().get( 1, 3 ), 0.001 );
        assertEquals( 6, s.getResidualDof().intValue() );
        assertEquals( 7, s.getNumeratorDof().intValue() );
        assertEquals( 0.8634, s.getF(), 0.01 );
        assertEquals( 0.5795, s.getP(), 0.001 );

    }

    /**
     * Originally causes failures during summarization step. There are two pivoted columns.
     * 
     * @throws Exception
     */
    @Test
    public void testSingular2() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/1027_GSE6189.data.test.txt" ) );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass().getResourceAsStream(
                "/data/1027_GSE6189_expdesign.data.txt" ) );
        DesignMatrix d = new DesignMatrix( sampleInfo, true );

        LeastSquaresFit fit = new LeastSquaresFit( d, testMatrix.getRowRange( 0, 0 ) );

        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );
        assertEquals( 1, sums.size() );
        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult a = lms.getAnova();
            assertNotNull( a );
        }
        LinearModelSummary s = sums.get( "1367452_at" );
        assertNotNull( s );
        assertNotNull( s.getCoefficients() );

        // log.info( s.getCoefficients() );

        // log.info( s.getAnova() );

        // our model matrix ends up with different coefficients than R which are
        // double[] rcoef = new double[] { 14.96355, 0.14421, -0.11525, 0.24257, Double.NaN, 0.04093, 0.06660,
        // Double.NaN };

        // here are the coefs we get in R if we use the exact model matrix we get drom our DesignMatrix
        double[] coef = new double[] { 15.10776244, -0.01689300, 0.09835841, -0.20163964, Double.NaN, -0.04092962,
                Double.NaN, 0.06660370 };

        for ( int i = 0; i < s.getCoefficients().rows(); i++ ) {
            assertEquals( coef[i], s.getCoefficients().get( i, 0 ), 0.0001 );
        }

    }

    @Test
    public void testThreeWaySingular() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/1064_GSE7863.data.test.txt" ) );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass().getResourceAsStream(
                "/data/1064_GSE7863_expdesign.data.test.txt" ) );
        DesignMatrix d = new DesignMatrix( sampleInfo, true );

        assertEquals( 5, d.getMatrix().columns() );

        LeastSquaresFit fit = new LeastSquaresFit( d, testMatrix );

        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );
        assertEquals( 416, sums.size() );

        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult a = lms.getAnova();
            assertNotNull( a );
        }

        LinearModelSummary s = sums.get( "1415696_at" );

        assertNotNull( s.getCoefficients() );
        assertEquals( 0.000794, s.getCoefficients().get( 2, 3 ), 0.001 );
        assertEquals( 11, s.getResidualDof().intValue() );
        assertEquals( 4, s.getNumeratorDof().intValue() );
        assertEquals( 24.38, s.getF(), 0.01 );
        assertEquals( 2.025e-05, s.getP(), 0.001 );
        GenericAnovaResult anova = s.getAnova();
        assertEquals( 29.0386, anova.getMainEffectF( "Treatment" ), 0.0001 );

        s = sums.get( "1415837_at" );
        assertNotNull( s.getCoefficients() );
        assertEquals( 11, s.getResidualDof().intValue() );
        assertEquals( 4, s.getNumeratorDof().intValue() );
        assertEquals( 22.72, s.getF(), 0.01 );
        assertEquals( 2.847e-05, s.getP(), 0.001 );
        anova = s.getAnova();
        assertEquals( 6.5977, anova.getMainEffectF( "Treatment" ), 0.0001 );

        s = sums.get( "1416179_a_at" );
        assertNotNull( s.getCoefficients() );
        assertEquals( 11, s.getResidualDof().intValue() );
        assertEquals( 4, s.getNumeratorDof().intValue() );
        assertEquals( 25.14, s.getF(), 0.01 );
        assertEquals( 1.743e-05, s.getP(), 0.001 );
        anova = s.getAnova();
        assertEquals( 38.411, anova.getMainEffectF( "Treatment" ), 0.001 );

        s = sums.get( "1456759_at" );
        assertNotNull( s.getCoefficients() );
        assertEquals( 11, s.getResidualDof().intValue() );
        assertEquals( 4, s.getNumeratorDof().intValue() );
        assertEquals( 7.903, s.getF(), 0.01 );
        assertEquals( 0.002960, s.getP(), 0.001 );
        anova = s.getAnova();
        assertEquals( 10.3792, anova.getMainEffectF( "Treatment" ), 0.001 );
        assertEquals( 2.6253, anova.getMainEffectF( "Genotype" ), 0.001 );
    }

    /**
     * Sanity check.
     * 
     * @throws Exception
     */
    @Test
    public void testTwoWayAnovaUnfittable() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass()
                .getResourceAsStream( "/data/lmtest10.dat.txt" ) );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass().getResourceAsStream(
                "/data/lmtest10.des.txt" ) );

        DesignMatrix d = new DesignMatrix( sampleInfo, true );

        LeastSquaresFit fit = new LeastSquaresFit( d, testMatrix );
        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );
        assertEquals( 1, sums.size() );

        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult a = lms.getAnova();
            assertNotNull( a );
            assertEquals( Double.NaN, a.getMainEffectP( "CellType" ), 0.0001 );
            assertEquals( Double.NaN, a.getMainEffectP( "SamplingTimePoint" ), 0.0001 );
        }

        DoubleMatrix2D coefficients = fit.getCoefficients();
        DoubleMatrix2D residuals = fit.getResiduals();

        assertEquals( 2.238, coefficients.get( 0, 0 ), 0.0001 ); // mean.
        assertEquals( 0.0, coefficients.get( 1, 0 ), 0.0001 );

        for ( int i = 0; i < residuals.rows(); i++ ) {
            assertEquals( 0.0, residuals.get( 0, i ), 0.00001 );
        }
    }

    /**
     * Check for problem reported by TF -- Gemma gives slightly different result. Problem is not at this level.
     * 
     * @throws Exception
     */
    @Test
    public void testTwoWayAnovaWithInteractions() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/GSE8441_expmat_8probes.txt" ) );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass().getResourceAsStream(
                "/data/606_GSE8441_expdesign.data.txt" ) );
        DesignMatrix d = new DesignMatrix( sampleInfo, true );
        d.addInteraction();

        assertEquals( 4, d.getMatrix().columns() );

        assertEquals( 22, testMatrix.columns() );

        LeastSquaresFit fit = new LeastSquaresFit( d, testMatrix );

        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );
        assertEquals( 8, sums.size() );

        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult a = lms.getAnova();
            assertNotNull( a );
        }

        LinearModelSummary s = sums.get( "217757_at" );
        GenericAnovaResult anova = s.getAnova();

        assertNotNull( s.getCoefficients() );
        assertEquals( 0.763, s.getCoefficients().get( 2, 3 ), 0.001 );
        assertEquals( 18, s.getResidualDof().intValue() );
        assertEquals( 3, s.getNumeratorDof().intValue() );
        assertEquals( 0.299, s.getF(), 0.01 );
        assertEquals( 0.8257, s.getP(), 0.001 );

        assertEquals( 0.5876, anova.getMainEffectF( "Treatment" ), 0.0001 );
        assertEquals( 0.5925, anova.getInteractionEffectP(), 0.001 );

        s = sums.get( "202851_at" );
        anova = s.getAnova();
        assertNotNull( s.getCoefficients() );
        assertEquals( 0.787, s.getCoefficients().get( 2, 3 ), 0.001 );
        assertEquals( 18, s.getResidualDof().intValue() );
        assertEquals( 3, s.getNumeratorDof().intValue() );
        assertEquals( 0.1773, s.getF(), 0.01 );
        assertEquals( 0.9104, s.getP(), 0.001 );

        assertEquals( 0.3777, anova.getMainEffectF( "Treatment" ), 0.0001 );
        assertEquals( 0.9956, anova.getInteractionEffectP(), 0.001 );
    }

    /**
     * No missing values; Two-way ANOVA with interaction, PLUS a continuous covariate.
     * 
     * @throws Exception
     */
    @Test
    public void testTwoWayTwoLevelsOneContinousInteractionC() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/example.madata.small.txt" ) );

        ObjectMatrix<String, String, Object> design = new ObjectMatrixImpl<String, String, Object>( 9, 3 );

        design.set( 0, 0, "A" );
        design.set( 1, 0, "A" );
        design.set( 2, 0, "A" );
        design.set( 3, 0, "A" );
        design.set( 4, 0, "B" );
        design.set( 5, 0, "B" );
        design.set( 6, 0, "B" );
        design.set( 7, 0, "B" );
        design.set( 8, 0, "B" );
        design.set( 0, 1, 0.12 );
        design.set( 1, 1, 0.24 );
        design.set( 2, 1, 0.48 );
        design.set( 3, 1, 0.96 );
        design.set( 4, 1, 0.12 );
        design.set( 5, 1, 0.24 );
        design.set( 6, 1, 0.48 );
        design.set( 7, 1, 0.96 );
        design.set( 8, 1, 0.96 );
        design.set( 0, 2, "C" );
        design.set( 1, 2, "C" );
        design.set( 2, 2, "D" );
        design.set( 3, 2, "D" );
        design.set( 4, 2, "C" );
        design.set( 5, 2, "C" );
        design.set( 6, 2, "D" );
        design.set( 7, 2, "D" );
        design.set( 8, 2, "D" );
        design.addColumnName( "Treat" );
        design.addColumnName( "Value" );
        design.addColumnName( "Geno" );

        DesignMatrix designMatrix = new DesignMatrix( design, true );
        designMatrix.addInteraction( "Treat", "Geno" );
        LeastSquaresFit fit = new LeastSquaresFit( designMatrix, testMatrix );

        Map<String, LinearModelSummary> sums = fit.summarizeByKeys( true );

        LinearModelSummary s = sums.get( "1553129_at" );
        assertEquals( 1.791, s.getF(), 0.01 );
        assertEquals( 0.2930, s.getP(), 0.001 );
        assertEquals( 3.71542, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.184, s.getCoefficients().get( 1, 3 ), 0.001 ); // this ordering might change?
        assertEquals( 0.137, s.getCoefficients().get( 4, 3 ), 0.001 ); // this ordering might change?
        GenericAnovaResult a = s.getAnova();
        assertEquals( 0.137, a.getInteractionEffectP(), 0.001 );

        s = sums.get( "232018_at" );
        assertEquals( 0.7167, s.getF(), 0.01 );
        assertEquals( 0.6235, s.getP(), 0.001 );
        assertEquals( 6.8873, s.getCoefficients().get( 0, 0 ), 0.001 );
        assertEquals( 0.587932, s.getCoefficients().get( 1, 3 ), 0.001 );
        a = s.getAnova();
        assertEquals( 0.2904, a.getInteractionEffectP(), 0.001 );

        for ( LinearModelSummary lms : sums.values() ) {
            GenericAnovaResult anova = lms.getAnova();
            assertNotNull( anova );
            Double interactionEffectP = anova.getInteractionEffectP();
            assertNotNull( interactionEffectP );
            assertTrue( !Double.isNaN( interactionEffectP ) );

        }

    }

    @Test
    public void testVectorRegress() {

        DoubleMatrix1D vectorA = new DenseDoubleMatrix1D( new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 } );
        DoubleMatrix1D vectorB = new DenseDoubleMatrix1D( new double[] { 1, 2, 2, 3, 3, 4, 4, 5, 5, 6 } );

        LeastSquaresFit fit = new LeastSquaresFit( vectorA, vectorB );

        DoubleMatrix2D coefficients = fit.getCoefficients();
        DoubleMatrix2D residuals = fit.getResiduals();

        assertEquals( 0.666666, coefficients.get( 0, 0 ), 0.0001 );
        assertEquals( 0.5152, coefficients.get( 1, 0 ), 0.0001 );

        double[] expectedResiduals = new double[] { -0.1818182, 0.3030303, -0.2121212, 0.2727273, -0.2424242,
                0.2424242, -0.2727273, 0.2121212, -0.3030303, 0.1818182 };

        for ( int i = 0; i < expectedResiduals.length; i++ ) {
            assertEquals( expectedResiduals[i], residuals.get( 0, i ), 0.00001 );
        }

    }

    /**
     * @throws Exception
     */
    @Test
    public void testVectorWeightedRegress() throws Exception {

        DoubleMatrix1D vectorA = new DenseDoubleMatrix1D( new double[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 } );
        DoubleMatrix1D vectorB = new DenseDoubleMatrix1D( new double[] { 1, 2, 2, 3, 3, 4, 4, 5, 5, 6 } );
        DoubleMatrix1D w = vectorA.copy().assign( Functions.inv );

        LeastSquaresFit fit = new LeastSquaresFit( vectorA, vectorB, w );

        DoubleMatrix2D coefficients = fit.getCoefficients();
        DoubleMatrix2D residuals = fit.getResiduals();

        assertEquals( 0.60469, coefficients.get( 0, 0 ), 0.0001 );
        assertEquals( 0.52642, coefficients.get( 1, 0 ), 0.0001 );

        double[] expectedResiduals = new double[] { -0.1311097, 0.3424702, -0.1839499, 0.2896301, -0.2367900,
                0.2367900, -0.2896301, 0.1839499, -0.3424702, 0.1311097 };

        for ( int i = 0; i < expectedResiduals.length; i++ ) {
            assertEquals( expectedResiduals[i], residuals.get( 0, i ), 0.00001 );
        }
        double[] expectedFitted = new double[] { 1.13111, 1.65753, 2.18395, 2.71037, 3.23679, 3.76321, 4.28963,
                4.81605, 5.34247, 5.86889 };

        for ( int i = 0; i < expectedFitted.length; i++ ) {
            assertEquals( expectedFitted[i], fit.getFitted().get( 0, i ), 0.00001 );
        }
    }

    /**
     * @throws Exception
     */
    @Test
    public void testVectorWeightedRegressWithMissing() throws Exception {

        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> testMatrix = f.read( this.getClass().getResourceAsStream(
                "/data/example.madata.withmissing.small.txt" ) );
        DoubleMatrix1D libSize = MatrixStats.colSums( testMatrix );
        MatrixStats.convertToLog2Cpm( testMatrix, libSize );

        StringMatrixReader of = new StringMatrixReader();
        StringMatrix<String, String> sampleInfo = of.read( this.getClass().getResourceAsStream(
                "/data/example.metadata.small.txt" ) );
        DesignMatrix designMatrix = new DesignMatrix( sampleInfo );
        DoubleMatrix2D weights = new DenseDoubleMatrix2D( testMatrix.asArray() );
        weights.assign( Functions.inv );

        LeastSquaresFit fit = new LeastSquaresFit( designMatrix, testMatrix, weights );
        assertTrue( fit.isHasMissing() );

        DoubleMatrix2D coefficients = fit.getCoefficients();
        DoubleMatrix2D residuals = fit.getResiduals();

        assertEquals( 15.339801, coefficients.get( 0, 0 ), 0.0001 );
        assertEquals( -0.024058, coefficients.get( 1, 1 ), 0.0001 );
        assertEquals( -0.059586, coefficients.get( 2, 18 ), 0.0001 );

        assertEquals( -0.073732, residuals.get( 0, 0 ), 0.0001 );
        assertEquals( -0.064656, residuals.get( 1, 1 ), 0.0001 );
        assertEquals( -0.085214, residuals.get( 18, 8 ), 0.0001 );
        assertTrue( Double.isNaN( residuals.get( 4, 2 ) ) );

    }
}