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
package ubic.basecode.io.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.dataStructure.matrix.SparseDoubleMatrix;
import ubic.basecode.math.Constants;

/**
 * @author pavlidis
 * 
 */
public class TestSparseDoubleMatrixReader {
    InputStream is = null;
    InputStream isa = null;
    DoubleMatrix<String, String> matrix = null;
    SparseDoubleMatrixReader reader = null;

    @Before
    public void setUp() throws Exception {
        reader = new SparseDoubleMatrixReader();
        is = TestSparseDoubleMatrixReader.class.getResourceAsStream( "/data/JW-testmatrix.txt" );
        isa = TestSparseDoubleMatrixReader.class.getResourceAsStream( "/data/adjacencylist-testmatrix.txt" );
    }

    @Test
    public void testReadJW() throws Exception {
        matrix = reader.readJW( is );
        assertEquals( 3, matrix.rows() );
        assertEquals( 3, matrix.columns() );
        assertEquals( 0.1, matrix.get( 1, 1 ), Constants.SMALL );
        assertEquals( 0.3, matrix.get( 2, 0 ), Constants.SMALL );
        assertEquals( 0.0, matrix.get( 0, 2 ), Constants.SMALL );
    }

    /*
     * Class under test for NamedMatrix read(String)
     */
    @Test
    public void testReadStream() throws Exception {
        matrix = reader.read( isa );
        assertTrue( "Got a " + matrix.getClass().getName(), matrix instanceof SparseDoubleMatrix<?, ?> );
        assertEquals( 3, matrix.rows() );
        assertEquals( 3, matrix.columns() );
        assertEquals( 0.1, matrix.get( 1, 1 ), Constants.SMALL );
        assertEquals( 0.3, matrix.get( 2, 0 ), Constants.SMALL );
        assertEquals( 0.3, matrix.get( 0, 2 ), Constants.SMALL );
        assertEquals( 0.8, matrix.get( 2, 2 ), Constants.SMALL );
    }

}
