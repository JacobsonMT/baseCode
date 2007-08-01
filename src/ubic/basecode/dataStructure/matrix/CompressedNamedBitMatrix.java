package ubic.basecode.dataStructure.matrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;

/**
 * Named compressed sparse bit matrix. Elements of the matrix are stored in the <code>long</code> data type.
 * 
 * @author xwan
 */
public class CompressedNamedBitMatrix extends AbstractNamedMatrix2D {

    private static final long serialVersionUID = 1775002416710933373L;
    private FlexCompRowMatrix[] matrix;
    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.AbstractNamedMatrix#columns()
     */
    public static int DOUBLE_LENGTH = 63; // java doesn't support unsigned long.
    private int totalBitsPerItem;
    private int rows = 0, cols = 0;
    public static long BIT1 = 0x0000000000000001L;

    /**
     * Constructs a matrix with specified rows, columns, and total bits per element
     * 
     * @param rows - number of rows in the matrix
     * @param cols - number of columns in the matrix
     * @param totalBitsPerItem - the number of bits for each element
     */
    public CompressedNamedBitMatrix( int rows, int cols, int totalBitsPerItem ) {
        super();
        // calculate number of matrices required
        int num = ( int ) ( totalBitsPerItem / CompressedNamedBitMatrix.DOUBLE_LENGTH ) + 1;
        matrix = new FlexCompRowMatrix[num];
        for ( int i = 0; i < num; i++ )
            matrix[i] = new FlexCompRowMatrix( rows, cols );
        this.totalBitsPerItem = totalBitsPerItem;
        this.rows = rows;
        this.cols = cols;
    }

    /**
     * Returns the total number of bits in a matrix element
     * 
     * @return the number of bits per element
     */
    public int getBitNum() {
        return this.totalBitsPerItem;
    }

    /**
     * Returns all of the bits for an element
     * 
     * @param row - the element row
     * @param col - the element column
     * @return all the bits in an array of <code>longs</code>
     */
    public long[] getAllBits( int row, int col ) {
        long[] allBits = new long[this.matrix.length];
        for ( int i = 0; i < this.matrix.length; i++ )
            allBits[i] = Double.doubleToRawLongBits( this.matrix[i].get( row, col ) );
        return allBits;
    }

    /**
     * Sets the bit of the specified element at the specified index to 1.
     * 
     * @param rows - matrix row
     * @param cols - matrix column
     * @param index - bit vector index
     */
    public void set( int rows, int cols, int index ) {
        if ( index >= this.totalBitsPerItem || rows > this.rows || cols > this.cols ) return;
        int num = ( int ) ( index / CompressedNamedBitMatrix.DOUBLE_LENGTH );
        int bit_index = index % CompressedNamedBitMatrix.DOUBLE_LENGTH;
        long binVal = Double.doubleToRawLongBits( matrix[num].get( rows, cols ) );
        double res = Double.longBitsToDouble( binVal | CompressedNamedBitMatrix.BIT1 << bit_index );
        matrix[num].set( rows, cols, res );
    }
    
    public void reset( int rows, int cols) {
        for ( int i = 0; i < this.matrix.length; i++ )
        	this.matrix[i].set(rows,cols,0);
    }


    /**
     * Checks the bit of the specified element at the specified index.
     * 
     * @param rows - matrix row
     * @param cols - matrix column
     * @param index - bit vector index
     * @return true if bit is 1, false if 0.
     */
    public boolean check( int rows, int cols, int index ) {
        if ( index >= this.totalBitsPerItem || rows > this.rows || cols > this.cols ) return false;
        int num = ( int ) ( index / CompressedNamedBitMatrix.DOUBLE_LENGTH );
        int bit_index = index % CompressedNamedBitMatrix.DOUBLE_LENGTH;
        long binVal = Double.doubleToRawLongBits( matrix[num].get( rows, cols ) );
        long res = binVal & CompressedNamedBitMatrix.BIT1 << bit_index;
        if ( res == 0 ) return false;
        return true;
    }

    /**
     * Count the number of bits of the passed-in <code>double</code>.
     * 
     * @param val
     * @return number of bits in val
     */
    static public int countBits( double val ) {
    	if(val == 0.0) return 0;
        long binVal = Double.doubleToRawLongBits( val );
        return Long.bitCount( binVal );
    }
    
