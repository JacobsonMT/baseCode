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
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Paul
 * @version $Id$
 */
public class IntegerMatrixTest {
    IntegerMatrix<String, String> mat;

    @Before
    public void setup() throws Exception {
        mat = new IntegerMatrix<String, String>( 20, 10 );
        for ( int i = 0; i < 20; i++ ) {
            mat.setRowName( Integer.toString( i ), i );
            for ( int j = 0; j < 10; j++ ) {
                if ( i == 0 ) {
                    mat.setColumnName( Integer.toString( j ), j );
                }
                mat.set( i, j, i * j );
            }
        }
    }

    @Test
    public void testColumns() {
        assertEquals( 10, mat.columns() );
    }

    @Test
    public void testGet() {
        int i = 11, j = 3;
        assertEquals( new Integer( i * j ), mat.get( i, j ) );

    }

    @Test
    public void testGetByKeys() {
        int i = 11, j = 3;
        assertEquals( new Integer( i * j ), mat.getByKeys( Integer.toString( i ), Integer.toString( j ) ) );
    }

    @Test
    public void testGetColObj() {
        Integer[] a = mat.getColObj( 2 );
        assertEquals( 20, a.length );
    }

    @Test
    public void testGetColumn() {
        Integer[] a = mat.getColumn( 4 );
        assertEquals( 20, a.length );
    }

    @Test
    public void testGetEntry() {
        mat.getEntry( 3, 12 );
    }

    @Test
    public void testGetObject() {
        mat.getObject( 2, 4 );
    }

    @Test
    public void testGetRow() {
        Integer[] a = mat.getRow( 9 );
        assertEquals( 10, a.length );
    }

    @Test
    public void testGetRowObj() {
        Integer[] a = mat.getRowObj( 9 );
        assertEquals( 10, a.length );
    }

    @Test
    public void testIsMissing() {
        assertTrue( !mat.isMissing( 4, 0 ) );
    }

    @Test
    public void testRows() {
        assertEquals( 20, mat.rows() );
    }

    @Test
    public void testSet() {
        mat.set( 4, 2, 95090 );
        assertEquals( new Integer( 95090 ), mat.get( 4, 2 ) );
    }

    @Test
    public void testSetByKeys() {
        mat.setByKeys( Integer.toString( 4 ), Integer.toString( 2 ), 95090 );
        assertEquals( new Integer( 95090 ), mat.get( 4, 2 ) );
    }

    @Test
    public void testSetObj() {
        mat.set( 4, 2, new Integer( 95090 ) );
        assertEquals( new Integer( 95090 ), mat.get( 4, 2 ) );
    }

    @Test
    public void testSize() {
        assertEquals( 10 * 20, mat.size() );
    }

    @Test
    public void testToInteger() {
        mat.toString();
    }

}
