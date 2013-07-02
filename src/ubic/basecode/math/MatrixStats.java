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

import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix;
import ubic.basecode.dataStructure.matrix.DenseDoubleMatrix1D;
import ubic.basecode.dataStructure.matrix.DoubleMatrix;
import ubic.basecode.dataStructure.matrix.SparseDoubleMatrix;
import cern.colt.function.DoubleFunction;
import cern.colt.function.DoubleProcedure;
import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import cern.jet.math.Functions;

/**
 * @author pavlidis
 * @version $Id$
 */
public class MatrixStats {

    /**
     * Convert a log_b-transformed data set to log 2.
     * 
     * @param matrix
     * @param base the current base
     */
    public static <R, C> void convertToLog2( DoubleMatrix<R, C> matrix, double base ) {
        double v = Math.log( 2.0 ) / Math.log( base );
        for ( int j = 0; j < matrix.rows(); j++ ) {
            DoubleMatrix1D row = matrix.viewRow( j );
            row.assign( Functions.div( v ) );
            for ( int i = 0; i < row.size(); i++ ) {
                matrix.set( j, i, row.get( i ) );
            }
        }
    }

    /**
     * Calculates the library size by performing colSums(data). NaN values are omitted from calculations.
     * 
     * @param <R>
     * @param <C>
     * @param data
     * @return column sums
     */
    public static <R, C> DoubleMatrix1D colSums( DoubleMatrix<R, C> data ) {
        assert data != null;
        DoubleMatrix1D librarySize = new DenseDoubleMatrix1D( data.columns() );
        for ( int i = 0; i < librarySize.size(); i++ ) {
            librarySize.set( i, DescriptiveWithMissing.sum( new DoubleArrayList( data.getColumn( i ) ) ) );
        }
        return librarySize;
    }

    /**
     * Convert the matrix to log2 counts per million. Equivalent to <code>t(log2(t(counts+0.5)/(lib.size+1)*1e6))</code>
     * in R.
     * 
     * @param matrix
     * @param librarySize if null, it will default to <code>colSums(matrix)</code>.
     */
    public static <R, C> void convertToLog2Cpm( DoubleMatrix<R, C> matrix, DoubleMatrix1D librarySize ) {

        if ( librarySize == null ) {
            librarySize = new DenseDoubleMatrix1D( matrix.columns() );
            for ( int i = 0; i < librarySize.size(); i++ ) {
                librarySize.set( i, DescriptiveWithMissing.sum( new DoubleArrayList( matrix.getColumn( i ) ) ) );
            }
        }

        assert librarySize.size() == matrix.columns();

        for ( int j = 0; j < matrix.rows(); j++ ) {
            DoubleMatrix1D row = matrix.viewRow( j );
            for ( int i = 0; i < row.size(); i++ ) {
                double val = matrix.get( j, i );
                val = ( val + 0.5 ) / ( librarySize.get( i ) + 1.0 ) * Math.pow( 10, 6 );
                val = Math.log( val ) / Math.log( 2.0 );
                matrix.set( j, i, val );
            }
        }
    }

    /**
     * Compute the correlation matrix of the rows of a matrix.
     * 
     * @param data
     * @return a symmetric matrix that has the rows and columns set to be the names of the rows of the input.
     */
    public static <R, C> DoubleMatrix<R, R> correlationMatrix( DoubleMatrix<R, C> data ) {
        DoubleMatrix<R, R> result = new DenseDoubleMatrix<R, R>( data.rows(), data.rows() );

        for ( int i = 0; i < data.rows(); i++ ) {
            DoubleArrayList irow = new DoubleArrayList( data.getRow( i ) );
            for ( int j = i; j < data.rows(); j++ ) {
                if ( j == i ) {
                    result.set( i, j, 1.0 );
                    continue;
                }
                DoubleArrayList jrow = new DoubleArrayList( data.getRow( j ) );
                double c = DescriptiveWithMissing.correlation( irow, jrow );
                result.set( i, j, c );
                result.set( j, i, c );
            }
        }
        result.setRowNames( data.getRowNames() );
        result.setColumnNames( data.getRowNames() );

        return result;
    }

    /**
     * @param data DenseDoubleMatrix2DNamed
     * @param threshold only correlations with absolute values above this level are stored.
     * @return a sparse symmetric matrix that has the rows and columns set to be the names of the rows of the input.
     */
    public static <R, C> SparseDoubleMatrix<R, R> correlationMatrix( DoubleMatrix<R, C> data, double threshold ) {
        SparseDoubleMatrix<R, R> result = new SparseDoubleMatrix<R, R>( data.rows(), data.rows() );

        for ( int i = 0; i < data.rows(); i++ ) {
            DoubleArrayList irow = new DoubleArrayList( data.getRow( i ) );
            for ( int j = i + 1; j < data.rows(); j++ ) {
                DoubleArrayList jrow = new DoubleArrayList( data.getRow( j ) );
                double c = DescriptiveWithMissing.correlation( irow, jrow );
                if ( Math.abs( c ) > threshold ) {
                    result.set( i, j, c );
                    result.set( j, i, c );
                }
            }
        }
        result.setRowNames( data.getRowNames() );
        result.setColumnNames( data.getRowNames() );

        return result;
    }

