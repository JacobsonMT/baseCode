/*
 * MicroarraySet.java
 *
 * Created on May 27, 2004, 9:59 PM
 */

package baseCode.Gui;

import java.awt.Color;
import baseCode.dataStructure.reader.DoubleMatrixReader;
import baseCode.dataStructure.DenseDoubleMatrix2DNamed;
import java.net.URL;

/**
 *
 * @author  Will
 */
public class ColorMatrix {

    // data fields
    Color[][] m_colors;
    Color[] m_colorPalette;
    final int m_suggestedNumberOfColors = 64;

    DenseDoubleMatrix2DNamed m_matrix;
    DoubleMatrixReader m_matrixReader;

    double m_minValue, m_maxValue;
    int m_totalRows, m_totalColumns;

    // colors and color maps
    Color m_missingColor = Color.lightGray;
    final Color DARK_RED = new Color( 128, 0, 0 );
    Color m_minColor = Color.green; // default
    Color m_maxColor = Color.red;   // default
    Color[] m_customColorMap    = { m_minColor, m_maxColor };
    final Color[] GREENRED_COLORMAP  = { Color.green, Color.black, Color.red   };
    final Color[] REDGREEN_COLORMAP  = { Color.red,   Color.black, Color.green };
    final Color[] BLACKBODY_COLORMAP = { Color.black, DARK_RED, Color.orange, Color.yellow, Color.white };
    Color[] m_currentColorMap = GREENRED_COLORMAP; // reference to a color map

    public ColorMatrix() {

        this( "C:\\3_missing-values.txt", false );
    }

    /**
     * @param  filename  either an absolute path, or if providing a relative
     *                   path (e.g. data.txt), then keep in mind that it will
     *                   be relative to the java interpreter, not the class
     *                   (not my fault -- that's how java treats relative paths)
     */
    public ColorMatrix( String filename, boolean normalize ) {

        DenseDoubleMatrix2DNamed matrix = loadFile( filename );
        loadMatrix( matrix, normalize );
    }
    
    public ColorMatrix( DenseDoubleMatrix2DNamed matrix, boolean normalize ) {

        loadMatrix( matrix, normalize );
    }

    /**
     * Calculate how fast we have to change color components.
     * Assume min and max colors are different!
     *
     * @param  minColor  red, green, or blue component of the RGB color
     * @param  maxColor  red, green, or blue component of the RGB color
     * @return  positive or negative step size
     */
    int getStepSize( int minColor, int maxColor, int totalColors ) {

        int colorRange = maxColor - minColor;
        double stepSize = colorRange / ( 1 == totalColors ? 1 : totalColors - 1 );
        return (int)Math.round( stepSize );
    }

    /**
     * Allocates colors across a range.
     *
     * @param suggestedNumberOfColors  palette resolution; if colorPalette.length
     *        does not evenly divide into this number, the actual number of
     *        colors in the palette will be rounded down.
     * @param colorPalette  the simplest color map is { minColor, maxColor };
     *                  you might, however, want to go through intermediate
     *                  colors instead of following a straight-line route
     *                  through the color space.
     * @return Color[]  the color palette
     */
    Color[] initColorPalette( int suggestedNumberOfColors, Color[] colorPalette ) {

        Color minColor;
        Color maxColor;

        // number of segments is one less than the number of points
        // dividing the line into segments;  the color map contains points,
        // not segments, so for example, if the color map is trivially
        // { minColor, maxColor }, then there is only one segment
        int totalSegments = m_currentColorMap.length - 1;

        // allocate colors across a range; distribute evenly
        // between intermediate points, if there are any
        int colorsPerSegment = suggestedNumberOfColors / totalSegments;

        // make sure all segments are equal by rounding down
        // the total number of colors if necessary
        int totalColors = totalSegments * colorsPerSegment;

        // create color map to return
        colorPalette = new Color[totalColors];

        for (int segment = 0;  segment < totalSegments;  segment++)
        {
           // the minimum color for each segment as defined by the current color map
           minColor = m_currentColorMap[segment];
           int r = minColor.getRed();
           int g = minColor.getGreen();
           int b = minColor.getBlue();

           // the maximum color for each segment and the step sizes
           maxColor = m_currentColorMap[segment + 1];
           int redStepSize   = getStepSize( r, maxColor.getRed(),   colorsPerSegment );
           int greenStepSize = getStepSize( g, maxColor.getGreen(), colorsPerSegment );
           int blueStepSize  = getStepSize( b, maxColor.getBlue(),  colorsPerSegment );

           for (int k, i=0;  i < colorsPerSegment;  i++)
           {
               // clip
               r = Math.min( r, 255 );
               g = Math.min( g, 255 );
               b = Math.min( b, 255 );

               // but also make sure it's not less than zero
               r = Math.max( r, 0 );
               g = Math.max( g, 0 );
               b = Math.max( b, 0 );

               k = segment * colorsPerSegment + i;
               colorPalette[k] = new Color( r, g, b );

               r += redStepSize;
               g += greenStepSize;
               b += blueStepSize;
           }
        }

        return colorPalette;

    } // end initColorPalette


