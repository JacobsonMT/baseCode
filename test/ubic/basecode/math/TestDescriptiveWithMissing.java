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
package ubic.basecode.math;

import junit.framework.TestCase;
import ubic.basecode.util.RegressionTesting;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * @author Paul Pavlidis
 * @version $Id$
 */
public class TestDescriptiveWithMissing extends TestCase {

    private DoubleArrayList data1missing;
    private DoubleArrayList data1Nomissing;
    private DoubleArrayList data2missing;
    private DoubleArrayList data2Nomissing;
    private DoubleArrayList data3shortmissing;
    private DoubleArrayList data3shortNomissing;

    private DoubleArrayList datacortest1Nomissing;
    private DoubleArrayList datacortest2Nomissing;
    private DoubleArrayList datacortest1missing;
    private DoubleArrayList datacortest2missing;

    protected void setUp() throws Exception {
        super.setUp();
        data1missing = new DoubleArrayList( new double[] { 1.0, Double.NaN, 3.0, 4.0, 5.0, 6.0, Double.NaN } );
        data2missing = new DoubleArrayList( new double[] { Double.NaN, Double.NaN, 3.0, Double.NaN, 3.5, 4.0,
                Double.NaN } );
        data3shortmissing = new DoubleArrayList( new double[] { Double.NaN, Double.NaN, 3.0, Double.NaN } );

        /* versions of the above, but without the NaNs */
        data1Nomissing = new DoubleArrayList( new double[] { 1.0, 3.0, 4.0, 5.0, 6.0 } );
        data2Nomissing = new DoubleArrayList( new double[] { 3.0, 3.5, 4.0 } );
        data3shortNomissing = new DoubleArrayList( new double[] { 3.0 } );

        datacortest1missing = new DoubleArrayList( new double[] { 3.0, Double.NaN, 5.0, 6.0, 3.2 } );
        datacortest2missing = new DoubleArrayList( new double[] { 3.0, 8.0, 3.5, 4.0, Double.NaN } );
        datacortest1Nomissing = new DoubleArrayList( new double[] { 3.0, 5.0, 6.0 } );
        datacortest2Nomissing = new DoubleArrayList( new double[] { 3.0, 3.5, 4.0 } );

    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testCorrelationA() {
        double s1 = Math.sqrt( Descriptive.sampleVariance( datacortest1Nomissing.size(), Descriptive
                .sum( datacortest1Nomissing ), Descriptive.sumOfSquares( datacortest1Nomissing ) ) );
        double s2 = Math.sqrt( Descriptive.sampleVariance( datacortest2Nomissing.size(), Descriptive
                .sum( datacortest2Nomissing ), Descriptive.sumOfSquares( datacortest2Nomissing ) ) );

        double expectedReturn = Descriptive.correlation( datacortest1Nomissing, s1, datacortest2Nomissing, s2 );
        double actualReturn = DescriptiveWithMissing.correlation( data1missing, data2missing );
        assertEquals( "return value", expectedReturn, actualReturn, 1e-15 );
    }

    public void testCorrelationB() {
        double s1 = Math.sqrt( Descriptive.sampleVariance( datacortest1Nomissing.size(), Descriptive
                .sum( datacortest1Nomissing ), Descriptive.sumOfSquares( datacortest1Nomissing ) ) );
        double s2 = Math.sqrt( Descriptive.sampleVariance( datacortest2Nomissing.size(), Descriptive
                .sum( datacortest2Nomissing ), Descriptive.sumOfSquares( datacortest2Nomissing ) ) );

        double s1m = 0;
        double s2m = 0;
        double expectedReturn = Descriptive.correlation( datacortest1Nomissing, s1, datacortest2Nomissing, s2 );
        double actualReturn = DescriptiveWithMissing.correlation( data1missing, s1m, data2missing, s2m );

        assertEquals( "return value", expectedReturn, actualReturn, 1e-15 );
    }

    public void testCovariance() {
        double expectedReturn = Descriptive.covariance( datacortest1Nomissing, datacortest2Nomissing );
        double actualReturn = DescriptiveWithMissing.covariance( data1missing, data2missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testDurbinWatson() {
        double expectedReturn = Descriptive.durbinWatson( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.durbinWatson( data1missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testDurbinWatsonTwo() {
        double expectedReturn = Descriptive.durbinWatson( data2Nomissing );
        double actualReturn = DescriptiveWithMissing.durbinWatson( data2missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testWeightedMean() {
        double expected = Descriptive.weightedMean( datacortest1Nomissing, datacortest2Nomissing );
        double actual = DescriptiveWithMissing.weightedMean( datacortest1missing, datacortest2missing );
        assertEquals( "return value", expected, actual, Double.MIN_VALUE );
    }

    public void testDurbinWatsonShort() {

        try {
            double expectedReturn = Descriptive.durbinWatson( data3shortNomissing );
            double actualReturn = DescriptiveWithMissing.durbinWatson( data3shortmissing );
            assertEquals( "Short array failure.", expectedReturn, actualReturn, Double.MIN_VALUE );
            fail( "Should have thrown an IllegalArgumentException" );
        } catch ( IllegalArgumentException e ) {
            return;
        } catch ( Exception e ) {
            fail( "Threw wrong exception: " + e );
        }
    }

    public void testGeometricMean() {
        double expectedReturn = Descriptive.geometricMean( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.geometricMean( data1missing );
        assertEquals( "Excercises sumOfLogarithms too; return value", expectedReturn, actualReturn, Double.MIN_VALUE );

    }

    public void testMean() {
        double expectedReturn = Descriptive.mean( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.mean( data1missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );

    }

    public void testMin() {
        double expectedReturn = Descriptive.min( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.min( data1missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testMax() {
        double expectedReturn = Descriptive.max( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.max( data1missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testMedian() {
        data1missing.sort();
        data1Nomissing.sort();
        double expectedReturn = Descriptive.median( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.median( data1missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testProduct() {
        double expectedReturn = Descriptive.product( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.product( data1missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testSampleKurtosis() {
        double expectedReturn = Descriptive.sampleKurtosis( data1Nomissing, Descriptive.mean( data1Nomissing ),
                Descriptive.sampleVariance( data1Nomissing, Descriptive.mean( data1Nomissing ) ) );
        double actualReturn = DescriptiveWithMissing.sampleKurtosis( data1missing, DescriptiveWithMissing
                .mean( data1missing ), DescriptiveWithMissing.sampleVariance( data1missing, DescriptiveWithMissing
                .mean( data1missing ) ) );
        assertEquals( "Exercises sampleVariance, mean as well; return value", expectedReturn, actualReturn,
                Double.MIN_VALUE );
    }

    public void testQuantile() {
        data1missing.sort();
        data1Nomissing.sort();
        double expectedReturn = Descriptive.quantile( data1Nomissing, 0.10 );
        double actualReturn = DescriptiveWithMissing.quantile( data1missing, 0.10 );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );

    }

    public void testSum() {
        double expectedReturn = Descriptive.sum( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.sum( data1missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testSumOfSquares() {
        double expectedReturn = Descriptive.sumOfSquares( data1Nomissing );
        double actualReturn = DescriptiveWithMissing.sumOfSquares( data1missing );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );
    }

    public void testSampleVariance() {
        double expectedReturn = Descriptive.sampleVariance( data1Nomissing, Descriptive.mean( data1Nomissing ) );
        double actualReturn = DescriptiveWithMissing.sampleVariance( data1missing, DescriptiveWithMissing
                .mean( data1missing ) );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );

    }

    public void testStandardize() {

        // we use this because Descriptive.standardize does not do exactly the
        // same thing - there is a correction applied.
        DoubleArrayList expectedReturn = new DoubleArrayList( new double[] { -1.4556506857481, Double.NaN,
                -0.415900195928029, 0.103975048982007, 0.623850293892044, 1.14372553880208, Double.NaN } );

        DescriptiveWithMissing.standardize( data1missing );
        assertEquals( true, RegressionTesting.closeEnough( data1missing, expectedReturn, 0.0001 ) );
    }

    public void testTrimmedMean() {
        data1Nomissing.sort();
        data1missing.sort();
        double expectedReturn = Descriptive.trimmedMean( data1Nomissing, Descriptive.mean( data1Nomissing ), 1, 1 );
        double actualReturn = DescriptiveWithMissing.trimmedMean( data1missing, DescriptiveWithMissing
                .mean( data1missing ), 1, 1 );
        assertEquals( "return value", expectedReturn, actualReturn, Double.MIN_VALUE );

    }

    public void testVariance() {
        double expectedReturn = Descriptive.variance( data1Nomissing.size(), Descriptive.sum( data1Nomissing ),
                Descriptive.sumOfSquares( data1Nomissing ) );
        double actualReturn = DescriptiveWithMissing.variance( DescriptiveWithMissing
                .sizeWithoutMissingValues( data1missing ), DescriptiveWithMissing.sum( data1missing ),
                DescriptiveWithMissing.sumOfSquares( data1missing ) );
        assertEquals( "return value", expectedReturn, actualReturn, 0.000001 );
    }

    public void testMeanEff() throws Exception {
        double actualReturn = DescriptiveWithMissing.mean( data1missing, 4 );
        assertEquals( "return value", 4.75, actualReturn, Double.MIN_VALUE );
    }

    public void testMeanAboveQuantile() throws Exception {
        double actualReturn = DescriptiveWithMissing.meanAboveQuantile( 0.75, data1Nomissing );
        assertEquals( "return value", 6, actualReturn, Double.MIN_VALUE );
    }

}