    /**
     * Normalize a count matrix in place to be a transition matrix. Assumes that the values are defined as "bigger is
     * better"
     * 
     * @param matrixToNormalize
     */
    public static <R, C> void countsNormalize( DoubleMatrix<R, C> matrixToNormalize ) {

        final double min = MatrixStats.min( matrixToNormalize );
        DoubleFunction f = new DoubleFunction() {
            @Override
            public double apply( double value ) {
                return value - min + 1;
            }
        };

        for ( int j = 0; j < matrixToNormalize.rows(); j++ ) { // do each row in turn ...
            DoubleMatrix1D row = matrixToNormalize.viewRow( j );
            row.assign( f );
            double sum = row.zSum();
            row.assign( Functions.div( sum ) );
        }
    }

    /**
     * Iteratively standardize the columns and rows of the matrix.
     * 
     * @param data
     */
    public static <R, C> DoubleMatrix<R, C> doubleStandardize( DoubleMatrix<R, C> matrix ) {
        DoubleMatrix<R, C> newMatrix = matrix.copy();

        int ITERS = 5;
        for ( int i = 0; i < ITERS; i++ ) {
            // scale columns, then rows.
            newMatrix = standardize( standardize( newMatrix.transpose() /* columns */).transpose() /* rows */);
        }

        /*
         * Check convergence.DEBUG CODE
         */
        MatrixRowStats.means( newMatrix ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                if ( Math.abs( element ) > 0.02 ) {
                    // throw new IllegalStateException( "Row mean was: " + Math.abs( element ) );
                }
                return true;
            }
        } );

        MatrixRowStats.means( newMatrix.transpose() ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                if ( Math.abs( element ) > 0.1 ) {
                    // throw new IllegalStateException( "Column mean was: " + Math.abs( element ) );
                }
                return true;
            }
        } );

        MatrixRowStats.sampleStandardDeviations( newMatrix ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                if ( Math.abs( element - 1.0 ) > 0.1 ) {
                    // throw new IllegalStateException();
                }
                return true;
            }
        } );

        MatrixRowStats.sampleStandardDeviations( newMatrix.transpose() ).forEach( new DoubleProcedure() {
            @Override
            public boolean apply( double element ) {
                if ( Math.abs( element - 1.0 ) > 0.1 ) {
                    // throw new IllegalStateException();
                }
                return true;
            }
        } );

        return newMatrix;
    }

    /**
     * Log-transform the values in a matrix (log base 2). Values that are less than or equal to zero are left as
     * Double.NaN.
     * 
     * @param matrixToNormalize
     */
    public static <R, C> void logTransform( DoubleMatrix<R, C> matrix ) {
        for ( int j = 0; j < matrix.rows(); j++ ) {
            DoubleMatrix1D row = matrix.viewRow( j );
            row.assign( Functions.log2 );
            for ( int i = 0; i < row.size(); i++ ) {
                matrix.set( j, i, row.get( i ) );
            }
        }
    }

    /**
     * Compute the maximum value in the matrix.
     * 
     * @param matrix DenseDoubleMatrix2DNamed
     * @return the largest value in the matrix
     */
    public static <R, C> double max( DoubleMatrix<R, C> matrix ) {

        int totalRows = matrix.rows();
        int totalColumns = matrix.columns();

        double max = -Double.MAX_VALUE;

        for ( int i = 0; i < totalRows; i++ ) {
            for ( int j = 0; j < totalColumns; j++ ) {
                double val = matrix.get( i, j );
                if ( Double.isNaN( val ) ) {
                    continue;
                }

                if ( val > max ) {
                    max = val;
                }
            }
        }

        if ( max == -Double.MAX_VALUE ) {
            return Double.NaN;
        }

        return max; // might be NaN if all values are missing

    }

    /**
     * Find the minimum of the entire matrix.
     * 
     * @param matrix DenseDoubleMatrix2DNamed
     * @return the smallest value in the matrix
     */
    public static <R, C> double min( DoubleMatrix<R, C> matrix ) {

        int totalRows = matrix.rows();
        int totalColumns = matrix.columns();

        double min = Double.MAX_VALUE;

        for ( int i = 0; i < totalRows; i++ ) {
            for ( int j = 0; j < totalColumns; j++ ) {
                double val = matrix.get( i, j );
                if ( Double.isNaN( val ) ) {
                    continue;
                }

                if ( val < min ) {
                    min = val;
                }

            }
        }
        if ( min == Double.MAX_VALUE ) {
            return Double.NaN;
        }
        return min; // might be NaN if all values are missing

    } // end min

    /**
     * @param data
     * @return matrix indicating whether each value in the input matix is NaN.
     */
    public static boolean[][] nanStatusMatrix( double[][] data ) {
        boolean[][] result = new boolean[data.length][];
        for ( int i = 0; i < data.length; i++ ) {
            double[] row = data[i];
            result[i] = new boolean[data[i].length];
            for ( int j = 0; j < row.length; j++ ) {
                result[i][j] = Double.isNaN( data[i][j] );
            }
        }
        return result;
    }

    /**
     * @param matrix
     * @return
     */
    public static DoubleMatrix2D pseudoinverse( DoubleMatrix2D matrix ) {
        Algebra solver = new Algebra();
        DoubleMatrix2D tDes = solver.transpose( matrix );
        DoubleMatrix2D mult = solver.mult( tDes, matrix );
        DoubleMatrix2D invXXT = solver.inverse( mult );
        return solver.mult( invXXT, tDes );
    }

    /**
     * Normalize a matrix in place to be a transition matrix. Assumes that values operate such that small values like p
     * values represent closer distances, and the values are probabilities.
     * <p>
     * Each point is first transformed via v' = exp(-v/sigma). Then the values for each node's edges are adjusted to sum
     * to 1.
     * 
     * @param matrixToNormalize
     * @param sigma a scaling factor for the input values.
     */
    public static <R, C> void rbfNormalize( DoubleMatrix<R, C> matrixToNormalize, final double sigma ) {

        // define the function we will use.
        DoubleFunction f = new DoubleFunction() {
            @Override
            public double apply( double value ) {
                return Math.exp( -value / sigma );
            }
        };

        for ( int j = 0; j < matrixToNormalize.rows(); j++ ) {

            DoubleMatrix1D row = matrixToNormalize.viewRow( j );
            row.assign( f );
            double sum = row.zSum();
            row.assign( Functions.div( sum ) );

        }
    }

    /**
     * @param input raw double 2-d matrix
     * @return the element-by-element product (not matrix product) of the matrix.
     */
    public static double[][] selfSquaredMatrix( double[][] input ) {
        double[][] returnValue = new double[input.length][];
        for ( int i = 0; i < returnValue.length; i++ ) {
            returnValue[i] = new double[input[i].length];

            for ( int j = 0; j < returnValue[i].length; j++ ) {
                returnValue[i][j] = input[i][j] * input[i][j];
            }

        }
        return returnValue;
    }

    /**
     * Scale the rows of the matrix; returns a new matrix.
     * 
     * @param <R>
     * @param <C>
     * @param data
     * @return
     */
    public static <R, C> DoubleMatrix<R, C> standardize( DoubleMatrix<R, C> matrix ) {
        DoubleMatrix<R, C> newMatrix = new DenseDoubleMatrix<R, C>( matrix.rows(), matrix.columns() );
        newMatrix.setRowNames( matrix.getRowNames() );
        newMatrix.setColumnNames( matrix.getColNames() );
        for ( int i = 0; i < matrix.rows(); i++ ) {
            double[] row = matrix.getRow( i );
            DoubleArrayList li = new DoubleArrayList( row );
            DescriptiveWithMissing.standardize( li );

            /*
             * DEBUG CODE
             */
            if ( Math.abs( DescriptiveWithMissing.mean( li ) ) > 0.001 ) {
                throw new IllegalStateException( "NOT CENTERED" );
            }
            if ( Math.abs( DescriptiveWithMissing.sampleVariance( li, DescriptiveWithMissing.mean( li ) ) - 1.0 ) > 0.001 ) {
                throw new IllegalStateException( "NOT SCALED" );
            }

            for ( int j = 0; j < matrix.columns(); j++ ) {
                newMatrix.set( i, j, li.getQuick( j ) );
            }
        }
        return newMatrix;
    }

    /**
     * Undo log2 transform.
     * 
     * @param <R>
     * @param <C>
     * @param matrix
     */
    public static <R, C> void unLogTransform( DoubleMatrix<R, C> matrix ) {

        for ( int j = 0; j < matrix.rows(); j++ ) {
            DoubleMatrix1D row = matrix.viewRow( j );
            for ( int i = 0; i < row.size(); i++ ) {
                row.setQuick( i, Math.pow( 2.0, row.getQuick( i ) ) );
            }
        }

    }

}
