package baseCode.io.reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import baseCode.dataStructure.matrix.NamedMatrix;
import baseCode.dataStructure.matrix.RCDoubleMatrix1D;
import baseCode.dataStructure.matrix.SparseDoubleMatrix2DNamed;
import baseCode.dataStructure.matrix.SparseRaggedDoubleMatrix2DNamed;
import baseCode.util.FileTools;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.map.OpenIntIntHashMap;
import cern.colt.map.OpenIntObjectHashMap;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Best data structure for reading really big, really sparse matrices when a matrix represetation is needed. *
 * <p>
 * The standard format looks like this:
 * 
 * <pre>
 * 
 *  
 *   
 *    
 *     
 *                              2          &lt;--- number of items - the first line of the file only. NOTE - this line is often blank or not present.
 *                              1 2        &lt;--- items 1 has 2 edges
 *                              1 2        &lt;--- edge indices are to items 1 &amp; 2
 *                              0.1 100    &lt;--- with the following weights
 *                              2 2        &lt;--- items 2 also has 2 edges
 *                              1 2        &lt;--- edge indices are also to items 1 &amp; 2 (fully connected)
 *                              100 0.1    &lt;--- with the following weights
 *     
 *    
 *   
 *  
 * </pre>
 * 
 * <hr>
 * <p>
 * Copyright (c) 2004 Columbia University
 * 
 * @author pavlidis
 * @version $Id$
 */
