package baseCode.dataStructure.matrix;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Vector;

import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.jet.stat.Descriptive;

/**
 * A sparse matrix class where the rows are ragged.
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class SparseRaggedDoubleMatrix2DNamed extends AbstractNamedDoubleMatrix {

   private Vector matrix; // a vector of DoubleArrayList containing the values of the matrix
   // private Vector indexes; // a vector of IntArrayList listing the columns used in each row.

   int columns = 0;
   private boolean isDirty = true;

   private DoubleMatrix1D rowToGive;

   public SparseRaggedDoubleMatrix2DNamed() {
      matrix = new Vector();
      //    indexes = new Vector();
   }

   /*
    * (non-Javadoc)
    * 
    * @see baseCode.dataStructure.matrix.NamedMatrix#rows()
    */
   public int rows() {
      return matrix.size();
   }

   /*
    * (non-Javadoc) Unfortunately this has to iterate over the entire array.
    * 
    * @see baseCode.dataStructure.matrix.NamedMatrix#columns()
    */
   public int columns() {

      if ( !isDirty ) {
         return columns;
      }

      int max = 0;
      for ( Iterator iter = matrix.iterator(); iter.hasNext(); ) {
         DoubleMatrix1D element = ( DoubleMatrix1D ) iter.next();

         int value = element.size();
         if ( value > max ) {
            max = value;
         }

      }

      columns = max;
      System.err.println( "Computed columns: " + columns );
      rowToGive = new SparseDoubleMatrix1D( columns );
      isDirty = false;
      return columns;
   }

   /*
    * (non-Javadoc)
    * 
    * @see baseCode.dataStructure.matrix.NamedMatrix#set(int, int, java.lang.Object)
    */
   public void set( int i, int j, Object val ) {
      set( i, j, ( ( Double ) val ).doubleValue() );
   }

   /**
    * Slow. todo Could save time if we know indices were sorted. Calling this frequently should be avoided at all costs.
    * 
    * @param i row
    * @param j column
    * @param d value
    */
   public void set( int i, int j, double d ) {

      //      if ( matrix.size() < i + 1 ) {
      //         throw new IllegalArgumentException( "Index out of bounds" );
      //      }
      //
      //      DoubleArrayList row = ( DoubleArrayList ) matrix.get( i );
      //      IntArrayList rowind = ( IntArrayList ) indexes.get( i );
      //      int[] el = rowind.elements();
      //      boolean success = false;
      //      for ( int k = 0; k < el.length; k++ ) {
      //         if ( el[k] == j ) {
      //            row.set( k, d );
      //            success = true;
      //            break;
      //         }
      //      }
      //
      //      if ( !success ) {
      //         rowind.add( j );
      //         row.add( d );
      //      }

      ( ( DoubleMatrix1D ) matrix.get( i ) ).set( j, d );

   }

   /*
    * (non-Javadoc)
    * 
    * @see baseCode.dataStructure.matrix.NamedMatrix#getRowObj(int)
    */
   public Object[] getRowObj( int i ) {
      Double[] result = new Double[columns()];

      double[] row = getRow( i );

      for ( int j = 0; j < columns(); j++ ) {
         result[i] = new Double( row[j] );
      }
      return result;
   }

   /*
    * (non-Javadoc)
    * 
    * @see baseCode.dataStructure.matrix.NamedMatrix#getColObj(int)
    */
   public Object[] getColObj( int i ) {
      throw new UnsupportedOperationException();
   }

   /*
    * (non-Javadoc)
    * 
    * @see baseCode.dataStructure.matrix.NamedMatrix#isMissing(int, int)
    */
   public boolean isMissing( int i, int j ) {

      //      IntArrayList rowind = ( IntArrayList ) indexes.get( i );
      //      int[] el = rowind.elements();
      //      boolean success = false;
      //      for ( int k = 0; k < el.length; k++ ) {
      //         if ( el[k] == j ) {
      //            success = true;
      //            break;
      //         }
      //      }

      return get( i, j ) == 0.0;
   }

   /**
    * @return java.lang.String
    */
   public String toString() {
      NumberFormat nf = NumberFormat.getInstance();
      String result = "";
      if ( this.hasColNames() || this.hasRowNames() ) {
         result = "label";
      }

      if ( this.hasColNames() ) {
         for ( int i = 0; i < columns(); i++ ) {
            result = result + "\t" + getColName( i );
         }
         result += "\n";
      }

      for ( int i = 0; i < rows(); i++ ) {
         if ( this.hasRowNames() ) {
            result += getRowName( i );
         }
         for ( int j = 0; j < columns(); j++ ) {

            double value = get( i, j );

            if ( value == 0.0 ) {
               result = result + "\t";
            } else {

               result = result + "\t" + nf.format( value );
            }
         }
         result += "\n";
      }
      return result;
   }

   /**
    * This is slow, because we don't know where the column is. todo could speed up...
    * 
    * @param row
    * @param column
    * @return
    */
   public double get( int i, int j ) {
      return ( ( DoubleMatrix1D ) matrix.get( i ) ).getQuick( j );
   }

   //   /**
   //    * If we are just iterating over the row, then we just want the item selected by an index into the row, not a
   // column
   //    * number.
   //    *
   //    * @param row
   //    * @param index
   //    * @return
   //    */
   //   public double getByInd( int row, int index ) {
   //      return ( ( DoubleArrayList ) matrix.get( row ) ).getQuick( index );
   //   }

   //   /**
   //    * Get the column number for a row item.
   //    *
   //    * @param row
   //    * @param num
   //    * @return
   //    */
   //   public double getIndex( int row, int num ) {
   //      return ( ( IntArrayList ) indexes.get( row ) ).getQuick( num );
   //   }

   /**
    * This gives just the list of values in the row - make sure this is what you want. It does not include the zero
    * values.
    * 
    * @param row
    * @return
    */
   public DoubleArrayList getRowArrayList( int row ) {
      //return ( DoubleArrayList ) matrix.get( row );
      DoubleArrayList returnVal = new DoubleArrayList();
      ( ( DoubleMatrix1D ) matrix.get( row ) ).getNonZeros( new IntArrayList(),
            returnVal );
      return returnVal;
   }

   /*
    * (non-Javadoc)
    * 
    * @see baseCode.dataStructure.matrix.AbstractNamedDoubleMatrix#getRowMatrix1D(int)
    */
   public DoubleMatrix1D getRowMatrix1D( int i ) {

      //      DoubleArrayList values = getRowArrayList( i );
      //      IntArrayList index = getIndex( i );
      // 
      //      if (rowToGive == null ) {
      //         columns();
      //      }
      //      
      //      rowToGive.assign(0.0); // requires iterating over the entire thing.
      //      for ( int j = 0; j < values.size(); j++ ) {
      //         double value = values.getQuick( j );
      //         int ind = index.getQuick( j ) - 1;
      //         rowToGive.set( ind, value );
      //      }
      //
      //      return rowToGive;
      return ( DoubleMatrix1D ) matrix.get( i );
   }

   /*
    * (non-Javadoc)
    * 
    * @see baseCode.dataStructure.matrix.AbstractNamedDoubleMatrix#getRow(int)
    */
   public double[] getRow( int i ) {
      // return getRowMatrix1D( i ).toArray();
      return ( ( DoubleMatrix1D ) matrix.get( i ) ).toArray();
   }

   //   /**
   //    * @param row
   //    * @return
   //    */
   //   public IntArrayList getIndex( int row ) {
   //      return ( IntArrayList ) indexes.get( row );
   //   }

   /**
    * @param name
    * @param values
    * @param indexes
    */
   public void addRow( String name, DoubleArrayList values, IntArrayList ind ) {

      SparseDoubleMatrix1D rowToAdd = new SparseDoubleMatrix1D( max( ind ) );
      //   matrix.add( new SparseDoubleMatrix1D( values.elements() ) );
      //  indexes.add( new SparseIntMatrix1D( ind.elements()) );

      int[] v = ind.elements();
      for ( int i = 0; i < v.length; i++ ) {
         int j = v[i];
         rowToAdd.set( j - 1, values.get( i ) );
      }
      matrix.add( rowToAdd );
      this.addColumnName( name, matrix.size() - 1 );
      this.addRowName( name, matrix.size() - 1 );
      isDirty = true;
   }

   private static int max( IntArrayList data ) {
      int size = data.size();
      if ( size == 0 ) throw new IllegalArgumentException();

      int[] elements = data.elements();
      int max = elements[size - 1];
      for ( int i = size - 1; --i >= 0; ) {
         if ( elements[i] > max ) max = elements[i];
      }

      return max;
   }

}