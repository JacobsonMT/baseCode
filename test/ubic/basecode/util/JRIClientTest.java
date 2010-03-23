/*
 * The baseCode project
 * 
 * Copyright (c) 2006-2010 University of British Columbia
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
package ubic.basecode.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.basecode.math.Constants;

/**
 * @author pavlidis
 * @version $Id$
 */
public class JRIClientTest extends TestCase {
    private static Log log = LogFactory.getLog( JRIClientTest.class.getName() );

    JRIClient rc = null;
    boolean connected = true;
    DoubleMatrix<String, String> tester;
    double[] test1 = new double[] { -1.2241396, -0.6794486, -0.8475404, -0.4119554, -2.1980083 };
    double[] test2 = new double[] { 0.67676154, 0.20346679, 0.09289084, 0.68850551, 0.61120011 };

    @Override
    public void setUp() throws Exception {

        if ( rc == null ) {

            try {
                rc = new JRIClient();
                if ( rc == null || !rc.isConnected() ) {
                    connected = false;
                    return;
                }
            } catch ( RuntimeException e ) {
                log.error( e, e );
                connected = false;
                return;
            }
        }

        DoubleMatrixReader reader = new DoubleMatrixReader();

        tester = reader.read( this.getClass().getResourceAsStream( "/data/testdata.txt" ) );
        assert rc.isConnected();
    }

    @Override
    public void tearDown() throws Exception {
        tester = null;
        // rc.disconnect();
        rc = null;
    }

    public void testAssignAndRetrieveMatrix() throws Exception {
        if ( !connected ) {
            log.warn( "Cannot load JRI, skipping test" );
            return;
        }
        DoubleMatrix<String, String> result = rc.retrieveMatrix( rc.assignMatrix( tester ) );
        assertEquals( "gene1_at", result.getRowName( 0 ) );
        assertEquals( "sample1", result.getColName( 0 ) );
        assertTrue( RegressionTesting.closeEnough( tester, result, 0.0001 ) );
    }

    public void testAssignAndRetrieveMatrixB() throws Exception {
        if ( !connected ) {
            log.warn( "Cannot load JRI, skipping test" );
            return;
        }
        String m = rc.assignMatrix( tester.asArray() );
        DoubleMatrix<String, String> result = rc.retrieveMatrix( m );
        assertTrue( RegressionTesting.closeEnough( tester, result, 0.0001 ) );
    }

    public void testStringListEval() throws Exception {
        if ( !connected ) {
            log.warn( "Cannot load JRI, skipping test" );
            return;
        }
        List<String> actual = rc.stringListEval( "c('a','b')" );
        assertEquals( 2, actual.size() );
        assertEquals( "a", actual.get( 0 ) );
        assertEquals( "b", actual.get( 1 ) );
    }

    public void testFactorAssign() throws Exception {
        if ( !connected ) {
            log.warn( "Cannot load JRI, skipping test" );
            return;
        }

        List<String> list = new ArrayList<String>();
        list.add( "a" );
        list.add( "b" );
        String factor = rc.assignFactor( list );
        assertNotNull( factor );
    }

    /*
     * Test method for 'RCommand.exec(String)'
     */
    public void testExec() throws Exception {
        if ( !connected ) {
            log.warn( "Cannot load JRI, skipping test" );
            return;
        }
        String actualValue = rc.stringEval( "R.version.string" );
        String expectedValue = "R version 2";

        assertTrue( "rc.eval() returned version '" + actualValue + "', expected something starting with R version 2",
                actualValue.startsWith( expectedValue ) );
    }

    public void testExecError() throws Exception {
        if ( !connected ) {
            log.warn( "Cannot load JRI, skipping test." );
            return;
        }
        try {
            rc.stringEval( "library(fooblydoobly)" );
            fail( "Should have gotten an exception" );
        } catch ( Exception e ) {
            assertTrue( e.getMessage().startsWith( "Error from R" ) );
        }
        try {
            rc.stringEval( "t.test(dadx,ymom)" );
            fail( "Should have gotten an exception" );
        } catch ( Exception e ) {
            assertTrue( e.getMessage().startsWith( "Error from R" ) );
        }
        try {
            rc.stringEval( "wwfollck(1)" );
            fail( "Should have gotten an exception" );
        } catch ( Exception e ) {
            assertTrue( e.getMessage().startsWith( "Error from R" ) );
        }
        try {
            rc.stringEval( "sqrt(\"A\")" );
            fail( "Should have gotten an exception" );
        } catch ( Exception e ) {
            assertTrue( e.getMessage().startsWith( "Error from R" ) );
        }
    }

    /*
     * Test method for 'exec(String)'
     */
    public void testExecDoubleArray() throws Exception {
        if ( !connected ) {
            log.warn( "Cannot load JRI, skipping test" );
            return;
        }
        double[] dd = rc.doubleArrayEval( "rep(1, 10)" );
        assertNotNull( dd );
        assertTrue( RegressionTesting.closeEnough( new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, dd, 0.001 ) );
    }

    public void testDoubleTwoDoubleArrayEval() throws Exception {
        if ( !connected ) {
            log.warn( "Cannot load JRI, skipping test" );
            return;
        }
        double actual = rc.doubleTwoDoubleArrayEval( "cor(a,b)", "a", test1, "b", test2 );
        double expected = -0.29843518070456654;
        assertEquals( expected, actual, Constants.SMALLISH );
    }
}