public class SparseRaggedDouble2DNamedMatrixReader extends
      AbstractNamedMatrixReader {

   /**
    * Read a sparse square matrix that is expressed as an adjacency list in a tab-delimited file:
    * 
    * <pre>
    * 
    *                item1 item2 weight
    *                item1 item5 weight
    *  
    * </pre>
    * 
    * <p>
    * By definition the resulting matrix is square.
    * 
    * @param name of file
    * @return
    */
   public NamedMatrix readFromAdjList( String fileName ) throws IOException {
      if ( !FileTools.testFile( fileName ) ) {
         throw new IOException( "Could not read from file " + fileName );
      }
      FileInputStream stream = new FileInputStream( fileName );
      return readFromAdjList( stream );
   }

   /**
    * @throws IOException
    * @throws NumberFormatException Read a sparse square matrix that is expressed as an adjacency list in a
    *         tab-delimited file:
    * 
    * <pre>
    * 
    *   item1 item2 weight
    *   item1 item5 weight
    *  
    * </pre>
    * 
    * <p>
    *         By definition the resulting matrix is square.
    * @param stream
    * @return
    */
   public NamedMatrix readFromAdjList(  InputStream stream )
         throws NumberFormatException, IOException {
      Set itemNames = new HashSet();
      Map rows = new HashMap();

      BufferedReader dis = new BufferedReader( new InputStreamReader( stream ) );

     OpenIntObjectHashMap indexNameMap = new OpenIntObjectHashMap(); // eventual row index --> name
      Map nameIndexMap = new HashMap(); // name --> eventual row index

      /* 
       * Store the information about the matrix in a temporary set of data structures, the most important of which is
       * a map of nodes to edge information. Each edge information object contains the index and the weight of the edge.
       */
      String row;
      int index = 0;
      while ( ( row = dis.readLine() ) != null ) {
         StringTokenizer st = new StringTokenizer( row, " \t", false );

         String itemA = "";
         if ( st.hasMoreTokens() ) {
            itemA = st.nextToken();

            if ( !itemNames.contains( itemA ) ) {
               rows.put( itemA, new HashSet() );
               itemNames.add( itemA );
              indexNameMap.put( index, itemA );
               nameIndexMap.put( itemA, new Integer( index ) );
               index++;
            }
         } else {
            //  continue;
         }

         String itemB = "";
         if ( st.hasMoreTokens() ) {
            itemB = st.nextToken();
            if ( !itemNames.contains( itemB ) ) {
               rows.put( itemB, new HashSet() );
               itemNames.add( itemB );
              indexNameMap.put( index, itemB );
               nameIndexMap.put( itemB, new Integer( index ) );
               index++;
            }
         } else {
            //  continue;
         }

         double weight;
         if ( st.hasMoreTokens() ) {
            weight = Double.parseDouble( st.nextToken() );
         } else {
            weight = 1.0; // just make it a binary matrix.
         }

         ( ( Set ) rows.get( itemA ) ).add( new IndexScoreDyad(
               ( ( Integer ) nameIndexMap.get( itemB ) ).intValue(), weight ) );
         ( ( Set ) rows.get( itemB ) ).add( new IndexScoreDyad(
               ( ( Integer ) nameIndexMap.get( itemA ) ).intValue(), weight ) );
      }

      SparseRaggedDoubleMatrix2DNamed matrix = new SparseRaggedDoubleMatrix2DNamed();
      
      IntArrayList inL =  indexNameMap.keys();
      inL.sort();
      int[] indexList =inL.elements();
      for ( int i = 0; i < indexList.length; i++ ) {
         int itemIndex = indexList[i];
 
         String itemName = ( String ) indexNameMap.get(itemIndex);
         
         Set arow = ( Set ) rows.get( itemName ); // set of IndexScoreDyads
         int size = arow.size();

         OpenIntIntHashMap map = new OpenIntIntHashMap( size, 0.4, 0.8 );
         DoubleArrayList values = new DoubleArrayList( size );
         DoubleArrayList finalValues = new DoubleArrayList( size );
 
         int j = 0;
         for ( Iterator iterator = arow.iterator(); iterator.hasNext(); ) {
            IndexScoreDyad element = ( IndexScoreDyad ) iterator.next();
            int ind = element.getKey();
            double weight = element.getValue();
            map.put( ind, j );
            values.add( weight );
            j++;
         }
         
         IntArrayList indexes = map.keys();
         indexes.sort();
         int[] ix = indexes.elements();
         for ( int k = 0; k < indexes.size(); k++ ) {
            finalValues.add( values.get( map.get( ix[k] ) ) );
         }

         DoubleMatrix1D rowMatrix = new RCDoubleMatrix1D( indexes, finalValues );
         matrix.addRow( itemName, rowMatrix );

      }

      dis.close();
      return matrix;
   }

   /*
    * (non-Javadoc)
    * 
    * @see baseCode.io.reader.AbstractNamedMatrixReader#read(java.lang.String)
    */
   public NamedMatrix read( String fileName ) throws IOException {
      if ( !FileTools.testFile( fileName ) ) {
         throw new IOException( "Could not read from file " + fileName );
      }
      FileInputStream stream = new FileInputStream( fileName );
      return read( stream );
   }

   public NamedMatrix readOneRow( BufferedReader dis ) throws IOException {
      return this.readOneRow( dis, 0 );
   }

   /**
    * Use this to read one row from a matrix. It does not close the reader. (this actually has to read several lines to
    * get the data for one matrix row)
    * 
    * @param stream
    * @param offset A value indicating the lowest value for the indexes listed. This is here in case the indexes in the
    *        stream are numbered starting from 1 instead of zero.
    * @return @throws IOException
    */
   public NamedMatrix readOneRow( BufferedReader dis, int offset )
         throws IOException {
      SparseRaggedDoubleMatrix2DNamed returnVal = new SparseRaggedDoubleMatrix2DNamed();

      String row = dis.readLine(); // line containing the id and the number of edges.
      StringTokenizer tok = new StringTokenizer( row, " \t" );

      int index = Integer.parseInt( tok.nextToken() );
      int amount = Integer.parseInt( tok.nextToken() );
      String rowName = new Integer( index ).toString();
      returnVal.addRow( rowName, readOneRow( dis, amount, offset ) );
      return returnVal;
   }

   /**
    * Read an entire sparse matrix from a stream.
    * 
    * @param stream
    * @return @throws IOException
    */
   public NamedMatrix read( InputStream stream ) throws IOException {
      return this.read( stream, 0 );
   }

   /**
    * Read an entire sparse matrix from a stream.
    * 
    * @param stream
    * @param offset A value indicating the lowest value for the indexes listed. This is here in case the indexes in the
    *        stream are numbered starting from 1 instead of zero.
    * @return @throws IOException
    */
   public NamedMatrix read( InputStream stream, int offset ) throws IOException {
      BufferedReader dis = new BufferedReader( new InputStreamReader( stream ) );
      SparseRaggedDoubleMatrix2DNamed returnVal = new SparseRaggedDoubleMatrix2DNamed();

      String row;
      int k = 1;

      while ( ( row = dis.readLine() ) != null ) {

         if ( row.equals( "" ) ) { // incase there is a blank line.
            continue;
         }

         StringTokenizer tok = new StringTokenizer( row, " \t" );

         if ( tok.countTokens() != 2 ) { // in case the row count is there.
            continue;
         }

         int index = Integer.parseInt( tok.nextToken() ) - offset;

         int amount = Integer.parseInt( tok.nextToken() );

         if ( ( index % 500 ) == 0 ) {
            log.info( new String( "loading  " + index + "th entry" ) );
         }

         returnVal.addRow( new Integer( k ).toString(), readOneRow( dis,
               amount, offset ) );

         k++;
      }

      dis.close();
      return returnVal;
   }

   private DoubleMatrix1D readOneRow( BufferedReader dis, int amount, int offset )
         throws IOException {

      /*
       * we have to be careful to skip any lines that invalid. Each line should have at least two characters. In the
       * files JW provided there are some lines that are just " ".
       */
      String rowInd = "";
      String rowWei = "";

      //     while ( rowInd.length() < 2 ) {
      rowInd = dis.readLine(); // row with indices.
      //    }

      //    while ( rowWei.length() < 2 ) {
      rowWei = dis.readLine(); // row with weights.
      //    }

      StringTokenizer tokw = new StringTokenizer( rowWei, " \t" );
      StringTokenizer toki = new StringTokenizer( rowInd, " \t" );

      OpenIntIntHashMap map = new OpenIntIntHashMap( amount, 0.4, 0.8 );
      DoubleArrayList values = new DoubleArrayList( amount );
      DoubleArrayList finalValues = new DoubleArrayList( amount );

      int i = 0;
      while ( toki.hasMoreTokens() ) {

         double weight = Double.parseDouble( tokw.nextToken() );
         int ind = Integer.parseInt( toki.nextToken() ) - offset;

         if ( ind < 0 ) {
            throw new IllegalStateException(
                  "Can't have negative index - check offset." );
         }

         map.put( ind, i );
         values.add( weight );
         i++;
      }

      IntArrayList indexes = map.keys();
      indexes.sort();
      int[] ix = indexes.elements();
      int size = ix.length;
      for ( int j = 0; j < size; j++ ) {
         finalValues.add( values.get( map.get( ix[j] ) ) );
      }

      return new RCDoubleMatrix1D( indexes, finalValues );
   }

}