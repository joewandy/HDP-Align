/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakml.math;


// java
import java.util.*;





/**
 * Convenience implementation of an R-like data-frame, which is essentially a matrix
 * with named rows and columns. It provides specific implementations like
 * {@link DataFrame.Double} and {@link DataFrame.Integer} for quick and memory
 * efficient storage and access to the data. Complete rows or columns can
 * be retrieved from the matrix for further processing.
 * <br />
 * When the data-frame is initialized the row- and column-names are initialized to
 * the index of the the row/column.
 * <p />
 * This class is slated for heavier integration with the {@link Statistical} class.
 */
public abstract class DataFrame
{
	// constructor(s)
	protected DataFrame(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
		for (int i=0; i<rows; ++i)
			rownames.add("" + i);
		for (int i=0; i<cols; ++i)
			colnames.add("" + i);
	}
	
	
	// access
	/**
	 * Returns the number of rows in the matrix.
	 * 
	 * @return			The number of rows in the matrix.
	 */
	public int getNrRows()
	{
		return rows;
	}
	
	/**
	 * Returns the index of the row with the given name. When the name cannot be found
	 * -1 is returned.
	 * 
	 * @param name		The name of the row.
	 * @return			The index of the row.
	 */
	public int getRowIndexOf(String name)
	{
		return rownames.indexOf(name);
	}
	
	/**
	 * Returns the name of the row at the given index. When the index is smaller than 0
	 * or larger or equal then the number of rows an IndexOutOfBoundsException is thrown.
	 * 
	 * @param row		The index of the row.
	 * @return			The name of the row at the given index.
	 * @throws IndexOutOfBoundsException
	 * 					Thrown when the given index falls outside the range of the number of colomns.
	 */
	public String getRowName(int row) throws IndexOutOfBoundsException
	{
		return rownames.get(row);
	}
	
	/**
	 * Returns a list with all the row-names. The position in the list is equal to the
	 * row-index.
	 * 
	 * @return			The list with all the row-names.
	 */
	public Vector<String> getRowNames()
	{
		return rownames;
	}
	
	/**
	 * Sets the name of the row at the given index.
	 * 
	 * @param row		The row index.
	 * @param name		The new name for the row.
	 * @throws IndexOutOfBoundsException
	 * 					Thrown when the given index falls outside the range of the number of rows.
	 */
	public void setRowName(int row, String name) throws IndexOutOfBoundsException
	{
		rownames.set(row, name);
	}
	
	/**
	 * Sets all the row-names in one step. The vector with names needs to be of equal
	 * size as the number of rows or otherwise a RuntimeException is thrown.
	 * 
	 * @param names		The list with the row-names.
	 * @throws RuntimeException
	 * 					Thrown when the list with names is not the same size as the number of rows.
	 */
	public void setRowNames(Vector<String> names) throws RuntimeException
	{
		if (names.size() != rownames.size())
			throw new RuntimeException("invalid length of the names");
		
		rownames.clear();
		rownames.addAll(names);
	}
	
	/**
	 * Sets all the row-names in one step. The array with names needs to be of equal
	 * size as the number of rows or otherwise a RuntimeException is thrown.
	 * 
	 * @param names		The list with the row-names.
	 * @throws RuntimeException
	 * 					Thrown when the list with names is not the same size as the number of rows.
	 */
	public void setRowNames(String... names) throws RuntimeException
	{
		if (names.length != rownames.size())
			throw new RuntimeException("invalid length of the names");
		
		rownames.clear();
		for (String name : names)
			rownames.add(name);
	}
	
	/**
	 * Returns the number of columns in the matrix.
	 * 
	 * @return			The number of columns in the matrix.
	 */
	public int getNrColumns()
	{
		return cols;
	}
	
	/**
	 * Returns the index of the column with the given name. When the name cannot be found
	 * -1 is returned.
	 * 
	 * @param name		The name of the column.
	 * @return			The index of the column.
	 */
	public int getColIndexOf(String name)
	{
		return colnames.indexOf(name);
	}
	