    public int[] getRowBits(int row, int[] bits){
    	for(int i = 0; i < this.matrix.length; i++){
    		SparseVector vector = this.matrix[i].getRow(row);
    		double[] data = vector.getData();
    		int[] indices = vector.getIndex();
	    	for(int j = 0; j < data.length; j++){
	    		if(indices[j] == 0 && j > 0) break;
	    		if(data[j] != 0.0)
	    			bits[indices[j]] = bits[indices[j]] + countBits(data[j]);
	    	}
    	}
    	return bits;
    }
    /**
     * Count the number of bits at the specified element position
     * 
     * @param rows
     * @param cols
     * @return
     */
    public int bitCount( int rows, int cols ) {
        int bits = 0;
        if ( rows > this.rows || cols > this.cols ) return bits;
        for ( int i = 0; i < this.matrix.length; i++ ) {
            double val = this.matrix[i].get( rows, cols );
            if ( val != 0 ) bits = bits + countBits( val );
        }
        return bits;
    }

    /**
     * Counts the number of bits that in common between the two specified elements; i.e. performs an AND operation on
     * the two bit vectors and counts the remaining 1 bits.
     * 
     * @param row1 - element 1 row
     * @param col1 - element 1 column
     * @param row2 - element 2 row
     * @param col2 - element 2 column
     * @return number of bits in common
     */
    public int overlap( int row1, int col1, int row2, int col2 ) {
        int bits = 0;
        for ( int i = 0; i < this.matrix.length; i++ ) {
            double val1 = this.matrix[i].get( row1, col1 );
            double val2 = this.matrix[i].get( row2, col2 );
            if ( val1 == 0 || val2 == 0 ) continue;
            long binVal1 = Double.doubleToRawLongBits( val1 );
            long binVal2 = Double.doubleToRawLongBits( val2 );
            bits = bits + countBits( binVal1 & binVal2 );
        }
        return bits;
    }

    /**
     * Return the number of columns in the matrix
     * 
     * @return number of columns
     */
    public int columns() {
        return this.cols;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.AbstractNamedMatrix#getColObj(int)
     */
    public Object[] getColObj( int i ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.AbstractNamedMatrix#getRowObj(int)
     */
    public Object[] getRowObj( int i ) {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.AbstractNamedMatrix#isMissing(int, int)
     */
    public boolean isMissing( int i, int j ) {
        // TODO Auto-generated method stub
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.AbstractNamedMatrix#rows()
     */
    public int rows() {
        return this.rows;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.AbstractNamedMatrix#set(int, int, java.lang.Object)
     */
    public void set( int i, int j, Object val ) {
        // TODO Auto-generated method stub

    }

    /**
     * Set the matrix element to the specified bit vector
     * @param row
     * @param col
     * @param val
     * @return true if set successfully
     */
    public boolean set( int row, int col, double[] val ) {
        // TODO Auto-generated method stub
        if ( val.length != this.matrix.length || row >= this.rows || col >= this.cols ) return false;
        for ( int mi = 0; mi < val.length; mi++ )
            this.matrix[mi].set( row, col, val[mi] );
        return true;
    }

    /**
     * Save the matrix to the specified file
     * @param fileName - save file
     */
    public void toFile( String fileName ) throws IOException {
        FileWriter out = new FileWriter( new File( fileName ) );
        out.write( this.rows + "\t" + this.cols + "\t" + this.totalBitsPerItem + "\n" );
        Object[] rowNames = this.getRowNames().toArray();
        for ( int i = 0; i < rowNames.length; i++ ) {
            out.write( rowNames[i].toString() );
            if ( i != rowNames.length - 1 ) out.write( "\t" );
        }
        out.write( "\n" );
        Object[] colNames = this.getColNames().toArray();
        for ( int i = 0; i < colNames.length; i++ ) {
            out.write( colNames[i].toString() );
            if ( i != colNames.length - 1 ) out.write( "\t" );
        }
        out.write( "\n" );
        for ( int i = 0; i < this.rows; i++ )
            for ( int j = 0; j < this.cols; j++ ) {
                if ( this.bitCount( i, j ) != 0 ) {
                    out.write( i + "\t" + j );
                    for ( int k = 0; k < this.matrix.length; k++ ) {
                        long binVal = Double.doubleToRawLongBits( this.matrix[k].get( i, j ) );
                        /* Long.parseLong( hexString, 16) to get it back; */
                        String hexString = Long.toHexString( binVal );
                        out.write( "\t" + hexString );
                    }
                    out.write( "\n" );
                }
            }
        out.close();
    }
}
