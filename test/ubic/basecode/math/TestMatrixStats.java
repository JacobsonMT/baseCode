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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.dataStructure.matrix.DoubleMatrixFactory;
import ubic.basecode.datafilter.AbstractTestFilter;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.basecode.util.RegressionTesting;
import cern.colt.function.DoubleProcedure;

/**
 * @author pavlidis
 * @version $Id$
 */
public class TestMatrixStats {

    private DoubleMatrix<String, String> testdata = null;
    private DoubleMatrix<String, String> smallT = null;

    double[][] testrdm = { { 1, 2, 3, 4 }, { 11, 12, 13, 14 }, { 21, Double.NaN, 23, 24 } };

    @Test
    public final void testCorrelationMatrix() throws Exception {
        DoubleMatrix<String, String> actualReturn = MatrixStats.correlationMatrix( testdata );
        DoubleMatrixReader f = new DoubleMatrixReader();
        DoubleMatrix<String, String> expectedReturn = f.read( AbstractTestFilter.class
                .getResourceAsStream( "/data/correlation-matrix-testoutput.txt" ) );

        assertEquals( true, RegressionTesting.closeEnough( expectedReturn, actualReturn, 0.001 ) );
    }

    @Test
    public final void testMax() throws Exception {
        double expectedReturn = 44625.7;
        double actualReturn = MatrixStats.max( testdata );
        assertEquals( "return value", expectedReturn, actualReturn, 0.01 );
    }

    @Test
    public final void testMin() throws Exception {
        double expectedReturn = -965.3;
        double actualReturn = MatrixStats.min( testdata );
        assertEquals( "return value", expectedReturn, actualReturn, 0.01 );
    }

    @Test
    public final void testStandardize() throws Exception {
        DoubleMatrix<String, String> standardize = MatrixStats.standardize( testdata );
        assertEquals( 30, standardize.rows() );
        assertEquals( -0.3972279, standardize.get( 3, 4 ), 0.0001 );
        assertEquals( -0.7385692, standardize.get( 13, 5 ), 0.0001 );

        MatrixRowStats.means( standardize ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                assertEquals( 0.0, element, 0.001 );
                return true;
            }
        } );

        MatrixRowStats.sampleStandardDeviations( standardize ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                assertEquals( 1.0, element, 0.001 );
                return true;
            }
        } );
    }

    @Test
    public final void testDoubleStandardize() throws Exception {
        DoubleMatrix<String, String> standardize = MatrixStats.doubleStandardize( testdata );
        assertEquals( 30, standardize.rows() );
        assertEquals( -0.472486, standardize.get( 3, 4 ), 0.01 );
        assertEquals( -0.4903036, standardize.get( 26, 2 ), 0.01 );

        MatrixRowStats.means( standardize ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                assertEquals( 0.0, element, 0.001 );
                return true;
            }
        } );

        MatrixRowStats.means( standardize.transpose() ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                assertEquals( 0.0, element, 0.002 );
                return true;
            }
        } );

        MatrixRowStats.sampleStandardDeviations( standardize ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                assertEquals( 1.0, element, 0.001 );
                return true;
            }
        } );

        MatrixRowStats.sampleStandardDeviations( standardize.transpose() ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                assertEquals( 1.0, element, 0.05 );
                return true;
            }
        } );
    }

    @Test
    public final void testNan() throws Exception {
        boolean[][] actual = MatrixStats.nanStatusMatrix( testrdm );
        assertFalse( actual[0][0] );
        assertFalse( actual[0][3] );
        assertTrue( actual[2][1] );
    }

    @Test
    public final void testRbfNormalize() throws Exception {
        double[][] actual = { { 0.001, 0.2, 0.13, 0.4 }, { 0.11, 0.12, 0.00013, 0.14 }, { 0.21, 0.0001, 0.99, 0.24 } };
        DenseDoubleMatrix<String, String> av = new DenseDoubleMatrix<String, String>( actual );
        MatrixStats.rbfNormalize( av, 1 );
        double[][] expected = { { 0.2968, 0.2432, 0.2609, 0.1991 }, { 0.2453, 0.2429, 0.2738, 0.2381 },
                { 0.273, 0.3368, 0.1252, 0.265 } };
        assertEquals( true, RegressionTesting
                .closeEnough( new DenseDoubleMatrix<String, String>( expected ), av, 0.001 ) );
        for ( int i = 0; i < 3; i++ ) {
            assertEquals( 1.0, av.viewRow( i ).zSum(), 0.0001 );
        }
    }

    @Test
    public final void testSelfSquare() throws Exception {
        double[][] actual = MatrixStats.selfSquaredMatrix( testrdm );
        assertEquals( 1, actual[0][0], 0.000001 );
        assertEquals( 16, actual[0][3], 0.00001 );
    }

    /*
     * @see TestCase#setUp()
     */
    @Before
    public void setUp() throws Exception {
        DoubleMatrixReader f = new DoubleMatrixReader();

        testdata = f.read( AbstractTestFilter.class.getResourceAsStream( "/data/testdata.txt" ) );

        smallT = DoubleMatrixFactory.dense( testrdm );
        smallT.setRowNames( java.util.Arrays.asList( new String[] { "a", "b", "c" } ) );
        smallT.setColumnNames( java.util.Arrays.asList( new String[] { "w", "x", "y", "z" } ) );
    }

}