	/**
	 * Returns the name of the column at the given index. When the index is smaller than 0
	 * or larger or equal then the number of columns an IndexOutOfBoundsException is thrown.
	 * 
	 * @param col		The index of the column.
	 * @return			The name of the column at the given index.
	 * @throws IndexOutOfBoundsException
	 * 					Thrown when the given index falls outside the range of the number of columns.
	 */
	public String getColName(int col) throws IndexOutOfBoundsException
	{
		return colnames.get(col);
	}
	
	/**
	 * Returns a list with all the column-names. The position in the list is equal to the
	 * column-index.
	 * 
	 * @return			The list with all the column-names.
	 */
	public Vector<String> getColNames()
	{
		return colnames;
	}
	
	/**
	 * Sets the name of the column at the given index.
	 * 
	 * @param col		The column index.
	 * @param name		The new name for the column.
	 * @throws IndexOutOfBoundsException
	 * 					Thrown when the given index falls outside the range of the number of columns.
	 */
	public void setColName(int col, String name)
	{
		colnames.set(col, name);
	}
	
	/**
	 * Sets all the column-names in one step. The vector with names needs to be of equal
	 * size as the number of columns or otherwise a RuntimeException is thrown.
	 * 
	 * @param names		The list with the column-names.
	 * @throws RuntimeException
	 * 					Thrown when the list with names is not the same size as the number of column.
	 */
	public void setColNames(Vector<String> names) throws RuntimeException
	{
		if (names.size() != colnames.size())
			throw new RuntimeException("invalid length of the names");
		
		colnames.clear();
		colnames.addAll(names);
	}
	
	/**
	 * Sets all the column-names in one step. The array with names needs to be of equal
	 * size as the number of columns or otherwise a RuntimeException is thrown.
	 * 
	 * @param names		The list with the column-names.
	 * @throws RuntimeException
	 * 					Thrown when the list with names is not the same size as the number of column.
	 */
	public void setColNames(String... names) throws RuntimeException
	{
		if (names.length != colnames.size())
			throw new RuntimeException("invalid length of the names");
		
		colnames.clear();
		for (String name : names)
			colnames.add(name);
	}
	
	
	// implementations
	/**
	 * Implementation of the data-frame with the double data-type.
	 */
	public static class Double extends DataFrame
	{
		// constructor(s)
		/**
		 * Constructs a new data-frame with the given number of rows and columns.
		 * 
		 * @param rows			The number of rows.
		 * @param cols			The number of columns.
		 */
		public Double(int rows, int cols)
		{
			super(rows, cols);
			data = new double[rows][cols];
		}


		// Matrix overrides
		/**
		 * Sets the given value at the given row and column.
		 * 
		 * @param row			The row index.
		 * @param col			The column index.
		 * @param val			The value.
		 * @throws IndexOutOfBoundsException
		 * 						Thrown when the row and/or column index does not match the range.
		 */
		public void set(int row, int col, double val) throws IndexOutOfBoundsException
		{
			if (row<0 || row>=rows || col<0 || col>=cols)
				throw new IndexOutOfBoundsException();
			data[row][col] = val;
		}
		
		/**
		 * Sets the given value at the given row and column.
		 * 
		 * @param row			The row name.
		 * @param col			The column index.
		 * @param val			The value.
		 * @throws IndexOutOfBoundsException
		 * 						Thrown when the row and/or column index does not match the range.
		 */
		public void set(String row, int col, double val) throws IndexOutOfBoundsException
		{
			set(getRowIndexOf(row), col, val);
		}
		
		/**
		 * Sets the given value at the given row and column.
		 * 
		 * @param row			The row index.
		 * @param col			The column name.
		 * @param val			The value.
		 * @throws IndexOutOfBoundsException
		 * 						Thrown when the row and/or column index does not match the range.
		 */
		public void set(int row, String col, double val) throws IndexOutOfBoundsException
		{
			set(row, getColIndexOf(col), val);
		}
		
