/*
 * The baseCode project
 * 
 * Copyright (c) 2008 University of British Columbia
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

import org.junit.Before;

/**
 * T
 * 
 * @author Paul
 * @version $Id$
 */
public class CompressedSparseDoubleMatrixTest extends AbstractDoubleMatrixTest {

    @Before
    public void setUp() throws Exception {
        DoubleMatrix<String, String> tmp = f.read( CompressedSparseDoubleMatrixTest.class
                .getResourceAsStream( "/data/testdata.txt" ) );
        testdata = new CompressedSparseDoubleMatrix<String, String>( tmp.asArray() );
        testdata.setRowNames( tmp.getRowNames() );
        testdata.setColumnNames( tmp.getColNames() );

        testM = new CompressedSparseDoubleMatrix<String, String>( testArray );
        testM.setRowNames( java.util.Arrays.asList( new String[] { "a", "b", "c" } ) );
        testM.setColumnNames( java.util.Arrays.asList( new String[] { "w", "x", "y", "z" } ) );
    }

}
