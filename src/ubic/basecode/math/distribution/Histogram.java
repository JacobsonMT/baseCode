/*
 * The baseCode project
 * 
 * Copyright (c) 2008-2010 University of British Columbia
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
package ubic.basecode.math.distribution;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import org.jfree.data.xy.XYSeries;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.jet.stat.Descriptive;

/**
 * A simple histogram.
 * 
 * @author keshav
 * @version $Id$
 */
public class Histogram {
    private double[] hist;
    private String name;
    private double min;
    private double max;
    private int nbins;
    private int entries;
    private double overflow;
    private double underflow;

    /**
     * @param name
     * @param nbins
     * @param min
     * @param max
     */
    public Histogram( String name, int nbins, double min, double max ) {
        this.nbins = nbins;
        this.min = min;
        this.max = max;
        this.name = name;
        this.hist = new double[this.nbins];
        this.underflow = 0;
        this.overflow = 0;
    }

    /**
     * @param name
     * @param nbins
     * @param data
     */
    public Histogram( String name, int nbins, DoubleMatrix1D data ) {
        this.name = name;
        this.nbins = nbins;
        this.hist = new double[this.nbins];
        this.underflow = 0;
        this.overflow = 0;

        this.min = Descriptive.min( new DoubleArrayList( data.toArray() ) );
        this.max = Descriptive.max( new DoubleArrayList( data.toArray() ) );

        this.fill( data );

    }

    /**
     * Fill the histogram with x.
     * 
     * @param x is the value to add in to the histogram
     */
    public void fill( double x ) {
        // use findBin method to work out which bin x falls in
        BinInfo bin = findBin( x );
        // check the result of findBin in case it was an overflow or underflow
        if ( bin.isUnderflow ) {
            underflow++;
        }
        if ( bin.isOverflow ) {
            overflow++;
        }
        if ( bin.isInRange ) {
            hist[bin.index]++;
        }

        // count the number of entries made by the fill method
        entries++;
    }

    /**
     * @param x
     * @return the index of the bin where x would fall. If x is out of range you get 0 or (nbins - 1).
     */
    public int getBinOf( double x ) {
        if ( x < min ) {
            return 0;
        } else if ( x > max ) {
            return nbins - 1;
        } else {
            // search for histogram bin into which x falls FIXME make this faster with a binary search
            double binWidth = stepSize();
            for ( int i = 0; i < nbins; i++ ) {
                double highEdge = min + ( i + 1 ) * binWidth;
                if ( x <= highEdge ) {
                    return i;
                }
            }
            throw new IllegalStateException( "Failed to find the bin for " + x );
        }
    }

    /**
     * @return
     */
    public Double[] getBinEdges() {

        double step = this.min();
        double[] binHeights = this.getArray();

        Double[] result = new Double[binHeights.length];

        for ( int i = 0; i < binHeights.length; i++ ) {
            result[i] = step;
            step += this.stepSize();
        }

        return result;
    }

    public String[] getBinEdgesStrings() {

        double step = this.min();
        double[] binHeights = this.getArray();

        String[] result = new String[binHeights.length];

        for ( int i = 0; i < binHeights.length; i++ ) {
            result[i] = String.format( "%.3f", step );
            step += this.stepSize();
        }

        return result;
    }

    /**
     *
     */
    private static class BinInfo {
        public int index;
        public boolean isUnderflow;
        public boolean isOverflow;
        public boolean isInRange;
    }

    /**
     * Determines the bin for a number in the histogram.
     * 
     * @return info on which bin x falls in.
     */
    private BinInfo findBin( double x ) {
        BinInfo bin = new BinInfo();
        bin.isInRange = false;
        bin.isUnderflow = false;
        bin.isOverflow = false;
        // first check if x is outside the range of the normal histogram bins
        if ( x < min ) {
            bin.isUnderflow = true;
        } else if ( x > max ) {
            bin.isOverflow = true;
        } else {
            // search for histogram bin into which x falls FIXME make this faster with a binary search
            double binWidth = stepSize();
            for ( int i = 0; i < nbins; i++ ) {
                double highEdge = min + ( i + 1 ) * binWidth;
                if ( x <= highEdge ) {
                    bin.isInRange = true;
                    bin.index = i;
                    break;
                }
            }
        }
        return bin;

    }

    /**
     * @return size of each bin
     */
    public double stepSize() {
        return ( max - min ) / nbins;
    }

    /**
     * Provide graph for JFreePlot; counts expressed as a fraction.
     */
    public XYSeries plot() {
        XYSeries series = new XYSeries( this.name, true, true );

        double step = this.min();

        double binWidth = stepSize();

        double[] binHeights = this.getArray();
        for ( int i = 0; i < binHeights.length; i++ ) {
            series.add( step, binHeights[i] / entries );
            step += binWidth;
        }
        return series;
    }

    /**
     * Write the histogram to a file. The format is:
     * <p>
     * bin (lower edge), number in bin.
     * <p>
     * 
     * @param out
     * @throws IOException
     */
    public void writeToFile( FileWriter out ) throws IOException {

        DecimalFormat dfBin = new DecimalFormat( " ##0.00;-##0.00" );
        DecimalFormat dfCount = new DecimalFormat( " ##0" );

        double step = this.min();

        double binWidth = stepSize();

        double[] binHeights = this.getArray();

        for ( int i = 0; i < binHeights.length; i++ ) {

            String line = dfBin.format( step ) + "\t" + dfCount.format( binHeights[i] );
            if ( i < binHeights.length - 1 ) line += "\n";

            out.write( line );

            step += binWidth;

        }

        out.close();
    }

    /**
     * The number of entries in the histogram (the number of times fill has been called).
     * 
     * @return number of entries
     */
    public int entries() {
        return entries;
    }

    /**
     * The name of the histogram.
     * 
     * @return histogram name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the number of bins in the histogram. The range of the histogram defined by min and max, and the range is
     * divided into the number of returned.
     * 
     * @return number of bins
     */
    public int numberOfBins() {
        return nbins;
    }

    /**
     * @return minimum x value covered by histogram
     */
    public double min() {
        return min;
    }

    /**
     * @return maximum x value covered by histogram
     */
    public double max() {
        return max;
    }

    /**
     * The height of the overflow bin.
     * 
     * @return number of overflows
     */
    public double overflow() {
        return overflow;
    }

    /**
     * The height of the underflow bin.
     * 
     * @return number of underflows
     */
    public double underflow() {
        return underflow;
    }

    /**
     * Returns the bin heights.
     * 
     * @return array of bin heights
     */
    public double[] getArray() {
        return hist;
    }

    /**
     * @param ghr
     */
    public void fill( DoubleMatrix1D ghr ) {
        for ( int i = 0; i < ghr.size(); i++ ) {
            this.fill( ghr.getQuick( i ) );
        }

    }

    /**
     * @return the number of items in the bin that has the most members.
     */
    public int getBiggestBinSize() {
        int max = Integer.MIN_VALUE;
        for ( double d : hist ) {
            if ( d > max ) max = ( int ) d;
        }
        return max;
    }
}
