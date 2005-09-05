/*
 * The baseCode project
 * 
 * Copyright (c) 2005 Columbia University
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package baseCode.dataFilter;

import java.util.List;
import java.util.Vector;

import baseCode.dataStructure.matrix.DoubleMatrix2DNamedFactory;
import baseCode.dataStructure.matrix.DoubleMatrixNamed;
import baseCode.dataStructure.matrix.NamedMatrix;
import baseCode.math.DescriptiveWithMissing;
import baseCode.math.Stats;
import cern.colt.list.DoubleArrayList;
import cern.jet.stat.Descriptive;

/**
 * Remove rows from a matrix based on some row-based statistic. Rows with values too high and/or too low can be removed.
 * Thresholds are inclusive (i.e., values must be at least as high as the set threshold to be included. A number of
 * statistics are available. In addition, this filter can remove rows that have all negative data values.
 * <p>
 * There are a number of decisions/caveats to consider:
 * <h2>Cutpoint determination</h2>
 * <p>
 * There are multiple ways of determining cutpoints. Some possibilities are the maximum value, the minimum value, the
 * mean value, or the median value. The range and coefficient of variation are also included.
 * <p>
 * Note that if you want to use different methods for high-level filtering than for low-level filtering (e.g., using max
 * for the low-level, and min for the high-level, you have to filter twice. This could cause problems if you are using
 * fractional filtering and there are negative values (see below).
 * <h2>Filtering ratiometric data</h2>
 * <p>
 * For data that are normalized or ratios, it does not make sense to use this method on the raw data. In that situation,
 * you should filter the data based on the raw data, and then use a {@link RowNameFilter}to select the rows from the
 * ratio data.
 * <h2>Negative values</h2>
 * <p>
 * For microarray expression data based on the Affymetrix MAS4.0 protocol (and possibly others), negative values can
 * occur. In some cases all the values can be negative. As these values are generally viewed as nonsensical, one might
 * decide that data rows that are all negative should be filtered.
 * <h2>Behavior at extremes</h2>
 * <p>
 * If you request removal/inclusion of 1.0 of the data, you might not get the result you expect because the filtering is
 * inclusive.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * <p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class RowLevelFilter extends AbstractLevelFilter {

    private boolean removeAllNegative = false;

    /**
     * Use the minimum of the row as the criterion.
     */
    public static final int MIN = 1;

    /**
     * Use the maximum of the row as the criterion.
     */
    public static final int MAX = 2;

    /**
     * Use the median as the criterion.
     */
    public static final int MEDIAN = 3;

    /**
     * Use the mean as the criterion.
     */
    public static final int MEAN = 4;

    /**
     * Use the range as the criterion
     */
    public static final int RANGE = 5;

    /**
     * Use the coefficient of variation as the criterion
     */
    public static final int CV = 6;

    private int method = MAX;

    /**
     * Choose the method that will be used for filtering. Default is 'MAX'. Those rows with the lowest values are
     * removed during 'low' filtering.
     * 
     * @param method one of the filtering method constants.
     */
    public void setMethod( int method ) {
        if ( method != MIN && method != MAX && method != MEDIAN && method != MEAN && method != RANGE && method != CV ) {
            throw new IllegalArgumentException( "Unknown filtering method requested" );
        }
        this.method = method;
    }

    /**
     * Set the filter to remove all rows that have only negative values. This is applied BEFORE applying fraction-based
     * criteria. In other words, if you request filtering 0.5 of the values, and 0.5 have all negative values, you will
     * get 0.25 of the data back. Default = false.
     * 
     * @param t boolean
     */
    public void setRemoveAllNegative( boolean t ) {
        log.info( "Rows with all negative values will be " + "removed PRIOR TO applying fraction-based criteria." );
        removeAllNegative = t;
    }

    /**
     * @param data
     * @return
     */
    public NamedMatrix filter( NamedMatrix data ) {

        if ( !( data instanceof DoubleMatrixNamed ) ) {
            throw new IllegalArgumentException( "Only valid for DoubleMatrixNamed" );
        }

        if ( lowCut == -Double.MAX_VALUE && highCut == Double.MAX_VALUE ) {
            log.info( "No filtering requested" );
            return data;
        }

        int numRows = data.rows();
        int numCols = data.columns();

        DoubleArrayList criteria = new DoubleArrayList( new double[numRows] );

        /*
         * compute criteria.
         */
        DoubleArrayList rowAsList = new DoubleArrayList( new double[numCols] );
        int numAllNeg = 0;
        for ( int i = 0; i < numRows; i++ ) {
            Double[] row = ( Double[] ) data.getRowObj( i );
            int numNeg = 0;
            /* stupid, copy into a DoubleArrayList so we can do stats */
            for ( int j = 0; j < numCols; j++ ) {
                double item = row[j].doubleValue();
                rowAsList.set( j, item );
                if ( item < 0.0 || Double.isNaN( item ) ) {
                    numNeg++;
                }
            }
            if ( numNeg == numCols ) {
                numAllNeg++;
            }

            switch ( method ) {
                case MIN: {
                    criteria.set( i, Descriptive.min( rowAsList ) );
                    break;
                }
                case MAX: {
                    criteria.set( i, Descriptive.max( rowAsList ) );
                    break;
                }
                case MEAN: {
                    criteria.set( i, DescriptiveWithMissing.mean( rowAsList ) );
                    break;
                }
                case MEDIAN: {
                    criteria.set( i, DescriptiveWithMissing.median( rowAsList ) );
                    break;
                }
                case RANGE: {
                    criteria.set( i, Stats.range( rowAsList ) );
                    break;
                }
                case CV: {
                    criteria.set( i, Stats.cv( rowAsList ) );
                    break;
                }
                default: {
                    break;
                }
            }
        }

        DoubleArrayList sortedCriteria = criteria.copy();
        sortedCriteria.sort();

        double realLowCut = -Double.MAX_VALUE;
        double realHighCut = Double.MAX_VALUE;
        int consideredRows = numRows;
        int startIndex = 0;
        if ( removeAllNegative ) {
            consideredRows = numRows - numAllNeg;
            startIndex = numAllNeg;
        }

        if ( useHighAsFraction ) {
            if ( !Stats.isValidFraction( highCut ) ) {
                throw new IllegalStateException( "High level cut must be a fraction between 0 and 1" );
            }
            int thresholdIndex = 0;
            thresholdIndex = ( int ) Math.ceil( consideredRows * ( 1.0 - highCut ) ) - 1;

            thresholdIndex = Math.max( 0, thresholdIndex );
            realHighCut = sortedCriteria.get( thresholdIndex );
        } else {
            realHighCut = highCut;
        }

        if ( useLowAsFraction ) {
            if ( !Stats.isValidFraction( lowCut ) ) {
                throw new IllegalStateException( "Low level cut must be a fraction between 0 and 1" );
            }

            int thresholdIndex = 0;
            thresholdIndex = startIndex + ( int ) Math.floor( consideredRows * lowCut );
            thresholdIndex = Math.min( numRows - 1, thresholdIndex );
            realLowCut = sortedCriteria.get( thresholdIndex );
        } else {
            realLowCut = lowCut;
        }

        // go back over the data now using the cutpoints. This is not optimally
        // efficient.
        int kept = 0;
        List rowsToKeep = new Vector();
        List rowNames = new Vector();

        for ( int i = 0; i < numRows; i++ ) {
            if ( criteria.get( i ) >= realLowCut && criteria.get( i ) <= realHighCut ) {
                kept++;
                rowsToKeep.add( data.getRowObj( i ) );
                rowNames.add( data.getRowName( i ) );
            }
        }

        DoubleMatrixNamed returnval = DoubleMatrix2DNamedFactory.fastrow( rowsToKeep.size(), numCols );
        for ( int i = 0; i < kept; i++ ) {
            Double[] row = ( Double[] ) rowsToKeep.get( i );
            for ( int j = 0; j < numCols; j++ ) {
                returnval.set( i, j, row[j].doubleValue() );
            }
        }
        returnval.setColumnNames( data.getColNames() );
        returnval.setRowNames( rowNames );

        log.info( "There are " + kept + " rows left after filtering." );

        return ( returnval );

    }
}