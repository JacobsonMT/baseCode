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
package ubic.basecode.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPDouble;
import org.rosuda.REngine.REXPFactor;
import org.rosuda.REngine.REXPGenericVector;
import org.rosuda.REngine.REXPInteger;
import org.rosuda.REngine.REXPList;
import org.rosuda.REngine.REXPLogical;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPString;
import org.rosuda.REngine.RList;

import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.util.r.type.HTest;
import ubic.basecode.util.r.type.TwoWayAnovaResult;

/**
 * Base class for RClients
 * 
 * @author Paul
 * @version $Id$
 */
public abstract class AbstractRClient implements RClient {

    private static Log log = LogFactory.getLog( AbstractRClient.class.getName() );

    /**
     * @param ob
     * @return
     */
    public static String variableIdentityNumber( Object ob ) {
        return Integer.toString( Math.abs( ob.hashCode() ) ) + RandomStringUtils.randomAlphabetic( 6 );
    }

    /**
     * Copy a matrix into an array, so that rows are represented consecutively in the array. (RServe has no interface
     * for passing a 2-d array).
     * 
     * @param matrix
     * @return
     */
    private static double[] unrollMatrix( double[][] matrix ) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[] unrolledMatrix = new double[rows * cols];

        int k = 0;
        for ( int i = 0; i < rows; i++ ) {
            for ( int j = 0; j < cols; j++ ) {
                unrolledMatrix[k] = matrix[i][j];
                k++;
            }
        }
        return unrolledMatrix;
    }

    /**
     * Copy a matrix into an array, so that rows are represented consecutively in the array. (RServe has no interface
     * for passing a 2-d array).
     * 
     * @param matrix
     * @return array representation of the matrix.
     */
    private static double[] unrollMatrix( DoubleMatrix<?, ?> matrix ) {
        // unroll the matrix into an array Unfortunately this makes a
        // copy of the data...and R will probably make yet
        // another copy. If there was a way to get the raw element array from the DoubleMatrixNamed, that would
        // be better.
        int rows = matrix.rows();
        int cols = matrix.columns();
        double[] unrolledMatrix = new double[rows * cols];

        int k = 0;
        for ( int i = 0; i < rows; i++ ) {
            for ( int j = 0; j < cols; j++ ) {
                unrolledMatrix[k] = matrix.get( i, j );
                k++;
            }
        }
        return unrolledMatrix;
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#assignFactor(java.util.List)
     */
    public String assignFactor( List<String> strings ) {
        String variableName = "factor." + variableIdentityNumber( strings );
        Object[] array = strings.toArray();
        String[] sa = new String[array.length];
        for ( int i = 0; i < array.length; i++ ) {
            sa[i] = array[i].toString();
        }

        String l = assignStringList( strings );
        this.voidEval( variableName + "<-factor(" + l + ")" );
        return variableName;
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#assignMatrix(double[][])
     */
    public String assignMatrix( double[][] matrix ) {
        String matrixVarName = "Matrix_" + variableIdentityNumber( matrix );
        log.debug( "Assigning matrix with variable name " + matrixVarName );
        int rows = matrix.length;
        int cols = matrix[0].length;
        if ( rows == 0 || cols == 0 ) throw new IllegalArgumentException( "Empty matrix?" );
        double[] unrolledMatrix = unrollMatrix( matrix );
        this.assign( "U" + matrixVarName, unrolledMatrix ); // temporary
        this.voidEval( matrixVarName + "<-matrix(" + "U" + matrixVarName + ", nrow=" + rows + " , ncol=" + cols
                + ", byrow=TRUE)" );
        this.voidEval( "rm(U" + matrixVarName + ")" ); // maybe this saves memory...

        return matrixVarName;
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#assignMatrix(ubic.basecode.dataStructure.matrix.DoubleMatrixNamed)
     */
    public String assignMatrix( DoubleMatrix<?, ?> matrix ) {
        String matrixVarName = "Matrix_" + variableIdentityNumber( matrix );
        log.debug( "Assigning matrix with variable name " + matrixVarName );
        int rows = matrix.rows();
        int cols = matrix.columns();
        if ( rows == 0 || cols == 0 ) throw new IllegalArgumentException( "Empty matrix?" );
        double[] unrolledMatrix = unrollMatrix( matrix );
        assert ( unrolledMatrix != null );
        this.assign( "U" + matrixVarName, unrolledMatrix );
        this.voidEval( matrixVarName + "<-matrix(" + "U" + matrixVarName + ", nrow=" + rows + ", ncol=" + cols
                + ", byrow=TRUE)" );
        this.voidEval( "rm(U" + matrixVarName + ")" ); // maybe this saves memory...

        if ( matrix.hasColNames() && matrix.hasRowNames() ) assignRowAndColumnNames( matrix, matrixVarName );
        return matrixVarName;
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#assignStringList(java.util.List)
     */
    @SuppressWarnings("unchecked")
    public String assignStringList( List strings ) {
        String variableName = "stringList." + variableIdentityNumber( strings );

        Object[] array = strings.toArray();
        String[] sa = new String[array.length];
        for ( int i = 0; i < array.length; i++ ) {
            sa[i] = array[i].toString();
        }

        assign( variableName, sa );
        return variableName;
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#booleanDoubleArrayEval(java.lang.String, java.lang.String, double[])
     */
    public boolean booleanDoubleArrayEval( String command, String argName, double[] arg ) {
        this.assign( argName, arg );
        REXP x = this.eval( command );
        if ( x.isLogical() ) {
            try {
                REXPLogical b = new REXPLogical( new boolean[1], new REXPList( x.asList() ) );
                return b.isTRUE()[0];
            } catch ( REXPMismatchException e ) {
                throw new RuntimeException( e );
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#doubleArrayDoubleArrayEval(java.lang.String, java.lang.String, double[])
     */
    public double[] doubleArrayDoubleArrayEval( String command, String argName, double[] arg ) {
        try {
            this.assign( argName, arg );
            RList l = this.eval( command ).asList();
            return l.at( argName ).asDoubles();
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#doubleArrayEval(java.lang.String)
     */
    public double[] doubleArrayEval( String command ) {
        REXP r = this.eval( command );
        if ( r == null ) {
            return null;
        }
        try {
            return r.asDoubles();
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#doubleArrayEvalWithLogging(java.lang.String)
     */
    public double[] doubleArrayEvalWithLogging( String command ) {
        RLoggingThread rLoggingThread = null;
        double[] doubleArray = null;
        try {
            rLoggingThread = RLoggingThreadFactory.createRLoggingThread();
            doubleArray = this.doubleArrayEval( command );
        } catch ( Exception e ) {
            throw new RuntimeException( "Problems executing R command " + command + ": " + e.getMessage() );
        } finally {
            if ( rLoggingThread != null ) {
                log.debug( "Shutting down logging thread." );
                rLoggingThread.done();
            }
        }
        return doubleArray;
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#doubleArrayTwoDoubleArrayEval(java.lang.String, java.lang.String, double[],
     * java.lang.String, double[])
     */
    public double[] doubleArrayTwoDoubleArrayEval( String command, String argName, double[] arg, String argName2,
            double[] arg2 ) {
        this.assign( argName, arg );
        this.assign( argName2, arg2 );
        try {
            return this.eval( command ).asDoubles();
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#doubleTwoDoubleArrayEval(java.lang.String, java.lang.String, double[],
     * java.lang.String, double[])
     */
    public double doubleTwoDoubleArrayEval( String command, String argName, double[] arg, String argName2, double[] arg2 ) {
        this.assign( argName, arg );
        this.assign( argName2, arg2 );
        REXP x = this.eval( command );
        try {
            return x.asDouble();
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#intArrayEval(java.lang.String)
     */
    public int[] intArrayEval( String command ) {
        try {
            return eval( command ).asIntegers();
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }
    }

    public List<?> listEval( Class<?> listEntryType, String command ) {

        REXP rexp = this.eval( command );

        List<Object> result = new ArrayList<Object>();
        try {
            if ( !rexp.isVector() ) {
                throw new IllegalArgumentException( "Command did not return some kind of vector" );
            }

            if ( rexp instanceof REXPInteger ) {
                log.debug( "integer" );
                double[][] asDoubleMatrix = rexp.asDoubleMatrix();
                for ( double[] ds : asDoubleMatrix ) {
                    result.add( ds );
                }

                if ( rexp instanceof REXPFactor ) {
                    log.info( "factor" );
                    // not sure what to do...
                }
            } else if ( rexp instanceof REXPGenericVector ) {
                log.debug( "generic" );
                REXPGenericVector v = ( REXPGenericVector ) rexp;
                List<?> tmp = new ArrayList<Object>( v.asList().values() );

                if ( tmp.size() == 0 ) return tmp;

                for ( Object t : tmp ) {
                    String clazz = ( ( REXP ) t ).getAttribute( "class" ).asString();
                    /*
                     * FIXME!!!!
                     */
                    if ( clazz.equals( "htest" ) ) {
                        try {
                            result.add( new HTest( ( ( REXP ) t ).asList() ) );
                        } catch ( REXPMismatchException e ) {
                            result.add( new HTest() );
                        }
                    } else if ( clazz.equals( "lm" ) ) {
                        throw new UnsupportedOperationException();
                    } else {
                        result.add( new HTest() ); // e.g. failed result or something we don't know about yet
                    }
                    /*
                     * todo: support lm objects, anova objects others? pair.htest?
                     */
                }

            } else if ( rexp instanceof REXPDouble ) {
                log.debug( "double" );
                double[][] asDoubleMatrix = rexp.asDoubleMatrix();
                for ( double[] ds : asDoubleMatrix ) {
                    result.add( ds );
                }

            } else if ( rexp instanceof REXPList ) {
                log.debug( "list" );
                if ( rexp.isPairList() ) {
                    // log.info( "pairlist" ); always true for REXPList.
                }

                if ( rexp.isLanguage() ) {
                    throw new UnsupportedOperationException( "Don't know how to deal with vector type of "
                            + rexp.getClass().getName() );
                } else {

                    log.debug( rexp.getClass().getName() );
                    result = new ArrayList<Object>( rexp.asList().values() );
                }
            } else {
                throw new UnsupportedOperationException( "Don't know how to deal with vector type of "
                        + rexp.getClass().getName() );
            }

            return result;
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }

    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#listEvalWithLogging(java.lang.Class, java.lang.String)
     */
    public List<?> listEvalWithLogging( Class<?> listEntryType, String command ) {
        RLoggingThread rLoggingThread = null;
        List<?> result = null;
        try {
            rLoggingThread = RLoggingThreadFactory.createRLoggingThread();
            result = this.listEval( listEntryType, command );
        } catch ( Exception e ) {
            throw new RuntimeException( "Problems executing R command " + command.toString(), e );
        } finally {
            if ( rLoggingThread != null ) {
                log.debug( "Shutting down logging thread." );
                rLoggingThread.done();
            }
        }
        return result;

    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#loadLibrary(java.lang.String)
     */
    public boolean loadLibrary( String libraryName ) {
        List<String> libraries = stringListEval( "installed.packages()[,1]" );
        if ( libraries.contains( libraryName ) ) {
            voidEval( "library(" + libraryName + ")" );
            return true;
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#remove(java.lang.String)
     */
    public void remove( String variableName ) {
        this.voidEval( "rm(" + variableName + ")" );
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#twoWayAnovaEval(java.lang.String)
     */
    public TwoWayAnovaResult twoWayAnovaEval( String command, boolean withInteractions ) {
        REXP rawResult = this.eval( command );

        if ( rawResult == null ) {
            return null;
        }

        RList mainList;
        try {
            mainList = rawResult.asList();
        } catch ( REXPMismatchException e1 ) {
            throw new RuntimeException( e1 );
        }
        if ( mainList == null ) {
            return null;
        }

        double[] missingData;
        int numPvalsPerExample;
        if ( withInteractions ) {
            numPvalsPerExample = 3;
            missingData = new double[] { Double.NaN, Double.NaN, Double.NaN };
        } else {
            numPvalsPerExample = 2;
            missingData = new double[] { Double.NaN, Double.NaN };
        }

        /*
         * The values are in the correct order, but the keys in the mainList (from mainList.keys()) are not for some
         * reason so I'm not using them. In the debugger, the key is the composite sequence, but this isn't the same
         * composite sequence I see directly in R. The order of the p values and statistics are correct, however.
         */
        LinkedHashMap<String, double[]> pvalues = new LinkedHashMap<String, double[]>();
        LinkedHashMap<String, double[]> statistics = new LinkedHashMap<String, double[]>();
        log.debug( mainList.keys().length + " results." );
        try {
            for ( int i = 0; i < mainList.keys().length; i++ ) {

                if ( log.isDebugEnabled() ) log.debug( "Key: " + mainList.keyAt( i ) );

                REXP r1 = mainList.at( i );

                String probeKeyPlaceholder = Integer.toString( i );

                if ( !r1.isList() ) { // like a failed result?
                    log.debug( "No anovaresult for " + probeKeyPlaceholder );
                    pvalues.put( probeKeyPlaceholder, missingData );
                    statistics.put( probeKeyPlaceholder, missingData );
                    continue;
                }

                RList l1 = r1.asList();

                String[] keys = l1.keys();

                for ( String key : keys ) {

                    /*
                     * Ensure we put something in.
                     */
                    pvalues.put( probeKeyPlaceholder, missingData );
                    statistics.put( probeKeyPlaceholder, missingData );

                    if ( StringUtils.equalsIgnoreCase( "Pr(>F)", key ) ) {
                        REXP r2 = l1.at( key );
                        double[] pValsFromR = r2.asDoubles();

                        if ( pValsFromR.length != numPvalsPerExample + 1 ) { // extra value from row for residuals.
                            /*
                             * This can happen if the interaction could not be estimated.
                             */
                            log
                                    .info( "No valid anovaresult for " + probeKeyPlaceholder + ", got "
                                            + r2.toDebugString() );
                            continue;
                        }

                        double[] pValsToUse = new double[numPvalsPerExample];

                        for ( int j = 0; j < numPvalsPerExample; j++ ) {
                            pValsToUse[j] = pValsFromR[j];
                        }

                        pvalues.put( probeKeyPlaceholder, pValsToUse );
                    } else if ( StringUtils.equalsIgnoreCase( "F value", key ) ) {
                        REXP r2 = l1.at( key );
                        double[] statisticsFromR = r2.asDoubles();
                        double[] statisticsToUse = new double[numPvalsPerExample];
                        for ( int j = 0; j < numPvalsPerExample; j++ ) {
                            statisticsToUse[j] = statisticsFromR[j];
                        }
                        assert statisticsToUse.length == numPvalsPerExample;
                        statistics.put( probeKeyPlaceholder, statisticsToUse );
                    }

                }

            }
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }

        assert statistics.size() == mainList.size();
        assert pvalues.size() == mainList.size();

        TwoWayAnovaResult result = new TwoWayAnovaResult( pvalues, statistics, withInteractions );

        return result;

    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#stringEval(java.lang.String)
     */
    public String stringEval( String command ) {
        try {
            return this.eval( command ).asString();
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * @param string
     * @return
     */
    public List<String> stringListEval( String command ) {
        try {
            REXP eval = this.eval( command );

            RList v;
            List<String> results = new ArrayList<String>();
            if ( eval instanceof REXPString ) {
                String[] strs = ( ( REXPString ) eval ).asStrings();
                for ( String string : strs ) {
                    results.add( string );
                }
            } else {
                v = eval.asList();
                for ( Iterator<?> it = v.iterator(); it.hasNext(); ) {
                    results.add( ( ( REXPString ) it.next() ).asString() );
                }
            }

            return results;
        } catch ( REXPMismatchException e ) {
            throw new RuntimeException( e );
        }
    }

    /*
     * (non-Javadoc)
     * @see ubic.basecode.util.RClient#twoWayAnovaEvalWithLogging(java.lang.String)
     */
    public TwoWayAnovaResult twoWayAnovaEvalWithLogging( String command, boolean withInteractions ) {
        RLoggingThread rLoggingThread = null;
        TwoWayAnovaResult twoWayAnovaResult = null;
        try {
            rLoggingThread = RLoggingThreadFactory.createRLoggingThread();
            twoWayAnovaResult = this.twoWayAnovaEval( command, withInteractions );
        } catch ( Exception e ) {
            log.error( "Problems executing R command " + command.toString(), e );
        } finally {
            if ( rLoggingThread != null ) {
                log.debug( "Shutting down logging thread." );
                rLoggingThread.done();
            }
        }
        return twoWayAnovaResult;
    }

    /**
     * @param matrix
     * @param matrixVarName
     * @return
     */
    protected <R, C> void assignRowAndColumnNames( DoubleMatrix<R, C> matrix, String matrixVarName ) {

        String rowNameVar = assignStringList( matrix.getRowNames() );
        String colNameVar = assignStringList( matrix.getColNames() );

        String dimcmd = "dimnames(" + matrixVarName + ")<-list(" + rowNameVar + ", " + colNameVar + ")";
        this.voidEval( dimcmd );
    }

    protected abstract REXP eval( String command );

}
