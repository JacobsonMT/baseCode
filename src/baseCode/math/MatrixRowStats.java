package baseCode.math;

import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import cern.colt.list.DoubleArrayList;

/**
 * Convenience functions for getting row statistics from matrices.
 * <p>
 * Copyright (c) 2004
 * </p>
 * <p>
 * Institution:: Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 * @todo Have min() and max() throw an EmptyMatrixException -- this exception
 *       class does not yet exist and needs to be defined somewhere.
 */
public class MatrixRowStats {

   private MatrixRowStats() {
   }

   public static DoubleArrayList sumOfSquaredDeviations(
         DenseDoubleMatrix2DNamed M ) {
      return sumOfSquaredDeviations( M, means( M ) );
   }

   /**
    * Calculates the sum of squares for each row of a matrix
    * 
    * @param M DenseDoubleMatrix2DNamed
    * @param means DoubleArrayList
    * @return DoubleArrayList
    */
   public static DoubleArrayList sumOfSquaredDeviations(
         DenseDoubleMatrix2DNamed M, DoubleArrayList means ) {
      DoubleArrayList r = new DoubleArrayList();

      for ( int i = 0; i < M.rows(); i++ ) {
         DoubleArrayList row = new DoubleArrayList( M.getRow( i ) );
         r.add( DescriptiveWithMissing.sumOfSquaredDeviations( row ) );
      }

      return r;
   }

   /**
    * Calculates the means of a matrix's rows.
    * 
    * @param M DenseDoubleMatrix2DNamed
    * @return DoubleArrayList
    */
   public static DoubleArrayList means( DenseDoubleMatrix2DNamed M ) {
      DoubleArrayList r = new DoubleArrayList();
      for ( int i = 0; i < M.rows(); i++ ) {
         r.add( DescriptiveWithMissing
               .mean( new DoubleArrayList( M.getRow( i ) ) ) );
      }
      return r;
   }

   /**
    * Calculate the sums of a matrix's rows.
    * 
    * @param M DenseDoubleMatrix2DNamed
    * @return DoubleArrayList
    * @todo calls new a lot.
    */
   public static DoubleArrayList sums( DenseDoubleMatrix2DNamed M ) {
      DoubleArrayList r = new DoubleArrayList();
      for ( int i = 0; i < M.rows(); i++ ) {
         r.add( DescriptiveWithMissing
               .sum( new DoubleArrayList( M.getRow( i ) ) ) );
      }
      return r;
   }

   /**
    * Calculates the standard deviation of each row of a matrix
    * 
    * @param M DenseDoubleMatrix2DNamed
    * @return DoubleArrayList
    */
   public static DoubleArrayList standardDeviations( DenseDoubleMatrix2DNamed M ) {
      DoubleArrayList r = new DoubleArrayList();
      for ( int i = 0; i < M.rows(); i++ ) {
         DoubleArrayList row = new DoubleArrayList( M.getRow( i ) );
         r.add( DescriptiveWithMissing
               .standardDeviation( DescriptiveWithMissing.variance(
                     DescriptiveWithMissing.sizeWithoutMissingValues( row ),
                     DescriptiveWithMissing.mean( row ), DescriptiveWithMissing
                           .sumOfSquares( row ) ) ) );
      }
      return r;
   }

   /**
    * Find the minimum of the entire matrix.
    * 
    * @param matrix DenseDoubleMatrix2DNamed
    * @return the smallest value in the matrix
    * @todo this should go somewhere else.
    */
   public static double min( DenseDoubleMatrix2DNamed matrix ) {

      int totalRows = matrix.rows();
      int totalColumns = matrix.columns();

      double min = Double.MAX_VALUE;

      for ( int i = 0; i < totalRows; i++ ) {
         for ( int j = 0; j < totalColumns; j++ ) {
            double val = matrix.getQuick( i, j );
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
    * Compute the maximum value in the matrix.
    * 
    * @param matrix DenseDoubleMatrix2DNamed
    * @return the largest value in the matrix
    * @todo this should go somewhere else.
    */
   public static double max( DenseDoubleMatrix2DNamed matrix ) {

      int totalRows = matrix.rows();
      int totalColumns = matrix.columns();

      double max = -Double.MAX_VALUE;

      for ( int i = 0; i < totalRows; i++ ) {
         for ( int j = 0; j < totalColumns; j++ ) {
            double val = matrix.getQuick( i, j );
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

   } // end max

}