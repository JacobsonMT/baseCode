package baseCode.dataStructure;

import java.util.Map;
import java.util.Vector;

import cern.colt.matrix.ObjectMatrix1D;
import cern.colt.matrix.impl.DenseObjectMatrix2D;

/**
 * A NamedMatrix containing String objects.
 * <p>
 * Copyright (c) 2004
 * </p>
 * <p>
 * Institution: Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public class StringMatrix2DNamed extends AbstractNamedMatrix {

   private DenseObjectMatrix2D matrix;
   private Vector rowNames;
   private Vector colNames;
   private Map rowMap; //contains a map of each row and elements in the row
   private Map colMap;

   public StringMatrix2DNamed( int x, int y ) {
      super();
      matrix = new DenseObjectMatrix2D( x, y );
   }

   /**
    * 
    * @return java.lang.String
    */
   public String toString() {
      String result = "label";
      for ( int i = 0; i < columns(); i++ ) {
         result = result + "\t" + getColName( i );
      }
      result += "\n";

      for ( int i = 0; i < rows(); i++ ) {
         result += getRowName( i );
         for ( int j = 0; j < columns(); j++ ) {
            result = result + "\t" + get( i, j );
         }
         result += "\n";
      }
      return result;
   }

   public Object[] getRow( int row ) {
      return viewRow( row ).toArray();
   }

   public Object[] getCol( int col ) {
      String[] result = new String[rows()];
      for ( int i = 0; i < rows(); i++ ) {
         result[i] = ( String ) get( i, col );
      }
      return result;
   }

   public Object[] getRowObj( int row ) {
      String[] result = new String[columns()];
      for ( int i = 0; i < columns(); i++ ) {
         result[i] = ( String ) get( row, i );
      }
      return result;
   }

   public Object[] getColObj( int col ) {
      String[] result = new String[rows()];
      for ( int i = 0; i < rows(); i++ ) {
         result[i] = ( String ) get( i, col );
      }
      return result;
   }

   public boolean isMissing( int i, int j ) {
      return get( i, j ) == "";
   }

   /**
    * @return
    */
   public int columns() {
      return matrix.columns();
   }

   /**
    * @param row
    * @param column
    * @return
    */
   public Object get( int row, int column ) {
      return matrix.get( row, column );
   }

   /**
    * @param row
    * @param column
    * @return
    */
   public Object getQuick( int row, int column ) {
      return matrix.getQuick( row, column );
   }

   /**
    * @return
    */
   public int rows() {
      return matrix.rows();
   }

   /**
    * @return
    */
   public int size() {
      return matrix.size();
   }

   /**
    * @param column
    * @return
    */
   public ObjectMatrix1D viewColumn( int column ) {
      return matrix.viewColumn( column );
   }

   /**
    * @param row
    * @return
    */
   public ObjectMatrix1D viewRow( int row ) {
      return matrix.viewRow( row );
   }

   /**
    * @param row
    * @param column
    * @param value
    */
   public void set( int row, int column, Object value ) {
      matrix.set( row, column, value );
   }

   /**
    * @param row
    * @param column
    * @param value
    */
   public void setQuick( int row, int column, Object value ) {
      matrix.setQuick( row, column, value );
   }
}