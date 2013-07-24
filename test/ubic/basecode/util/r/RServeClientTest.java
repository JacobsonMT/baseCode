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
package ubic.basecode.util.r;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.io.reader.DoubleMatrixReader;
import ubic.basecode.math.Constants;
import ubic.basecode.util.RegressionTesting;

/**
 * @author pavlidis
 * @version $Id$
 */
public class RServeClientTest extends TestCase {
    private static Log log = LogFactory.getLog( RServeClientTest.class.getName() );

    boolean connected = false;
    RServeClient rc = null;
    double[] test1 = new double[] { -1.2241396, -0.6794486, -0.8475404, -0.4119554, -2.1980083 };
    double[] test2 = new double[] { 0.67676154, 0.20346679, 0.09289084, 0.68850551, 0.61120011 };
    DoubleMatrix<String, String> tester;

    @Override
    public void setUp() throws Exception {
        try {
            rc = new RServeClient();
            connected = rc.isConnected();
        } catch ( IOException e ) {
            connected = false;
        }

        DoubleMatrixReader reader = new DoubleMatrixReader();

        tester = reader.read( this.getClass().getResourceAsStream( "/data/testdata.txt" ) );
    }

    @Override
    public void tearDown() {
        tester = null;
        if ( rc != null && rc.isConnected() ) rc.disconnect();
    }

    public void testAssignAndRetrieveMatrix() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        String mat = rc.assignMatrix( tester );
        DoubleMatrix<String, String> result = rc.retrieveMatrix( mat );
        assertTrue( RegressionTesting.closeEnough( tester, result, 0.0001 ) );

        for ( int i = 0; i < tester.rows(); i++ ) {
            assertEquals( tester.getRowName( i ), result.getRowName( i ) );
        }

