package baseCode.math;

import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Mathematical functions for statistics that allow missing values without scotching the calculations.
 *
 * <p>Be careful because some methods from cern.jet.stat.Descriptive have not been overridden and will
 * yield a UnsupportedOperationException if used!</p>
 *
 * <p>Some functions that come with DoubleArrayLists will not work in an entirely compatible way
 * with missing values. For examples, size() reports the total number of elements, including missing values.
 * To get a count of non-missing values, use this.sizeWithoutMissingValues(). The right one to use
 * may vary, so be careful.</p>
 *
 * <p>Not all methods need to be overridden. However, all methods that take a "size" parameter should
 * be passed the results of sizeWithoutMissingValues(data), instead of data.size().</p>
 *
 *  <p>Copyright � 2004 Columbia University
 * <p>Based in part on code from the colt package:
 * Copyright � 1999 CERN - European Organization for Nuclear Research.
 * @see <a href="http://hoschek.home.cern.ch/hoschek/colt/V1.0.3/doc/cern/jet/stat/Descriptive.html">cern.jet.stat.Descriptive</a>
 * @author Paul Pavlidis
 * @version $Id$
 */
public class DescriptiveWithMissing
    extends cern.jet.stat.Descriptive {

   private DescriptiveWithMissing() {}

   /**
    * <b>Not supported.</b>
    *
    * @param data DoubleArrayList
    * @param lag int
    * @param mean double
    * @param variance double
    * @return double
    */
   public static double autoCorrelation( DoubleArrayList data, int lag,
                                         double mean, double variance ) {
      throw new UnsupportedOperationException( "autoCorrelation not supported with missing values" );
   }

   /**
    * Returns the correlation of two data sequences. That is
    * <tt>covariance(data1,data2)/(standardDev1*standardDev2)</tt>. Missing values are ignored.
    *
    * This method is overridden to stop users from using the method in the superclass when missing values are present.
    * The problem is that the standard deviation cannot be computed without knowning which values are not missing in both
    * vectors to be compared. Thus the standardDev parameters are thrown away by this method.
    *
    * @param data1 DoubleArrayList
    * @param standardDev1 double - not used
    * @param data2 DoubleArrayList
    * @param standardDev2 double - not used
    * @return double
    */
   public static double correlation( DoubleArrayList data1, double standardDev1,
                                     DoubleArrayList data2, double standardDev2 ) {
      return correlation( data1, data2 );
   }

   /**
    * Calculate the pearson correlation of two arrays. Missing values (NaNs) are
    * ignored.
    *
    * @param x DoubleArrayList
    * @param y DoubleArrayList
    * @return double
    */
   public static double correlation( DoubleArrayList x, DoubleArrayList y ) {
      int j;
      double syy, sxy, sxx, sx, sy, xj, yj, ay, ax;
      int numused;
      syy = 0.0;
      sxy = 0.0;
      sxx = 0.0;
      sx = 0.0;
      sy = 0.0;
      numused = 0;
      if ( x.size() != y.size() ) {
         throw new ArithmeticException();
      }

      int length = x.size();
      for ( j = 0; j < length; j++ ) {
         xj = x.elements()[j];
         yj = y.elements()[j];

         if ( !Double.isNaN( xj ) && !Double.isNaN( yj ) ) {
            sx += xj;
            sy += yj;
            sxy += xj * yj;
            sxx += xj * xj;
            syy += yj * yj;
            numused++;
         }
      }

      if ( numused > 0 ) {
         ay = sy / numused;
         ax = sx / numused;
         return ( sxy - sx * ay ) / Math.sqrt( ( sxx - sx * ax ) * ( syy - sy * ay ) );
      } else {
         return -2.0; // signifies that it could not be calculated.
      }
   }

   /**
    * Returns the covariance of two data sequences. Pairs of values are only
    * considered if both are not NaN. If there are no non-missing pairs, the covariance is zero.
    *
    * @param data1 the first vector
    * @param data2 the second vector
    * @return double
    */
   public static double covariance( DoubleArrayList data1,
                                    DoubleArrayList data2 ) {
      int size = data1.size();
      if ( size != data2.size() || size == 0 ) {
         throw new IllegalArgumentException();
      }
      double[] elements1 = data1.elements();
      double[] elements2 = data2.elements();

      /* initialize sumx and sumy to the first non-NaN pair of values */

      int i = 0;
      double sumx = 0.0, sumy = 0.0, Sxy = 0.0;
      while ( i < size ) {
         sumx = elements1[i];
         sumy = elements2[i];
         if ( !Double.isNaN( elements1[i] ) && !Double.isNaN( elements2[i] ) ) {
            break;
         }
         i++;
      }
      i++;
      int usedPairs = 1;
      for ( ; i < size; ++i ) {
         double x = elements1[i];
         double y = elements2[i];
         if ( Double.isNaN( x ) || Double.isNaN( y ) ) {
            continue;
         }

         sumx += x;
         Sxy += ( x - sumx / ( usedPairs + 1 ) ) * ( y - sumy / usedPairs );
         sumy += y;
         usedPairs++;
      }
      return Sxy / usedPairs;
   }

   /**
    * Durbin-Watson computation.
    * This measures the serial correlation in a data series.
    *
    * @param data DoubleArrayList
    * @return double
    * @todo this will still break in some situations where there are missing values
    */
   public static double durbinWatson( DoubleArrayList data ) {
      int size = data.size();
      if ( size < 2 ) {
         throw new IllegalArgumentException(
             "data sequence must contain at least two values." );
      }

      double[] elements = data.elements();
      double run = 0;
      double run_sq = 0;
      int firstNotNaN = 0;
      while ( firstNotNaN < size ) {
         run_sq = elements[firstNotNaN] * elements[firstNotNaN];

         if ( !Double.isNaN( elements[firstNotNaN] ) ) {
            break;
         }

         firstNotNaN++;
      }

      if ( firstNotNaN > 0 && size - firstNotNaN < 2 ) {
         throw new IllegalArgumentException(
             "data sequence must contain at least two non-missing values." );

      }

      for ( int i = firstNotNaN + 1; i < size; i++ ) {
         int gap = 1;
         while ( Double.isNaN( elements[i] ) ) {
            gap++;
            i++;
            continue;
         }
         double x = elements[i] - elements[i -
             gap];
         /**  */
         run += x * x;
         run_sq += elements[i] * elements[i];
      }

      return run / run_sq;
   }

   /**
    * Returns the geometric mean of a data sequence. Missing values are ignored.
    * Note that for a geometric mean to be meaningful, the minimum of the data
    * sequence must not be less or equal to zero.
    * <br> The geometric mean is given by <tt>pow( Product( data[i] ),
    * 1/data.size())</tt>. This method tries to avoid overflows at the expense
    * of an equivalent but somewhat slow definition: <tt>geo = Math.exp( Sum(
    * Log(data[i]) ) / data.size())</tt>.
    *
    * @param data DoubleArrayList
    * @return double
    */
   public static double geometricMean( DoubleArrayList data ) {
      return geometricMean( sizeWithoutMissingValues( data ),
                            sumOfLogarithms( data, 0, data.size() - 1 ) );
   }

   /**
    * <b>Not supported.</b>
    * @param data DoubleArrayList
    * @param from int
    * @param to int
    * @param inOut double[]
    */
   public static void incrementalUpdate( DoubleArrayList data, int from, int to, double[] inOut ) {
      throw new UnsupportedOperationException(
          "incrementalUpdate not supported with missing values" );
   }

   /**
    * <b>Not supported.</b>
    * @param data DoubleArrayList
    * @param from int
    * @param to int
    * @param fromSumIndex int
    * @param toSumIndex int
    * @param sumOfPowers double[]
    */
   public static void incrementalUpdateSumsOfPowers( DoubleArrayList data, int from, int to,
       int fromSumIndex, int toSumIndex, double[] sumOfPowers ) {
      throw new UnsupportedOperationException(
          "incrementalUpdateSumsOfPowers not supported with missing values" );
   }

   /**
    *<b>Not supported.</b>
    * @param data DoubleArrayList
    * @param weights DoubleArrayList
    * @param from int
    * @param to int
    * @param inOut double[]
    */
   public static void incrementalWeightedUpdate( DoubleArrayList data, DoubleArrayList weights,
                                                 int from, int to, double[] inOut ) {
      throw new UnsupportedOperationException(
          "incrementalWeightedUpdate not supported with missing values" );
   }

   /**
    * Returns the kurtosis (aka excess) of a data sequence.
    *
    * @param moment4 the fourth central moment, which is
    *   <tt>moment(data,4,mean)</tt>.
    * @param standardDeviation the standardDeviation.
    * @return double
    */
   public static double kurtosis( double moment4, double standardDeviation ) {
      return -3 +
          moment4 / ( standardDeviation * standardDeviation * standardDeviation * standardDeviation );
   }

   /**
    * Returns the kurtosis (aka excess) of a data sequence, which is <tt>-3 +
    * moment(data,4,mean) / standardDeviation<sup>4</sup></tt>.
    *
    * @param data DoubleArrayList
    * @param mean double
    * @param standardDeviation double
    * @return double
    */
   public static double kurtosis( DoubleArrayList data, double mean, double standardDeviation ) {
      return kurtosis( moment( data, 4, mean ), standardDeviation );
   }

   /**
    * <b>Not supported.</b>
    * @param data DoubleArrayList
    * @param mean double
    * @return double
    */
   public static double lag1( DoubleArrayList data, double mean ) {
      throw new UnsupportedOperationException( "lag1 not supported with missing values" );
   }

   /**
    * @param data Values to be analyzed.
    * @return Mean of the values in x. Missing values are ignored in the analysis.
    */
   public static double mean( DoubleArrayList data ) {
      return sum( data ) / sizeWithoutMissingValues( data );
   }

   /**
    * Special mean calculation where we use the effective size as an input.
    *
    * @param x The data
    * @param effectiveSize The effective size used for the mean calculation.
    * @return double
    */
   public static double mean( DoubleArrayList x, int effectiveSize ) {

      int length = x.size();

      if ( 0 == effectiveSize ) {
         return Double.NaN;
      }

      double sum = 0.0;
      int i, count;
      count = 0;
      double value;
      double[] elements = x.elements();
      for ( i = 0; i < length; i++ ) {
         value = elements[i];
         if ( Double.isNaN( value ) ) {
            continue;
         }
         sum += value;
         count++;
      }
      if ( 0.0 == count ) {
         return Double.NaN;
      } else {
         return sum /
             effectiveSize;
      }
   }

   /**
       * Special mean calculation where we use the effective size as an input.
       *
       * @param elements The data double array.
       * @param effectiveSize The effective size used for the mean calculation.
       * @return double
       */
      public static double mean( double[] elements, int effectiveSize ) {

         int length = elements.length;

         if ( 0 == effectiveSize ) {
            return Double.NaN;
         }

         double sum = 0.0;
         int i, count;
         count = 0;
         double value;
         for ( i = 0; i < length; i++ ) {
            value = elements[i];
            if ( Double.isNaN( value ) ) {
               continue;
            }
            sum += value;
            count++;
         }
         if ( 0.0 == count ) {
            return Double.NaN;
         } else {
            return sum /
                effectiveSize;
         }
      }

      /**
          * Calculate the mean of the values above a particular quantile of an array.
          *
          * @param quantile A value from 0 to 100
          * @param array Array for which we want to get the quantile.
          * @return double
          */
         public static double meanAboveQuantile( int quantile,
                                                 DoubleArrayList array ) {

            if ( quantile < 0 || quantile > 100 ) {
               throw new IllegalArgumentException( "Quantile must be between 0 and 100" );
            }

            double returnvalue = 0.0;
            int k = 0;

            double median = Descriptive.quantile( array, ( double ) quantile );

            for ( int i = 0; i < array.size(); i++ ) {
               if ( array.get( i ) >= median ) {
                  returnvalue += array.get( i );
                  k++;
               }
            }

            if ( k == 0 ) {
               throw new ArithmeticException( "No values found above quantile" );
            }

            return ( returnvalue / k );
         }



   /**
    * Returns the median of a sorted data sequence. Missing values are not
    * considered.
    *
    * @param sortedData the data sequence; <b>must be sorted ascending</b>.
    * @return double
    */
   public static double median( DoubleArrayList sortedData ) {
      return quantile( sortedData, 0.5 );
   }

   /**
    * Returns the moment of <tt>k</tt>-th order with constant <tt>c</tt> of a
    * data sequence, which is <tt>Sum( (data[i]-c)<sup>k</sup> ) /
    * data.size()</tt>.
    *
    * @param data DoubleArrayList
    * @param k int
    * @param c double
    * @return double
    */
   public static double moment( DoubleArrayList data, int k, double c ) {
      return sumOfPowerDeviations( data, k, c ) / sizeWithoutMissingValues( data );
   }

   /**
    * Returns the product of a data sequence, which is <tt>Prod( data[i] )</tt>.
    * Missing values are ignored. In other words:
    * <tt>data[0]*data[1]*...*data[data.size()-1]</tt>. Note that you may easily
    * get numeric overflows.
    *
    * @param data DoubleArrayList
    * @return double
    */
   public static double product( DoubleArrayList data ) {
      int size = data.size();
      double[] elements = data.elements();

      double product = 1;
      for ( int i = size; --i >= 0; ) {
         if ( Double.isNaN( elements[i] ) ) {
            continue;
         }
         product *= elements[i];

      }
      return product;
   }

   /**
    * Returns the <tt>phi-</tt>quantile; that is, an element <tt>elem</tt> for
    * which holds that <tt>phi</tt> percent of data elements are less than
    * <tt>elem</tt>. Missing values are ignored. The quantile need not
    * necessarily be contained in the data sequence, it can be a linear
    * interpolation.
    *
    * @param sortedData the data sequence; <b>must be sorted ascending</b>.
    * @param phi the percentage; must satisfy <tt>0 &lt;= phi &lt;= 1</tt>.
    * @todo possibly implement so a copy is not made.
    * @return double
    */
   public static double quantile( DoubleArrayList sortedData, double phi ) {
      return Descriptive.quantile( removeMissing( sortedData ), phi );
   }

   /**
    * Returns how many percent of the elements contained in the receiver are <tt>&lt;= element</tt>.
    * Does linear interpolation if the element is not contained but lies in between two contained elements.
    * Missing values are ignored.
    *
    * @param sortedList the list to be searched (must be sorted ascending).
    * @param element the element to search for.
    * @return the percentage <tt>phi</tt> of elements <tt>&lt;= element</tt> (<tt>0.0 &lt;= phi &lt;= 1.0)</tt>.
    */
   public static double quantileInverse( DoubleArrayList sortedList, double element ) {
      return rankInterpolated( sortedList, element ) / sortedList.size();
   }

   /**
    * Returns the quantiles of the specified percentages.
    * The quantiles need not necessarily be contained in the data sequence, it can be a linear interpolation.
    * @param sortedData the data sequence; <b>must be sorted ascending</b>.
    * @param percentages the percentages for which quantiles are to be computed.
    * Each percentage must be in the interval <tt>[0.0,1.0]</tt>.
    * @return the quantiles.
    */
   public static DoubleArrayList quantiles( DoubleArrayList sortedData, DoubleArrayList percentages ) {
      int s = percentages.size();
      DoubleArrayList quantiles = new DoubleArrayList( s );

      for ( int i = 0; i < s; i++ ) {
         quantiles.add( quantile( sortedData, percentages.get( i ) ) );
      }

      return quantiles;
   }

   /**
    * Returns the linearly interpolated number of elements in a list less or equal to a given element. Missing values are ignored.
    * The rank is the number of elements <= element.
    * Ranks are of the form <tt>{0, 1, 2,..., sortedList.size()}</tt>.
    * If no element is <= element, then the rank is zero.
    * If the element lies in between two contained elements, then linear interpolation is used and a non integer value is returned.
    *
    * @param sortedList the list to be searched (must be sorted ascending).
    * @param element the element to search for.
    * @return the rank of the element.
    * @todo possibly implement so a copy is not made.
    */
   public static double rankInterpolated( DoubleArrayList sortedList, double element ) {
      return Descriptive.rankInterpolated( removeMissing( sortedList ), element );
   }

   /**
    * Returns the sample kurtosis (aka excess) of a data sequence.
    *
    * @param data DoubleArrayList
    * @param mean double
    * @param sampleVariance double
    * @return double
    */
   public static double sampleKurtosis( DoubleArrayList data, double mean, double sampleVariance ) {
      return sampleKurtosis( sizeWithoutMissingValues( data ), moment( data, 4, mean ),
                             sampleVariance );
   }

   /**
    * Returns the sample skew of a data sequence.
    *
    * @param data DoubleArrayList
    * @param mean double
    * @param sampleVariance double
    * @return double
    */
   public static double sampleSkew( DoubleArrayList data, double mean, double sampleVariance ) {
      return sampleSkew( sizeWithoutMissingValues( data ), moment( data, 3, mean ), sampleVariance );
   }

   /**
    * Returns the skew of a data sequence, which is <tt>moment(data,3,mean) /
    * standardDeviation<sup>3</sup></tt>.
    *
    * @param data DoubleArrayList
    * @param mean double
    * @param standardDeviation double
    * @return double
    */
   public static double skew( DoubleArrayList data, double mean, double standardDeviation ) {
      return skew( moment( data, 3, mean ), standardDeviation );
   }

   /**
    * Returns the sample variance of a data sequence. That is <tt>Sum (
    * (data[i]-mean)^2 ) / (data.size()-1)</tt>.
    *
    * @param data DoubleArrayList
    * @param mean double
    * @return double
    */
   public static double sampleVariance( DoubleArrayList data, double mean ) {
      double[] elements = data.elements();
      int effsize = sizeWithoutMissingValues( data );
      int size = data.size();
      double sum = 0;
      // find the sum of the squares
      for ( int i = size; --i >= 0; ) {
         if ( Double.isNaN( elements[i] ) ) {
            continue;
         }
         double delta = elements[i] - mean;
         sum += delta * delta;
      }

      return sum / ( effsize - 1 );
   }

   /**
    * Modifies a data sequence to be standardized. Mising values are ignored.
    * Changes each element <tt>data[i]</tt> as follows: <tt>data[i] = (data[i]-mean)/standardDeviation</tt>.
    *
    * @param data DoubleArrayList
    * @param mean mean of data
    * @param standardDeviation stdev of data
    */
   public static void standardize( DoubleArrayList data, double mean,
                                   double standardDeviation ) {
      double[] elements = data.elements();
      for ( int i = data.size(); --i >= 0; ) {
         if ( Double.isNaN( elements[i] ) ) {
            continue;
         }
         elements[i] = ( elements[i] - mean ) / standardDeviation;
      }
   }

   /**
    * Standardize
    *
    * @param data DoubleArrayList
    */
   public static void standardize( DoubleArrayList data ) {
      int effsize = DescriptiveWithMissing.sizeWithoutMissingValues( data );
      double sumsq = DescriptiveWithMissing.sumOfSquares( data );
      double sum = DescriptiveWithMissing.sum( data );
      double variance = DescriptiveWithMissing.variance( effsize, sum, sumsq );
      double mean = DescriptiveWithMissing.mean( data );
      double stdev = DescriptiveWithMissing.standardDeviation( variance );
      DescriptiveWithMissing.standardize( data, mean, stdev );
   }

   /**
    * Returns the sum of a data sequence. That is <tt>Sum( data[i] )</tt>.
    *
    * @param data DoubleArrayList
    * @return double
    */
   public static double sum( DoubleArrayList data ) {
      return sumOfPowerDeviations( data, 1, 0.0 );
   }

   /**
    * Returns the sum of inversions of a data sequence, which is <tt>Sum( 1.0 /
    * data[i])</tt>.
    *
    * @param data the data sequence.
    * @param from the index of the first data element (inclusive).
    * @param to the index of the last data element (inclusive).
    * @return double
    */
   public static double sumOfInversions( DoubleArrayList data, int from, int to ) {
      return sumOfPowerDeviations( data, -1, 0.0, from, to );
   }

   /**
    * Returns the sum of logarithms of a data sequence, which is <tt>Sum(
    * Log(data[i])</tt>. Missing values are ignored.
    *
    * @param data the data sequence.
    * @param from the index of the first data element (inclusive).
    * @param to the index of the last data element (inclusive).
    * @return double
    */
   public static double sumOfLogarithms( DoubleArrayList data, int from, int to ) {
      double[] elements = data.elements();
      double logsum = 0;
      for ( int i = from - 1; ++i <= to; ) {
         if ( Double.isNaN( elements[i] ) ) {
            continue;
         }
         logsum += Math.log( elements[i] );
      }
      return logsum;
   }

   /**
    * Returns the sum of powers of a data sequence, which is <tt>Sum (
    * data[i]<sup>k</sup> )</tt>.
    *
    * @param data DoubleArrayList
    * @param k int
    * @return double
    */
   public static double sumOfPowers( DoubleArrayList data, int k ) {
      return sumOfPowerDeviations( data, k, 0 );
   }

   /**
    * Returns the sum of squares of a data sequence. Skips missing values.
    *
    * @param data DoubleArrayList
    * @return double
    */
   public static double sumOfSquares( DoubleArrayList data ) {
      return sumOfPowerDeviations( data, 2, 0.0 );
   }

   /**
    * Compute the sum of the squared deviations from the mean of a data sequence. Missing values are ignored.
    * @param data DoubleArrayList
    * @return double
    */
   public static double sumOfSquaredDeviations( DoubleArrayList data ) {
      return sumOfSquaredDeviations( sizeWithoutMissingValues( data ),
                                     variance( sizeWithoutMissingValues( data ),
                                               sum( data ),
                                               sumOfSquares( data ) ) );
   }

   /**
    * Returns <tt>Sum( (data[i]-c)<sup>k</sup> )</tt>; optimized for common
    * parameters like <tt>c == 0.0</tt> and/or <tt>k == -2 .. 4</tt>.
    *
    * @param data DoubleArrayList
    * @param k int
    * @param c double
    * @return double
    */
   public static double sumOfPowerDeviations( DoubleArrayList data, int k, double c ) {
      return sumOfPowerDeviations( data, k, c, 0, data.size() - 1 );
   }

   /**
    * Returns <tt>Sum( (data[i]-c)<sup>k</sup> )</tt> for all <tt>i = from ..
    * to</tt>; optimized for common parameters like <tt>c == 0.0</tt> and/or
    * <tt>k == -2 .. 5</tt>. Missing values are ignored.
    *
    * @param data DoubleArrayList
    * @param k int
    * @param c double
    * @param from int
    * @param to int
    * @return double
    */
   public static double sumOfPowerDeviations( final DoubleArrayList data,
                                              final int k, final double c,
                                              final int from, final int to ) {
      final double[] elements = data.elements();
      double sum = 0;
      double v;
      int i;
      switch ( k ) { // optimized for speed
         case -2:
            if ( c == 0.0 ) {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i];
                  sum += 1 / ( v * v );
               }
            } else {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i] - c;
                  sum += 1 / ( v * v );
               }
            }
            break;
         case -1:
            if ( c == 0.0 ) {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  sum += 1 / ( elements[i] );
               }
            } else {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  sum += 1 / ( elements[i] - c );
               }
            }
            break;
         case 0:
            sum += to - from + 1;
            break;
         case 1:
            if ( c == 0.0 ) {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  sum += elements[i];
               }
            } else {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  sum += elements[i] - c;
               }
            }
            break;
         case 2:
            if ( c == 0.0 ) {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i];
                  sum += v * v;
               }
            } else {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i] - c;
                  sum += v * v;
               }
            }
            break;
         case 3:
            if ( c == 0.0 ) {
               for ( i = from - 1; ++i <= to; ) {
                  v = elements[i];
                  sum += v * v * v;
               }
            } else {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i] - c;
                  sum += v * v * v;
               }
            }
            break;
         case 4:
            if ( c == 0.0 ) {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i];
                  sum += v * v * v * v;
               }
            } else {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i] - c;
                  sum += v * v * v * v;
               }
            }
            break;
         case 5:
            if ( c == 0.0 ) {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i];
                  sum += v * v * v * v * v;
               }
            } else {
               for ( i = from - 1; ++i <= to; ) {
                  if ( Double.isNaN( elements[i] ) ) {
                     continue;
                  }
                  v = elements[i] - c;
                  sum += v * v * v * v * v;
               }
            }
            break;
         default:
            for ( i = from - 1; ++i <= to; ) {
               if ( Double.isNaN( elements[i] ) ) {
                  continue;
               }
               sum += Math.pow( elements[i] - c, k );
            }
            break;
      }
      return sum;
   }

   /**
    * Return the size of the list, ignoring missing values.
    * @param list DoubleArrayList
    * @return int
    */
   public static int sizeWithoutMissingValues( DoubleArrayList list ) {

      int size = 0;
      for ( int i = 0; i < list.size(); i++ ) {
         if ( !Double.isNaN( list.get( i ) ) ) {
            size++;
         }
      }
      return size;
   }

   /**
    * Returns the trimmed mean of a sorted data sequence. Missing values are
    * completely ignored.
    *
    * @param sortedData the data sequence; <b>must be sorted ascending</b>.
    * @param mean the mean of the (full) sorted data sequence.
    * @param left int the number of leading elements to trim.
    * @param right int number of trailing elements to trim.
    * @return double
    */
   public static double trimmedMean( DoubleArrayList sortedData, double mean,
                                     int left, int right ) {
      return Descriptive.trimmedMean( removeMissing( sortedData ), mean, left, right );
   }

   /**
    * Provided for convenience!
    * @param data DoubleArrayList
    * @return double
    */
   public static double variance( DoubleArrayList data ) {
      return variance(
          sizeWithoutMissingValues( data ),
          sum( data ),
          sumOfSquares( data ) );
   }

   /**
    * Returns the weighted mean of a data sequence. That is <tt> Sum (data[i] *
    * weights[i]) / Sum ( weights[i] )</tt>.
    *
    * @param data DoubleArrayList
    * @param weights DoubleArrayList
    * @return double
    */
   public static double weightedMean( DoubleArrayList data,
                                      DoubleArrayList weights ) {
      int size = data.size();
      if ( size != weights.size() || size == 0 ) {
         throw new IllegalArgumentException();
      }

      double[] elements = data.elements();
      double[] theWeights = weights.elements();
      double sum = 0.0;
      double weightsSum = 0.0;
      for ( int i = size; --i >= 0; ) {
         double w = theWeights[i];
         if ( Double.isNaN( elements[i] ) ) {
            continue;
         }
         sum += elements[i] * w;
         weightsSum += w;
      }

      return sum / weightsSum;
   }

   /**
    * <b>Not supported.</b>
    * @param sortedData DoubleArrayList
    * @param mean double
    * @param left int
    * @param right int
    * @return double
    */
   public static double winsorizedMean( DoubleArrayList sortedData, double mean, int left,
                                        int right ) {
      throw new UnsupportedOperationException( "winsorizedMean not supported with missing values" );
   }

   /* private methods */

   /**
    * Convenience function for internal use. Makes a copy of the list that doesn't have the missing values.
    * @param data DoubleArrayList
    * @return DoubleArrayList
    */
   private static DoubleArrayList removeMissing( DoubleArrayList data ) {
      DoubleArrayList r = new DoubleArrayList( sizeWithoutMissingValues( data ) );
      double[] elements = data.elements();
      int size = data.size();
      for ( int i = 0; i < size; i++ ) {
         if ( Double.isNaN( elements[i] ) ) {
            continue;
         }
         r.add( elements[i] );
      }
      return r;
   }

} // end of class
