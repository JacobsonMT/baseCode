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
package ubic.basecode.io.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;
import ubic.basecode.dataStructure.matrix.DoubleMatrixNamed;

/**
 * @author pavlidis
 * @version $Id$
 */
public class TestDoubleMatrixReader extends TestCase {

    DoubleMatrixNamed matrix = null;
    InputStream is = null;
    DoubleMatrixReader reader = null;
    InputStream ism = null;
    InputStream ismb = null; // missing, with bad rows.
    ZipInputStream isbig = null; // missing, with bad rows.

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        reader = new DoubleMatrixReader();
        is = TestStringMatrixReader.class.getResourceAsStream( "/data/testdata.txt" );

        ism = TestStringMatrixReader.class.getResourceAsStream( "/data/testdatamissing.txt" );

        ismb = TestStringMatrixReader.class.getResourceAsStream( "/data/testdatamissing-badrows.txt" );

        isbig = new ZipInputStream( TestStringMatrixReader.class
                .getResourceAsStream( "/data/melanoma_and_sarcomaMAS5.zip" ) );
        isbig.getNextEntry();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        is.close();
        ism.close();
        ismb.close();
        isbig.close();
        matrix = null;
    }

    public void testReadInputStreamMissing() throws Exception {

        matrix = ( DoubleMatrixNamed ) reader.read( ism );
        int actualReturn = matrix.rows();
        int expectedReturn = 30;
        assertEquals( "return value", expectedReturn, actualReturn );

    }

    public void testReadInputStreamMissingBad() throws Exception {
        try {
            matrix = ( DoubleMatrixNamed ) reader.read( ismb );
            fail( "Should have gotten an IO error" );
        } catch ( IOException e ) {
        }
    }

    /*
     * Class under test for NamedMatrix read(InputStream)
     */
    public void testReadInputStreamRowCount() throws Exception {

        matrix = ( DoubleMatrixNamed ) reader.read( is );
        int actualReturn = matrix.rows();
        int expectedReturn = 30;
        assertEquals( "return value", expectedReturn, actualReturn );

    }

    public void testReadInputStreamColumnCount() throws Exception {

        matrix = ( DoubleMatrixNamed ) reader.read( is );
        int actualReturn = matrix.columns();
        int expectedReturn = 12;
        assertEquals( "return value", expectedReturn, actualReturn );

    }

    public void testReadInputStreamGotRowName() throws Exception {

        matrix = ( DoubleMatrixNamed ) reader.read( is );
        boolean actualReturn = matrix.containsRowName( "gene1_at" ) && matrix.containsRowName( "AFFXgene30_at" );
        boolean expectedReturn = true;
        assertEquals( "return value", expectedReturn, actualReturn );

    }

    public void testReadInputStreamGotColName() throws Exception {
        matrix = ( DoubleMatrixNamed ) reader.read( is );
        boolean actualReturn = matrix.containsColumnName( "sample1" ) && matrix.containsColumnName( "sample12" );
        boolean expectedReturn = true;
        assertEquals( "return value (for sample1 and sample12)", expectedReturn, actualReturn );

    }

    // public void testReadInputStreamBig() {
    // try {
    // matrix = ( DoubleMatrixNamed ) reader.read( isbig );
    // int actualReturn = matrix.rows();
    // int expectedReturn = 12533;
    // assertEquals( "return value ",
    // expectedReturn, actualReturn );
    // } catch ( IOException e ) {
    // e.printStackTrace();
    // } catch ( OutOfMemoryError e) {
    // e.printStackTrace();
    // }
    // }

}