        for ( int i = 0; i < tester.columns(); i++ ) {
            assertEquals( tester.getColName( i ), result.getColName( i ) );
        }

    }

    public void testAssignAndRetrieveMatrixB() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        DoubleMatrix<String, String> result = rc.retrieveMatrix( rc.assignMatrix( tester.asArray() ) );
        assertTrue( RegressionTesting.closeEnough( tester, result, 0.0001 ) );

    }

    public void testAssignStringList() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }

        List<String> l = new ArrayList<String>();
        l.add( "foo" );
        l.add( "bar" );

        String varname = rc.assignStringList( l );
        String actualValue = rc.stringEval( varname + "[1]" );
        assertEquals( "foo", actualValue );
        actualValue = rc.stringEval( varname + "[2]" );
        assertEquals( "bar", actualValue );
    }

    public void testDoubleArrayTwoDoubleArrayEval() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        double[] actual = rc.doubleArrayTwoDoubleArrayEval( "a-b", "a", test1, "b", test2 );
        double[] expected = new double[] { -1.9009011, -0.8829154, -0.9404312, -1.1004609, -2.8092084 };
        RegressionTesting.closeEnough( expected, actual, Constants.SMALLISH );
    }

    public void testDoubleTwoDoubleArrayEval() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        double actual = rc.doubleTwoDoubleArrayEval( "cor(a,b)", "a", test1, "b", test2 );
        double expected = -0.29843518070456654;
        assertEquals( expected, actual, Constants.SMALLISH );
    }

    /*
     * Test method for ' RCommand.exec(String)'
     */
    public void testExec() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        String actualValue = rc.stringEval( "R.version.string" );
        String expectedValue = "R version";

        assertTrue( "rc.eval() return version " + actualValue + ", expected something starting with R version",
                actualValue.startsWith( expectedValue ) );
    }

    /*
     * Test method for ' RCommand.exec(String)'
     */
    public void testExecDoubleArray() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        assertTrue( RegressionTesting.closeEnough( new double[] { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 },
                rc.doubleArrayEval( "rep(1, 10)" ), 0.001 ) );
    }

    public void testExecError() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        try {
            rc.stringEval( "library(fooblydoobly)" );
            fail( "Should have gotten an exception" );
        } catch ( Exception e ) {
            assertTrue( e.getMessage().startsWith( "Error from R" ) );
        }
    }

    public void testFactorAssign() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }

        List<String> list = new ArrayList<String>();
        list.add( "a" );
        list.add( "b" );
        String factor = rc.assignFactor( list );
        assertNotNull( factor );
    }

    public void testFindExecutable() throws Exception {
        String cmd = RServeClient.findRserveCommand();
        assertNotNull( cmd ); // should always come up with something.
    }

    public void testLoadLibrary() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        assertFalse( rc.loadLibrary( "foooobly" ) );
        assertTrue( rc.loadLibrary( "graphics" ) );
    }

    public void testLoadScript() throws Exception {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }

        BufferedReader reader = new BufferedReader( new InputStreamReader( this.getClass().getResourceAsStream(
                "/ubic/basecode/util/r/testScript.R" ) ) );
        String line = null;
        StringBuilder buf = new StringBuilder();
        while ( ( line = reader.readLine() ) != null ) {
            if ( line.startsWith( "#" ) || StringUtils.isBlank( line ) ) {
                continue;
            }
            buf.append( StringUtils.trim( line ) + "\n" );
        }
        String sc = buf.toString();

        rc.loadScript( this.getClass().getResourceAsStream( "/ubic/basecode/util/r/linearModels.R" ) );
        String rawscript = "rowlm<-function(formula,data)" + "{\nmf<-lm(formula,data,method=\"model.frame\")\n"
                + "mt <- attr(mf, \"terms\")\n" + "x <- model.matrix(mt, mf)\n"
                + "design<-model.matrix(formula)\ncl <- match.call()\n" + "r<-nrow(data)\nres<-vector(\"list\",r)\n"
                + "lev<-.getXlevels(mt, mf)\nclz<-c(\"lm\")\n" + "D<-as.matrix(data)\n" + "ids<-row.names(data)\n"
                + "for(i in 1:r) {\n" + "y<-as.vector(D[i,])\n" + "id<-ids[i]\n" + "m<-is.finite(y)\n"
                + "if (sum(m) > 0) {\n" + "X<-design[m,,drop=FALSE]\n"
                + "attr(X,\"assign\")<-attr(design,\"assign\")\n" + "y<-y[m]\n" + "z<-lm.fit(X,y)\n"
                + "class(z) <- clz\n" + "z$na.action <- na.exclude\n" + "z$contrasts <- attr(x, \"contrasts\")\n"
                + "z$xlevels <- lev\n" + "z$call <- cl\n" + "z$terms <- mt\n" + "z$model <- mf\n" + "res[[i]]<-z"
                + "\n}\n" + "}\n" + "names(res)<-row.names(data)\n" + "return(res)\n" + "}\n";

        rc.voidEval( rawscript );
        rc.voidEval( sc );

        rc.loadScript( this.getClass().getResourceAsStream( "/ubic/basecode/util/r/linearModels.R" ) );
    }

    public void testStringListEval() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        List<String> actual = rc.stringListEval( "c('a','b')" );
        assertEquals( 2, actual.size() );
        assertEquals( "a", actual.get( 0 ) );
        assertEquals( "b", actual.get( 1 ) );
    }

    public void testTTest() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        List<String> rFactors = new ArrayList<String>();
        rFactors.add( "f" );
        rFactors.add( "f" );
        rFactors.add( "g" );
        rFactors.add( "g" );

        String facts = rc.assignStringList( rFactors );

        String tfacts = "t(" + facts + ")";

        String factor = "factor(" + tfacts + ")";

        DoubleMatrix<String, String> m = new DenseDoubleMatrix<String, String>( 1, 4 );
        m.set( 0, 0, 4.0 );
        m.set( 0, 1, 5.0 );
        m.set( 0, 2, 2.0 );
        m.set( 0, 3, 1.0 );
        String matrixName = rc.assignMatrix( m );

        /* handle the p-values */
        StringBuffer pvalueCommand = new StringBuffer();
        pvalueCommand.append( "apply(" );
        pvalueCommand.append( matrixName );
        pvalueCommand.append( ", 1, function(x) {t.test(x ~ " + factor + ")$p.value}" );
        pvalueCommand.append( ")" );

        double[] pvalues = rc.doubleArrayEval( pvalueCommand.toString() );
        assertEquals( 1, pvalues.length );
        assertEquals( 0.05, pvalues[0], 0.01 );
    }

    public void testTTestFail() {
        if ( !connected ) {
            log.warn( "Could not connect to RServe, skipping test." );
            return;
        }
        List<String> rFactors = new ArrayList<String>();
        rFactors.add( "f" );
        rFactors.add( "f" );
        rFactors.add( "g" );
        rFactors.add( "g" );

        String facts = rc.assignStringList( rFactors );
        String tfacts = "t(" + facts + ")";
        String factor = "factor(" + tfacts + ")";

        DoubleMatrix<String, String> m = new DenseDoubleMatrix<String, String>( 1, 4 );
        // will fail with "data are essentially constant"
        m.set( 0, 0, 4.0 );
        m.set( 0, 1, 4.0 );
        m.set( 0, 2, 2.0 );
        m.set( 0, 3, 2.0 );
        String matrixName = rc.assignMatrix( m );

        /* handle the p-values - will fail internally, but we silently return 1.0 */
        StringBuffer pvalueCommand = new StringBuffer();
        pvalueCommand.append( "apply(" + matrixName + ", 1, function(x) {  tryCatch( t.test(x ~ " + factor
                + ")$p.value, error=function(e) { 1.0})})" );

        double[] r = rc.doubleArrayEval( pvalueCommand.toString() );

        assertEquals( 1, r.length );
        assertEquals( 1.0, r[0], 0.00001 );

    }
}