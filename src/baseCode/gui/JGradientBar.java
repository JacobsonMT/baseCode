package baseCode.gui;

import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JLabel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Dimension;
import java.text.DecimalFormat;
import baseCode.graphics.text.Util;

/**
 * A GUI legend component that displays a color map as a color gradient
 * from min to max, traversing all the colors in the color map.
 *
 * @author  Will Braynen
 * @version $Id$
 */
public class JGradientBar extends JPanel {

   protected JNumberLabel m_min;
   protected JNumberLabel m_max;
   protected JGradientLabel m_gradient;
   protected final static Color[] EMPTY = { Color.GRAY, Color.GRAY };

   /** Creates a new instance of JGradientBar */
   public JGradientBar() {

      m_gradient = new JGradientLabel( EMPTY );
      m_min = new JNumberLabel();
      m_max = new JNumberLabel();

      Font font = getFont().deriveFont( Font.BOLD, 13.0f );
      m_min.setFont( font );
      m_max.setFont( font );

      setLayout( new FlowLayout() );
      add( m_min );
      add( m_gradient );
      add( m_max );
   } // end constructor

   public void setColorMap( Color[] colorMap ) {
      m_gradient.m_colorMap = colorMap;
   } // end setColorMap

   public void setLabels( double min, double max ) {
      m_min.setText( min );
      m_max.setText( max );
   } // end setLabels

} // end class JGradientBar



class JNumberLabel extends JLabel {

   DecimalFormat m_scientificNotation = new DecimalFormat( "0.##E0" );
   DecimalFormat m_regular = new DecimalFormat();
   protected final int MINIMUM_WIDTH = 10;

   public JNumberLabel() {
      super();
   }

   public JNumberLabel( double number ) {
      super();
      m_regular.setMaximumFractionDigits( 3 );
      setText( number );
   }

   public void setText( double number ) {

      // Only very small and very large numbers should be displayed in scientific notation
      String text;
      if ( Math.abs( number ) < 0.01 || Math.abs( number ) > 999 ) {
         text = m_scientificNotation.format( number );
      }
      else {
         text = m_regular.format( number );
      }

      super.setText( text );
   }

   public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      if ( getText().length() > 0 && Util.stringPixelWidth( getText(), getFont(), this ) < MINIMUM_WIDTH ) {
         d.width = MINIMUM_WIDTH;
      }
      return d;
   }
}


class JGradientLabel extends JLabel {

   // fields
   final int WIDTH = 100;
   final int HEIGHT = 20;
   Color[] m_colorMap;

   /** Creates a new instance of JGradientLabel */
   public JGradientLabel( Color[] colorMap ) {

      // colorMap should contain at least two colors
      if (0 == colorMap.length) {

         // if there are no colors, default to grey for both colors
         Color color = colorMap[0];
         colorMap = new Color[2];
         colorMap[0] = colorMap[1] = Color.LIGHT_GRAY;
      }
      else if (1 == colorMap.length) {

         // if there is only one color, make the second color the same
         Color color = colorMap[0];
         colorMap = new Color[2];
         colorMap[0] = colorMap[1] = color;
      }

      m_colorMap = colorMap;

      Dimension d = new Dimension( WIDTH, HEIGHT );
      setSize( d );
      setPreferredSize( d );
   } // end constructor

   protected void paintComponent( Graphics g ) {

      Graphics2D g2 = (Graphics2D) g;

      final int width = getWidth();
      final int height = getHeight();

      int x = 0;
      int y = 0;

      // Go from one color to another, creating a gradient in-between,
      // painting from left to right on this component
      int intervalCount = m_colorMap.length - 1;
      int intervalWidth = width / intervalCount;

      for (int i = 0;  i < intervalCount;  i++) {

         Color color1 = m_colorMap[i];
         Color color2 = m_colorMap[i+1];

         GradientPaint oneColorToAnother = new GradientPaint( x, y, color1, x + intervalWidth, y, color2 );
         g2.setPaint( oneColorToAnother );
         g2.fillRect( x, y, width, height );

         // Move to paint the next vertical screen slice of this component
         x += ( width / intervalCount );
      }
   } // end paintComponent
} // end JGradientLabel