    public void initColors()
    {
        m_colorPalette = initColorPalette( m_suggestedNumberOfColors, m_currentColorMap );
        double range = m_maxValue - m_minValue;
        if (0.0 == range)
        {
            System.err.println( "Warning: range of values in data is zero." );
            range = 1.0; // This avoids getting a step size of zero
                         // in case all values in the matrix are equal.
        }

        // map values to colors
        for (int row = 0;  row < m_totalRows;  row++)
        {
            for (int column = 0;  column < m_totalColumns;  column++)
            {
                double value = m_matrix.get( row, column );
                
                if (Double.isNaN (value))
                {
                    // the value is missing
                    m_colors[row][column] = m_missingColor;
                }
                else
                {
                    // if values can be less than zero, shift them up
                    // to the range [0, maxValue + minValue]
                    if (m_minValue < 0)
                    {
                        value += Math.abs( m_minValue );
                    }
                    // normalize the values to the range [0, totalColors]
                    int i = (int)(( (m_colorPalette.length-1) / range ) * value );
                    m_colors[row][column] = m_colorPalette[i];
                }
            }
        }
    } // end initColors


    public int getRowCount() {

        return m_totalRows;
    }

    public int getColumnCount() {

        return m_totalColumns;
    }

    public Color getColor( int row, int column ) {

        return m_colors[row][column];
    }

    public String getRowName( int i ) {

        return m_matrix.getRowName( i );
    }

    /**
     * Changes values in a row, clipping if there are more values than columns.
     *
     * @param  row     row whose values we want to change
     * @param  values  new row values
     */
    protected void setRow( int row, double values[] ) {
        
        // clip if we have more values than columns
        int totalValues = Math.min( values.length, m_totalColumns );
                 
        for (int column = 0;  column < totalValues;  column++)
            m_matrix.set( row, column, values[column] );
        
    } // end setRow
    
    public void loadMatrix( DenseDoubleMatrix2DNamed matrix, boolean normalize ) {

        m_matrix = matrix;
        m_totalRows = m_matrix.rows();
        m_totalColumns = m_matrix.columns();
        m_colors = new Color[m_totalRows][m_totalColumns];

        // normalize the data
        if (normalize)
        {
            // normalize the data in each row
            for (int r = 0;  r < m_totalRows;  r++)
            {
                double[] rowValues = matrix.getRow( r );
                cern.colt.list.DoubleArrayList doubleArrayList = new cern.colt.list.DoubleArrayList( rowValues );
                doubleArrayList = baseCode.Math.Stats.standardize( doubleArrayList );
                rowValues = doubleArrayList.elements();
                setRow( r, rowValues );
            }
            
            // ??? - we normalized to variance one, mean zero, so shouldn't this be true:
            m_minValue = -1;
            m_maxValue = 1;
        }
        //else
        {
            // compute min and max values in the matrix
            m_minValue = m_maxValue = m_matrix.get( 0, 0 );
            for (int r = 0;  r < m_totalRows;  r++)
            {
                for (int c = 0;  c < m_totalColumns;  c++)
                {
                    double value = m_matrix.get( r, c );
                    m_minValue = (m_minValue > value ? value : m_minValue);
                    m_maxValue = (m_maxValue < value ? value : m_maxValue);
                }
            }
        }

        // map values to colors
        initColors();
    }

    public DenseDoubleMatrix2DNamed loadFile( String filename ) {

        m_matrixReader = new DoubleMatrixReader();
        DenseDoubleMatrix2DNamed matrix = (DenseDoubleMatrix2DNamed) m_matrixReader.read( filename );
        return matrix;
    }
}