		/**
		 * Sets the given value at the given row and column.
		 * 
		 * @param row			The row name.
		 * @param col			The column name.
		 * @param val			The value.
		 * @throws IndexOutOfBoundsException
		 * 						Thrown when the row and/or column index does not match the range.
		 */
		public void set(String row, String col, double val) throws IndexOutOfBoundsException
		{
			set(getRowIndexOf(row), getColIndexOf(col), val);
		}
		
		/**
		 * Returns the value at the given row and column.
		 * 
		 * @param row			The row index.
		 * @param col			The column index.
		 * @return				The value.
		 * @throws IndexOutOfBoundsException
		 * 						Thrown when the row and/or column index does not match the range.
		 */
		public double get(int row, int col) throws IndexOutOfBoundsException
		{
			if (row<0 || row>=rows || col<0 || col>=cols)
				throw new IndexOutOfBoundsException();
			return data[row][col];
		}
		
		/**
		 * Returns the value at the given row and column.
		 * 
		 * @param row			The row name.
		 * @param col			The column index.
		 * @return				The value.
		 * @throws IndexOutOfBoundsException
		 * 						Thrown when the row and/or column index does not match the range.
		 */
		public double get(String row, int col) throws IndexOutOfBoundsException
		{
			return get(getRowIndexOf(row), col);
		}
		
		/**
		 * Returns the value at the given row and column.
		 * 
		 * @param row			The row index.
		 * @param col			The column name.
		 * @return				The value.
		 * @throws IndexOutOfBoundsException
		 * 						Thrown when the row and/or column index does not match the range.
		 */
		public double get(int row, String col) throws IndexOutOfBoundsException
		{
			return get(row, getColIndexOf(col));
		}
		
		/**
		 * Returns the value at the given row and column.
		 * 
		 * @param row			The row name.
		 * @param col			The column name.
		 * @return				The value.
		 * @throws IndexOutOfBoundsException
		 * 						Thrown when the row and/or column index does not match the range.
		 */
		public double get(String row, String col) throws IndexOutOfBoundsException
		{
			return get(getRowIndexOf(row), getColIndexOf(col));
		}
		
		/**
		 * Returns the complete row with the given name. This is an array with the number
		 * of columns as its size.
		 * 
		 * @param rowname		The row name.
		 * @return				An array with all the row values.
		 */
		public double[] getRow(String rowname)
		{
			return getRow(getRowIndexOf(rowname));
		}
		
		/**
		 * Returns the complete row at the given index. This is an array with the number
		 * of columns as its size.
		 * 
		 * @param row			The row index.
		 * @return				An array with all the row values.
		 */
		public double[] getRow(int row)
		{
			double values[] = new double[cols];
			for (int col=0; col<cols; ++col)
				values[col] = data[row][col];
			return values;
		}
		
		/**
		 * Returns the complete column with the given name. This is an array with the number
		 * of rows as its size.
		 * 
		 * @param colname		The column name.
		 * @return				An array with all the column values.
		 */
		public double[] getCol(String colname)
		{
			return getCol(getColIndexOf(colname));
		}
		
		/**
		 * Returns the complete column with the given name. This is an array with the number
		 * of rows as its size.
		 * 
		 * @param col			The column index.
		 * @return				An array with all the column values.
		 */
		public double[] getCol(int col)
		{
			double values[] = new double[rows];
			for (int row=0; row<rows; ++row)
				values[row] = data[row][col];
			return values;
		}
		
		/**
		 * Returns a matrix with all the data in the dataframe.
		 * 
		 * @return				The matrix making up the data of the dataframe.
		 */
		public double[][] getMatrix()
		{
			return data;
		}
		
		
		// data
		protected double data[][];
	}
	
	
	// data
	protected int rows;
	protected int cols;
	protected Vector<String> rownames = new Vector<String>();
	protected Vector<String> colnames = new Vector<String>();
}
