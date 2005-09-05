/*
 * The baseCode project
 * 
 * Copyright (c) 2005 Columbia University
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package baseCode.dataStructure.matrix;

import java.util.Iterator;
import java.util.List;

/**
 * Represents a matrix with named columns and rows.
 * <p>
 * Copyright (c) 2004
 * </p>
 * <p>
 * Institution:: Columbia University
 * </p>
 * 
 * @author Paul Pavlidis
 * @version $Id$
 */
public interface NamedMatrix {

    /**
     * Add a column name associated with an index.
     * 
     * @param s String a column name
     * @param index int the column index associated with this name
     */
    public void addColumnName( String s, int index );

    /**
     * Add a row name associated with a row index.
     * 
     * @param s String
     * @param index int
     */
    public void addRowName( String s, int index );

    /**
     * Get the index of a row by name..
     * 
     * @param s String
     * @return int
     */
    public int getRowIndexByName( String s );

    /**
     * Get the index of a column by name.
     * 
     * @param s String
     * @return int
     */
    public int getColIndexByName( String s );

    /**
     * Get the row name for an index
     * 
     * @param i int
     * @return java.lang.String
     */
    public String getRowName( int i );

    /**
     * Gte the column name for an index.
     * 
     * @param i int
     * @return java.lang.String
     */
    public String getColName( int i );

    /**
     * @return boolean
     */
    public boolean hasRowNames();

    /**
     * Check if this matrix has a valid set of column names.
     * 
     * @return boolean
     */
    public boolean hasColNames();

    /**
     * @param v List a vector of Strings.
     */
    public void setRowNames( List v );

    /**
     * @param v List a vector of Strings.
     */
    public void setColumnNames( List v );

    /**
     * @return List of Strings
     */
    public List getColNames();

    /**
     * @return List of Strings
     */
    public List getRowNames();

    /**
     * @param r String
     * @return boolean
     */
    public boolean hasRow( String r );

    /**
     * @return java.util.Iterator
     */
    public Iterator getRowNameMapIterator();

    /**
     * Get the number of rows the matrix has
     * 
     * @return int
     */
    public int rows();

    /**
     * Get the number of columns the matrix has.
     * 
     * @return int
     */
    public int columns();

    /**
     * Set a value in the matrix.
     * 
     * @param i int
     * @param j int
     * @param val Object
     */
    public void set( int i, int j, Object val );

    /**
     * Get a row in the matrix as a generic Object[]. This exists so NamedMatrices can be used more generically.
     * 
     * @param i int row
     * @return Object[]
     */
    public Object[] getRowObj( int i );

    /**
     * @param i int column
     * @return Object[]
     */
    public Object[] getColObj( int i );

    /**
     * Check if the value at a given index is missing.
     * 
     * @param i row
     * @param j column
     * @return true if the value is missing, false otherwise.
     */
    public boolean isMissing( int i, int j );

    /**
     * Return the number of missing values in the matrix.
     * 
     * @return
     */
    public int numMissing();

    /**
     * @param rowName
     * @return
     */
    public boolean containsRowName( String rowName );

    /**
     * @param columnName
     * @return
     */
    public boolean containsColumnName( String columnName );

    /**
     * @return
     */

}