/*
 * The baseCode project
 * 
 * Copyright (c) 2013 University of British Columbia
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
package ubic.basecode.dataStructure.matrix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ubic.basecode.io.reader.DoubleMatrixReader;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * TODO Document Me
 * 
 * @author Paul
 * @version $Id$
 */
public abstract class AbstractDoubleMatrixTest {
    protected DoubleMatrixReader f = new DoubleMatrixReader();
    protected double[][] testArray = { { 1, 2, 3, 4 }, { 11, 12, 13, 14 }, { 21, Double.NaN, 23, 24 } };
    protected DoubleMatrix<String, String> testdata;
    protected DoubleMatrix<String, String> testM;

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.columns()'
     */
    @Test
    public void testColumns() {
        assertEquals( 12, testdata.columns() );
    }

    @Test
    public void testCopy() {
        DoubleMatrix<String, String> actual = testdata.copy();
        double[][] tt = testdata.asArray();

        for ( int i = 0; i < tt.length; i++ ) {
            assertEquals( testdata.getRowName( i ), actual.getRowName( i ) );
            int len = tt[i].length;
            for ( int j = 0; j < len; j++ ) {
                assertEquals( testdata.getColName( j ), actual.getColName( j ) );
                assertEquals( tt[i][j], actual.get( i, j ), 0.00001 );
            }
        }

    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.get(int, int)'
     */
    @Test
    public void testGet() {
        assertEquals( 27873.8, testdata.get( 2, 4 ), 0.0001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#get(int, int)}.
     */
    @Test
    public void testGet2() {
        assertEquals( 24.0, testM.get( 2, 3 ), 0.0001 );
        assertEquals( 1.0, testM.get( 0, 0 ), 0.0001 );
        assertEquals( 13.0, testM.get( 1, 2 ), 0.0001 );
        assertEquals( Double.NaN, testM.get( 2, 1 ), 0.0001 );
        assertEquals( 23.0, testM.get( 2, 2 ), 0.0001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getByKeys(Object, Object)}.
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
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getColByName(java.lang.Object)}.
     */
    @Test
    public void testGetColByName() {
        double[] actual = testM.getColumnByName( "x" );
        assertEquals( 2, actual[0], 0.000001 );
        assertEquals( 12, actual[1], 0.000001 );
        assertEquals( Double.NaN, actual[2], 0.0001 );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColByNameFail() {
        testM.getColumnByName( "nnnn" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetColByNameNull() {
        testM.getColumnByName( null );
    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.getColObj(int)'
     */
    @Test
    public void testGetColObj() {
        Double[] c = testdata.getColObj( 4 );
        assertEquals( 30, c.length );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getColObj(int)}.
     */
    @Test
    public void testGetColObj2() {
        Double[] actual = testM.getColObj( 0 );
        assertEquals( 3, actual.length );
        assertEquals( 1.0, actual[0], 0.000001 );
        assertEquals( 11.0, actual[1], 0.000001 );
        assertEquals( 21.0, actual[2], 0.000001 );
    }

    @Test
    public void testGetColRange() {
        DoubleMatrix<String, String> range = testdata.getColRange( 1, 4 );
        assertEquals( 4, range.columns() );
        assertEquals( 30, range.rows() );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getColumn(int)}.
     */
    @Test
    public void testGetColumn() {
        double[] actual = testM.getColumn( 1 );
        assertEquals( 3, actual.length );
        assertEquals( 2, actual[0], 0.000001 );
        assertEquals( 12.0, actual[1], 0.000001 );
        assertEquals( Double.NaN, actual[2], 0.0001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getObject(int, int)}.
     */
    @Test
    public void testGetObject() {
        assertEquals( 4.0, testM.getObject( 0, 3 ), 0.000001 );
    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.getRow(int)'
     */
    @Test
    public void testGetRow() {
        double[] res = testdata.getRow( 2 );
        assertEquals( 27873.8, res[4], 0.001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getRow(int)}.
     */
    @Test
    public void testGetRow2() {
        double[] actual = testM.getRow( 1 );
        assertEquals( 4, actual.length );
        assertEquals( 11.0, actual[0], 0.000001 );
        assertEquals( 12.0, actual[1], 0.000001 );
        assertEquals( 13.0, actual[2], 0.000001 );
        assertEquals( 14.0, actual[3], 0.000001 );
    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.getRowArrayList(int)'
     */
    public void testGetRowArrayList() {
        DoubleArrayList res = testdata.getRowArrayList( 2 );
        assertEquals( 27873.8, res.get( 4 ), 0.001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getRowArrayList(int)}.
     */
    @Test
    public void testGetRowArrayList2() {
        DoubleArrayList actual = testM.getRowArrayList( 2 );
        assertEquals( 4, actual.size() );
        assertEquals( 21.0, actual.get( 0 ), 0.000001 );
        assertEquals( Double.NaN, actual.get( 1 ), 0.0001 );
        assertEquals( 23.0, actual.get( 2 ), 0.000001 );
        assertEquals( 24.0, actual.get( 3 ), 0.000001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getRowByName(java.lang.Object)}.
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

    @Test(expected = IllegalArgumentException.class)
    public void testGetRowByNameFail() {
        testM.getRowByName( "nnnn" );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetRowByNameNull() {
        testM.getRowByName( null );
    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.getRowObj(int)'
     */
    @Test
    public void testGetRowObj() {
        Double[] rowRange = testdata.getRowObj( 4 );
        assertEquals( 12, rowRange.length );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#getRowObj(int)}.
     */
    @Test
    public void testGetRowObj2() {
        Double[] actual = testM.getRowObj( 0 );
        assertEquals( 4, actual.length );
        assertEquals( 1.0, actual[0], 0.000001 );
        assertEquals( 2.0, actual[1], 0.000001 );
        assertEquals( 3.0, actual[2], 0.000001 );
        assertEquals( 4.0, actual[3], 0.000001 );
    }

    @Test
    public void testGetRowRange() {
        DoubleMatrix<String, String> rowRange = testdata.getRowRange( 1, 4 );
        assertEquals( 12, rowRange.columns() );
        assertEquals( 4, rowRange.rows() );
    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.isMissing(int, int)'
     */
    @Test
    public void testIsMissing() throws Exception {
        assertTrue( !testdata.isMissing( 5, 3 ) );
        DoubleMatrix<String, String> md = f.read( FastRowAccessDoubleMatrixTest.class
                .getResourceAsStream( "/data/testdatamissing.txt" ) );
        assertTrue( md.isMissing( 5, 3 ) );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#isMissing(int, int)}.
     */
    @Test
    public void testIsMissing2() {
        assertFalse( testM.isMissing( 2, 2 ) );
        assertTrue( testM.isMissing( 2, 1 ) );
    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.rows()'
     */
    @Test
    public void testRows() {
        assertTrue( testdata.rows() == 30 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#rows()}.
     */
    @Test
    public void testRows2() {
        assertEquals( 3, testM.rows() );
    }

    @Test
    public void testSet() {
        testdata.set( 1, 2, 3.0 );
        assertEquals( 3.0, testdata.get( 1, 2 ), 0.00001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#set(int, int, double)}.
     */
    @Test
    public void testSet2() {
        testM.set( 2, 2, 666.0 );
        double[] actual = testM.getRow( 2 );
        assertEquals( 666.0, actual[2], 0.00001 );
    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.set(int, int, double)'
     */
    public void testSetIntIntDouble() {
        testdata.set( 1, 2, 3.0 );
        assertEquals( 3.0, testdata.get( 1, 2 ), 0.00001 );
    }

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.set(int, int, Object)'
     */
    @Test
    public void testSetIntIntObject() {
        testdata.set( 1, 2, new Double( 3.0 ) );
        assertEquals( 3.0, testdata.get( 1, 2 ), 0.00001 );
    }

    @Test
    public void testSize() {
        assertEquals( 12, testM.size() );
    }

    @Test
    public void testSortByColumnAbsoluteValue() {
        for ( int i = 0; i < testdata.columns(); i++ ) {
            List<String> sortByColumnAbsoluteValues = testdata.sortByColumnAbsoluteValues( i, false );
            Double last = 0.0;
            assertTrue( !sortByColumnAbsoluteValues.isEmpty() );
            for ( String string : sortByColumnAbsoluteValues ) {
                Double d = testdata.getRowByName( string )[i];
                assertNotNull( d );
                assertTrue( "at column " + i + " got " + d + " and prev " + last, Math.abs( d ) >= Math.abs( last ) );

                last = d;
                assertNotNull( last );
            }
        }

        for ( int i = 0; i < testdata.columns(); i++ ) {
            double last = Double.MAX_VALUE;
            List<String> sortByColumnAbsoluteValues = testdata.sortByColumnAbsoluteValues( i, true );
            assertTrue( !sortByColumnAbsoluteValues.isEmpty() );

            for ( String string : sortByColumnAbsoluteValues ) {
                double d = testdata.getRowByName( string )[i];

                assertTrue( "at column " + i + " got " + d + " and prev " + last, Math.abs( d ) <= Math.abs( last ) );

                last = d;
            }
        }
    }

    @Test
    public void testSubsetColumns() {
        List<String> c = testdata.getColNames();
        List<String> subList = c.subList( 1, 3 );
        DoubleMatrix<String, String> s = testdata.subsetColumns( subList );
        assertEquals( 2, s.columns() );
        assertEquals( c.get( 1 ), s.getColNames().get( 0 ) );
    }

    @Test
    public void testSubsetRows() {
        List<String> rowNames = testdata.getRowNames();
        List<String> subList = rowNames.subList( 1, 3 );
        DoubleMatrix<String, String> subsetRows = testdata.subsetRows( subList );
        assertEquals( 2, subsetRows.rows() );
    }

    @Test
    public void testToArray() {
        double[][] actual = testM.asArray();
        for ( int i = 0; i < testArray.length; i++ ) {
            int len = testArray[i].length;
            for ( int j = 0; j < len; j++ ) {
                assertEquals( testArray[i][j], actual[i][j], 0.0001 );
            }
        }

    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#toString()}.
     */
    @Test
    public void testToString() {
        String actual = testM.toString();
        assertEquals(
                "# 3x4 matrix: showing up to 100 rows\nlabel\tw\tx\ty\tz\na\t1.000\t2.000\t3.000\t4.000\nb\t11.00\t12.00\t13.00\t14.00\nc\t21.00\t\t23.00\t24.00\n",
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

    /*
     * Test method for 'basecode.dataStructure.matrix.FastRowAccessDoubleMatrix2DNamed.viewRow(int)'
     */
    @Test
    public void testViewRow() {
        DoubleMatrix1D res = testdata.viewRow( 1 );
        assertEquals( 242.1, res.get( 2 ), 0.001 );
    }

    /**
     * Test method for {@link ubic.basecode.dataStructure.matrix.DenseDoubleMatrix#viewRow(int)}.
     */
    @Test
    public void testViewRow2() {
        DoubleMatrix1D actual = testM.viewRow( 0 );
        assertEquals( 4, actual.size() );
        assertEquals( 1.0, actual.get( 0 ), 0.000001 );
        assertEquals( 2.0, actual.get( 1 ), 0.000001 );
        assertEquals( 3.0, actual.get( 2 ), 0.000001 );
        assertEquals( 4.0, actual.get( 3 ), 0.000001 );

    }

}
