package baseCode.gui.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.Border;
import java.awt.Rectangle;

/**
 * 
 * @author Will Braynen
 * @version $Id$
 */
public class JBarGraphCellRenderer extends JLabel implements TableCellRenderer {

   protected Object m_values = null;
   protected final static int LINE_WIDTH = 2;
   protected final static Color[] COLORS = { Color.BLUE, Color.GRAY, Color.RED,
         Color.GREEN, Color.CYAN, Color.MAGENTA, Color.ORANGE };
   protected static Border m_noFocusBorder = new EmptyBorder( 1, 1, 1, 1 );
   protected static Color m_selectionBackground;
   protected boolean m_isSelected = false;
   protected boolean m_isBarGraph = false;
   DecimalFormat m_regular = new DecimalFormat();
   
   public JBarGraphCellRenderer() {
      super();
      setOpaque( false );
      setBorder( m_noFocusBorder );
   }

   /**
    * This method is called each time a cell in a column
    * using this renderer needs to be rendered.
    *
    * @param  table      the <code>JTable</code>
    * @param  value      the value to assign to the cell at
    *			 <code>[row, column]</code>
    * @param  isSelected true if cell is selected
    * @param  hasFocus   true if cell has focus
    * @param  row        the row of the cell to render
    * @param  column     the column of the cell to render
    * @return the default table cell renderer
    */
   public Component getTableCellRendererComponent( JTable table, Object value,
         boolean isSelected, boolean hasFocus, int row, int column ) {

      m_values = value;

      // set background
      m_isSelected = isSelected;
      if ( isSelected ) {
         super.setBackground( m_selectionBackground = table.getSelectionBackground() );
      }
      else {
         super.setBackground( table.getBackground() );
         // or force a white background instead
      }

      if ( hasFocus ) {
         setBorder( UIManager.getBorder( "Table.focusCellHighlightBorder" ) );
      } else {
         setBorder( m_noFocusBorder );
      }
      
      m_isBarGraph = false;
      if ( value.getClass().equals( ArrayList.class ) ) {
         // bar graph
         m_isBarGraph = true;
         m_values = value;
         
        // Double x = ( Double ) ( ( ArrayList ) value ).get( 0 ); 
         Double x = new Double(m_regular.format(-Math.log( ( ( Double ) ( ( ArrayList ) value ).get( 0 ) ).doubleValue())/Math.log(10)));
         setToolTipText( x.toString() );
      }
      else if ( value.getClass().equals( Double.class ) ) {
         // just double value, no bar graph
         setText( value.toString() );
         setFont( table.getFont() );
      }
      
      // Since the renderer is a component, return itself
      return this;
   }
   
   protected void paintBackground( Graphics g ) {
      g.setColor( m_selectionBackground );
      g.fillRect( 0, 0, getWidth(), getHeight() );
   }
   
   protected void paintComponent( Graphics g ) {

      if ( m_isSelected ) {
         paintBackground( g );
      }
      
      super.paintComponent( g );
      
      if ( ! m_isBarGraph ) return;
      if ( m_values == null ) return;

      final int width = getWidth();
      final int height = getHeight();
      final int y = 0;
      
      ArrayList values = ( ArrayList ) m_values;

      double maxPval = 10.0;
      
      for ( int i = 0; i < values.size(); i++ ) {

         // @todo only use log if doLog is requested. probably log should be in genesettablemodel
         double value = Math.min(maxPval,-Math.log( ( ( Double ) values.get( i ) ).doubleValue())/Math.log(10));
         
         if ( ! Double.isNaN( value ) )
         {
            // map from [0,1] range to [0,width] range
           // int x = ( int ) ( value * width );
            int x = (int)( (double)width * value / maxPval ) ;

            // what color to use?
            if ( i < COLORS.length ) {
               g.setColor( COLORS[i] );
            } else {
               // ran out of colors!
               g.setColor( Color.LIGHT_GRAY );
            }

            // draw the vertical bar line
            if ( x > width ) x = width - LINE_WIDTH;
            g.fillRect( x, y, LINE_WIDTH, height );
         }
      }
   } // end paintComponent
   
   public void validate() {}
   public void revalidate() {}
   public void repaint(long tm, int x, int y, int width, int height) {}
   public void repaint(Rectangle r) {}
   
   
} // end class
