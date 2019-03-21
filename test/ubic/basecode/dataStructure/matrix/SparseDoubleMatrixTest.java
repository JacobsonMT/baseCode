/*
 * The baseCode project
 * 
 * Copyright (c) 2008-2019 University of British Columbia
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
package ubic.basecode.dataStructure.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import ubic.basecode.io.reader.SparseDoubleMatrixReader;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * @author Paul
 * 
 */
public class SparseDoubleMatrixTest {

    double[][] testArray = { { 1, 2, 3, 4 }, { 11, 12, 13, 14 }, { 21, Double.NaN, 23, 24 } };
    SparseDoubleMatrix<String, String> testM;
    DoubleMatrix<String, String> testMatrix;

    @Before
    public void setUp() throws Exception {

        SparseDoubleMatrixReader f = new SparseDoubleMatrixReader();
        testMatrix = f.read( this.getClass().getResourceAsStream( "/data/adjacencylist-testmatrix.txt" ) );
        assert testMatrix instanceof SparseDoubleMatrix<?, ?> : "Got a " + testMatrix.getClass().getName();
        testM = new SparseDoubleMatrix<String, String>( testArray );
        testM.setRowNames( java.util.Arrays.asList( new String[] { "a", "b", "c" } ) );
        testM.setColumnNames( java.util.Arrays.asList( new String[] { "w", "x", "y", "z" } ) );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#columns()}.
     */
    @Test
    public void testColumns() {
        assertEquals( 4, testM.columns() );
    }

    @Test
    public void testCopy() {
        DoubleMatrix<String, String> actual = testM.copy();
        for ( int i = 0; i < testArray.length; i++ ) {
            assertEquals( testM.getRowName( i ), actual.getRowName( i ) );
            int len = testArray[i].length;
            for ( int j = 0; j < len; j++ ) {
                assertEquals( testM.getColName( j ), actual.getColName( j ) );
                assertEquals( testArray[i][j], actual.get( i, j ), 0.0001 );
            }
        }

    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#get(int, int)}.
     */
    @Test
    public void testGet() {
        assertEquals( 24.0, testM.get( 2, 3 ), 0.0001 );
        assertEquals( 1.0, testM.get( 0, 0 ), 0.0001 );
        assertEquals( 13.0, testM.get( 1, 2 ), 0.0001 );
        assertEquals( Double.NaN, testM.get( 2, 1 ), 0.0001 );
        assertEquals( 23.0, testM.get( 2, 2 ), 0.0001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getByKeys(Object, Object)}.
     */
    @Test
    public void testGetByKeys() {
        assertEquals( 24.0, testM.getByKeys( "c", "z" ), 0.0001 );
        assertEquals( 1.0, testM.getByKeys( "a", "w" ), 0.0001 );
        assertEquals( 13.0, testM.getByKeys( "b", "y" ), 0.0001 );
        assertEquals( Double.NaN, testM.getByKeys( "c", "x" ), 0.0001 );
        assertEquals( 23.0, testM.getByKeys( "c", "y" ), 0.0001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getColByName(java.lang.Object)}.
     */
    @Test
    public void testGetColByName() {
        double[] actual = testM.getColumnByName( "x" );
        assertEquals( 2, actual[0], 0.000001 );
        assertEquals( 12, actual[1], 0.000001 );
        assertEquals( Double.NaN, actual[2], 0.0001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getColObj(int)}.
     */
    @Test
    public void testGetColObj() {
        Double[] actual = testM.getColObj( 0 );
        assertEquals( 3, actual.length );
        assertEquals( 1.0, actual[0], 0.000001 );
        assertEquals( 11.0, actual[1], 0.000001 );
        assertEquals( 21.0, actual[2], 0.000001 );
    }

    @Test
    public void testGetColRange() {
        DoubleMatrix<String, String> range = testMatrix.getColRange( 1, 2 );
        assertEquals( 2, range.columns() );
        assertEquals( 3, range.rows() );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getColumn(int)}.
     */
    @Test
    public void testGetColumn() {
        double[] actual = testM.getColumn( 1 );
        assertEquals( 3, actual.length );
        assertEquals( 2, actual[0], 0.000001 );
        assertEquals( 12.0, actual[1], 0.000001 );
        assertEquals( Double.NaN, actual[2], 0.00001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getObject(int, int)}.
     */
    @Test
    public void testGetObject() {
        assertEquals( 4.0, testM.getObject( 0, 3 ), 0.000001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getRow(int)}.
     */
    @Test
    public void testGetRow() {
        double[] actual = testM.getRow( 1 );
        assertEquals( 4, actual.length );
        assertEquals( 11.0, actual[0], 0.000001 );
        assertEquals( 12.0, actual[1], 0.000001 );
        assertEquals( 13.0, actual[2], 0.000001 );
        assertEquals( 14.0, actual[3], 0.000001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getRowArrayList(int)}.
     */
    @Test
    public void testGetRowArrayList() {
        DoubleArrayList actual = testM.getRowArrayList( 2 );
        assertEquals( 4, actual.size() );
        assertEquals( 21.0, actual.get( 0 ), 0.000001 );
        assertEquals( Double.NaN, actual.get( 1 ), 0.0001 );
        assertEquals( 23.0, actual.get( 2 ), 0.000001 );
        assertEquals( 24.0, actual.get( 3 ), 0.000001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getRowByName(java.lang.Object)}.
     */
    @Test
    public void testGetRowByName() {
        double[] actual = testM.getRowByName( "b" );
        assertEquals( 4, actual.length );
        assertEquals( 11.0, actual[0], 0.000001 );
        assertEquals( 12.0, actual[1], 0.000001 );
        assertEquals( 13.0, actual[2], 0.000001 );
        assertEquals( 14.0, actual[3], 0.000001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#getRowObj(int)}.
     */
    @Test
    public void testGetRowObj() {
        Double[] actual = testM.getRowObj( 0 );
        assertEquals( 4, actual.length );
        assertEquals( 1.0, actual[0], 0.000001 );
        assertEquals( 2.0, actual[1], 0.000001 );
        assertEquals( 3.0, actual[2], 0.000001 );
        assertEquals( 4.0, actual[3], 0.000001 );
    }

    @Test
    public void testGetRowRange() {
        DoubleMatrix<String, String> rowRange = testMatrix.getRowRange( 1, 2 );
        assertEquals( 3, rowRange.columns() );
        assertEquals( 2, rowRange.rows() );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#isMissing(int, int)}.
     */
    @Test
    public void testIsMissing() {
        assertFalse( testM.isMissing( 2, 2 ) );
        assertTrue( testM.isMissing( 2, 1 ) );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#rows()}.
     */
    @Test
    public void testRows() {
        assertEquals( 3, testM.rows() );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#set(int, int, double)}.
     */
    @Test
    public void testSet() {
        testM.set( 2, 2, 666.0 );
        double[] actual = testM.getRow( 2 );
        assertEquals( 666.0, actual[2], 0.00001 );
    }

    @Test
    public void testSize() {
        assertEquals( 12, testM.size() );
    }

    @Test
    public void testSubsetCols() {
        DoubleMatrix<String, String> subset = testMatrix.subsetColumns( Arrays.asList( new String[] { "2", "1" } ) );
        assertEquals( 3, subset.rows() );
        assertEquals( 2, subset.columns() );
    }

    @Test
    public void testSubsetRows() {
        DoubleMatrix<String, String> subset = testMatrix.subsetRows( Arrays.asList( new String[] { "1", "3" } ) );
        assertEquals( 2, subset.rows() );
        assertEquals( 3, subset.columns() );
    }

    @Test
    public void testToArray() {
        double[][] actual = testM.asArray();
        for ( int i = 0; i < testArray.length; i++ ) {
            int len = testArray[i].length;
            for ( int j = 0; j < len; j++ ) {
                assertEquals( testArray[i][j], actual[i][j], 0.00001 );
            }
        }

    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#toString()}.
     */
    @Test
    public void testToString() {
        String actual = testM.toString();
        assertEquals(
                "# 3x4 matrix\nlabel\tw\tx\ty\tz\na\t1.000\t2.000\t3.000\t4.000\nb\t11.00\t12.00\t13.00\t14.00\nc\t21.00\t\t23.00\t24.00\n",
                actual );
    }

    @Test
    public void testViewColumn() {
        DoubleMatrix1D actual = testM.viewColumn( 0 );
        assertEquals( 3, actual.size() );
        assertEquals( 1.0, actual.get( 0 ), 0.000001 );
        assertEquals( 11.0, actual.get( 1 ), 0.000001 );
        assertEquals( 21.0, actual.get( 2 ), 0.000001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.SparseDoubleMatrix#viewRow(int)}.
     */
    @Test
    public void testViewRow() {
        DoubleMatrix1D actual = testM.viewRow( 0 );
        assertEquals( 4, actual.size() );
        assertEquals( 1.0, actual.get( 0 ), 0.000001 );
        assertEquals( 2.0, actual.get( 1 ), 0.000001 );
        assertEquals( 3.0, actual.get( 2 ), 0.000001 );
        assertEquals( 4.0, actual.get( 3 ), 0.000001 );

    }

}
