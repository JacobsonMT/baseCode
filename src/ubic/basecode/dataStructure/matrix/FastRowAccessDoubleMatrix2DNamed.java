/*
 * The baseCode project
 * 
 * Copyright (c) 2006 University of British Columbia
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
package ubic.basecode.dataStructure.matrix;

import cern.colt.list.DoubleArrayList;
import cern.colt.matrix.DoubleMatrix1D;

/**
 * Dense 2D matrix implementation designed for very fast access of entire rows.
 * <p>
 * Implementation note: The key difference between this and the DenseDouble2DMatrixNamed is that this delegates to a
 * DoubleArrayList[], while DenseDouble2DMatrixNamed delegates to a DenseDoubleMatrix2D.
 * 
 * @author pavlidis
 * @version $Id$
 */
public class FastRowAccessDoubleMatrix2DNamed<R, C> extends DoubleMatrixNamed<R, C> {

    /**
     * 
     */
    private static final long serialVersionUID = -5458302072944941517L;
    private DoubleArrayList[] data;

    /**
     * Implementation note: The input matrix is NOT COPIED (due to the way colt DoubleArrayList(double[]) is
     * implemented).F
     * 
     * @param t
     */
    public FastRowAccessDoubleMatrix2DNamed( double[][] t ) {
        super();
        data = new DoubleArrayList[t.length];
        for ( int i = 0; i < t.length; i++ ) {
            data[i] = new DoubleArrayList( t[i] );
        }
    }

    /**
     * @param rows
     * @param cols
     */
    public FastRowAccessDoubleMatrix2DNamed( int rows, int cols ) {
        super();
        data = new DoubleArrayList[rows];
        for ( int i = 0; i < rows; i++ ) {
            data[i] = new DoubleArrayList( new double[cols] );
        }
    }

    /**
     * @return double[][]
     */
    @Override
    public double[][] asArray() {
        double[][] result = new double[rows()][];
        for ( int i = 0; i < rows(); i++ ) {
            result[i] = getRow( i );
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.dataStructure.matrix.DoubleMatrixNamed#columns()
     */

    public int columns() {
        assert data[0] != null;
        return data[0].size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.dataStructure.matrix.DoubleMatrixNamed#get(int, int)
     */

    @Override
    public double get( int x, int y ) {
        assert data[x] != null;
        return data[x].get( y );
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.dataStructure.matrix.DoubleMatrixNamed#getColObj(int)
     */

    public Double[] getColObj( int col ) {
        Double[] result = new Double[rows()];
        for ( int i = 0; i < rows(); i++ ) {
            result[i] = new Double( get( i, col ) );
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.dataStructure.matrix.DoubleMatrixNamed#getColumn(int)
     */
    @Override
    public double[] getColumn( int col ) {
        double[] result = new double[rows()];
        for ( int i = 0; i < rows(); i++ ) {
            result[i] = get( i, col );
        }
        return result;
    }

    public Double getObject( int row, int col ) {
        return new Double( get( row, col ) );
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.dataStructure.matrix.DoubleMatrixNamed#getRow(int)
     */

    @Override
    public double[] getRow( int i ) {
        return data[i].elements();
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.dataStructure.matrix.DoubleMatrixNamed#getRowArrayList(int)
     */
    @Override
    public DoubleArrayList getRowArrayList( int i ) {
        return data[i];
    }

    public Double[] getRowObj( int row ) {
        Double[] result = new Double[columns()];
        for ( int i = 0; i < columns(); i++ ) {
            result[i] = new Double( get( row, i ) );
        }
        return result;
    }

    public boolean isMissing( int i, int j ) {
        return Double.isNaN( get( i, j ) );
    }

    public int rows() {
        return data.length;
    }

    public void set( int x, int y, Double value ) {
        assert data[x] != null;
        assert y >= 0 && y < data[x].size();
        data[x].set( y, value );
    }

    /*
     * (non-Javadoc)
     * 
     * @see basecode.dataStructure.matrix.DoubleMatrixNamed#viewRow(int)
     */
    @Override
    public DoubleMatrix1D viewRow( int j ) {
        return new DenseDoubleMatrix1D( data[j].elements() );
    }

    /*
     * (non-Javadoc)
     * 
     * @see ubic.basecode.dataStructure.matrix.DoubleMatrixNamed#copy()
     */
    @Override
    public DoubleMatrixNamed<R, C> copy() {
        DoubleMatrixNamed<R, C> returnval = new FastRowAccessDoubleMatrix2DNamed<R, C>( this.rows(), this.columns() );

        for ( int i = 0; i < this.rows(); i++ ) {
            returnval.addRowName( this.getRowName( i ), i );
            for ( int j = 0; j < this.columns(); j++ ) {
                if ( i == 0 ) {
                    returnval.addColumnName( this.getColName( j ), j );
                }
                returnval.set( i, j, this.get( i, j ) );
            }
        }
        return returnval;

    }

}
