package baseCode.Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * <p>Title: JMatrixDisplay</p>
 * <p>Description: a visual component for displaying a color matrix</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Institution:: Columbia University</p>
 * @author Will Braynen
 * @version $Id$
 */
public class JMatrixDisplay extends JPanel {

  // data fields
  ColorMatrix m_matrix;

  protected boolean m_isShowLabels = false;
  protected BufferedImage m_image = null;

  protected int m_ratioWidth = 0;
  protected int m_rowNameWidth;
  protected int m_labelGutter = 5;
  protected Font m_labelFont = null;
  protected int m_fontSize = 10;
  protected final int m_maxFontSize = 10;
  protected final int m_defaultResolution  = 120;
  protected int m_resolution = m_defaultResolution;
  protected int m_textSize = 0;

  /** Cell height in pixels; if printing row names, minimum recommended height is 10 pixels */
  protected static int m_cellHeight = 10; // in pixels
  /** Cell width in pixels; if printing column names, minimum recommended width is 10 pixels */
  protected static int m_cellWidth = 10; // in pixels


  public JMatrixDisplay( ColorMatrix matrix ) {

     m_matrix = matrix;
     setFont();
     m_rowNameWidth = m_labelGutter + maxStringPixelWidth( matrix.getRowNames(), m_labelFont, this );
     //m_rowNameWidth += m_labelGutter; // this is optional (leaves some space on the right)
     initSize();
  }

  /**
   * Sets the display sizes based on the microarraySetView
   */
  protected void initSize() {

    if (m_matrix != null)
    {
      int height = m_cellHeight * m_matrix.getRowCount();
      int width  = m_cellWidth  * m_matrix.getColumnCount();

      if (m_isShowLabels)
      {
          width += m_rowNameWidth;
      }

      Dimension d = new Dimension( width, height );
      setMinimumSize( d );
      setPreferredSize( d );
      setSize( d );
      this.revalidate();
    }
  }

  /**
   * <code>JComponent</code> method used to render this component
   * @param g Graphics used for painting
   */
  protected void paintComponent( Graphics g ) {

    super.paintComponent(g);
    drawDisplay( g, m_matrix );

  } // end paintComponent

  /**
   * Gets called from #paintComponent and #saveToFile
   */
  protected void drawDisplay( Graphics g, ColorMatrix matrix ) {

     if (matrix != null)
     {
        g.setColor(Color.white);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        int fontGutter = (int) ( (double) m_cellHeight * .22);
        int rowCount = matrix.getRowCount();
        int columnCount = matrix.getColumnCount();

//        // TO DO: print column names vertically
//        if (m_isShowLabels && columnCount > 0)
//        {
//           for (int j = 0;  j < columnCount;  j++)
//           {           
//              int x = (j * m_cellWidth);
//              int y = columnNamesHeight;
//
//              g.setColor(Color.black);
//              g.setFont(m_labelFont);
//              int xRatio = (columnCount * m_cellWidth) + fontGutter;
//              int yRatio = y + m_cellHeight - m_labelGutter;
//              String columnName = matrix.getColumnName(j);
//              if (null == columnName) {
//                columnName = "Undefined";
//              }
//              g.drawString( "" + j, x, y );           
//           }
//        } // end printing column names
        
        // loop through the matrix, one row at a time
        for (int i = 0;  i < rowCount;  i++)
        {
           int y = (i * m_cellHeight); // + columnNamesHeight

           // draw an entire row, one cell at a time
           for (int j = 0; j < columnCount; j++)
           {
              int x = (j * m_cellWidth);
              int width = ( (j + 1) * m_cellWidth) - x;

              Color color = matrix.getColor(i, j);
              g.setColor(color);
              g.fillRect(x, y, width, m_cellHeight);
           }
           
           // print row names
           if (m_isShowLabels && columnCount > 0)
           {
              g.setColor(Color.black);
              g.setFont(m_labelFont);
              int xRatio = (columnCount * m_cellWidth) + m_labelGutter;
              int yRatio = y + m_cellHeight - fontGutter;
              String rowName = matrix.getRowName(i);
              rowName = rowName.trim();  // remove leading and trailing whitespace
              if (null == rowName) {
                 rowName = "Undefined";
              }
              g.drawString(rowName, xRatio, yRatio);
           } // end printing row names
        } // end looping through the matrix, one row at a time
     } // end if (matrix != null)
  } // end drawDisplay

  /**
   * ----------- SHOULD PROBABLY NOT BE IN THIS CLASS -----------
   *
   * @return  the pixel width of the string for the specified font.
   */
  public static int stringPixelWidth( String s, Font font, Component c ) {
     
     FontMetrics fontMetrics = c.getFontMetrics( font );
     return fontMetrics.charsWidth( s.toCharArray(), 0, s.length() );
     
  } // end stringPixelWidth
  
  /**
   * ----------- SHOULD PROBABLY NOT BE IN THIS CLASS -----------
   */
  public static int maxStringPixelWidth( String[] strings, Font font, Component c ) {
     
     // the number of chars in the longest string
     int maxWidth = 0;
     int width;
     String s;
     for (int i = 0;  i < strings.length;  i++)
     {
        s = strings[i];
        s.trim();  // remove leading and trailing whitespace
        width = stringPixelWidth( s, font, c );
        if (maxWidth < width)
           maxWidth = width;
     }
     
     return maxWidth;
     
  } // end getMaxPixelWidth
  
  
  /**
   * Sets the font used for drawing text
   */
  private void setFont() {
     int fontSize =
         Math.min(getFontSize(),
                 (int)( (double) m_maxFontSize /
                        (double) m_defaultResolution * (double) m_resolution) );
    if((fontSize != m_fontSize) || (m_labelFont == null))
    {
       m_fontSize  = fontSize;
       m_labelFont = new Font("Ariel", Font.PLAIN, m_fontSize);
    }
  }

  /**
   * @return  the height of the font
   */
  private int getFontSize() {
     return Math.max( m_cellHeight, 5 );
  }

  /**
   * Saves screenshot to file
   */
  public void saveScreenshotToFile( String outPngFilename ) throws java.io.IOException {

     Graphics2D g = null;
     m_image = new BufferedImage(this.getWidth(),
                                 this.getHeight(),
                                 BufferedImage.TYPE_INT_RGB);
     g = m_image.createGraphics();

     drawDisplay( g, m_matrix );

      ImageIO.write( m_image, "png", new File( outPngFilename ));
  }

  /**
   * If this display component has already been added to the GUI,
   * it will be resized to fit or exclude the row names
   */
  public void showLabels( boolean isShowLabels ) {
     m_isShowLabels = isShowLabels;
     initSize();
  }

  public ColorMatrix getMatrix() {
     return m_matrix;
  }

  /**
   * @param  matrix  the new matrix to use;  will resize
   *                 this display component as necessary
   */
  public void setMatrix( ColorMatrix matrix ) {
     m_matrix = matrix;
     initSize();
  }
}
