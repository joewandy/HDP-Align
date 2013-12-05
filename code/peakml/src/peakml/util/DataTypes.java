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



package peakml.util;


// java





/**
 * Utility class, providing a standard interface for dealing with standard data
 * types.
 */
public class DataTypes
{
	/**
	 * Converts the contents of the double-matrix to a string. The data is formatted
	 * to look nice (3 decimals) and readable.
	 * 
	 * @param matrix		The matrix to be converted.
	 * @return				The string.
	 */
	public static String toString(double[][] matrix)
	{
		StringBuffer str = new StringBuffer();
		
		for (int i=0; i<matrix.length; ++i)
		{
			str.append("|");
			for (int j=0; j<matrix[i].length; ++j)
				str.append(String.format(" %7.3f", matrix[i][j]));
			str.append(" |");
			if (i < matrix.length-1)
				str.append("\n");
		}
		return str.toString();
	}
	
	/**
	 * Converts the contents of the float-matrix to a string. The data is formatted
	 * to look nice (3 decimals) and readable.
	 * 
	 * @param matrix		The matrix to be converted.
	 * @return				The string.
	 */
	public static String toString(float[][] matrix)
	{
		StringBuffer str = new StringBuffer();
		
		for (int i=0; i<matrix.length; ++i)
		{
			str.append("|");
			for (int j=0; j<matrix[i].length; ++j)
				str.append(String.format(" %7.3f", matrix[i][j]));
			str.append(" |");
			if (i < matrix.length-1)
				str.append("\n");
		}
		return str.toString();
	}
	
	/**
	 * Converts the contents of the integer-matrix to a string. The data is formatted
	 * to look nice (3 decimals) and readable.
	 * 
	 * @param matrix		The matrix to be converted.
	 * @return				The string.
	 */
	public static String toString(int[][] matrix)
	{
		StringBuffer str = new StringBuffer();
		
		for (int i=0; i<matrix.length; ++i)
		{
			str.append("|");
			for (int j=0; j<matrix[i].length; ++j)
				str.append(String.format("%5d", matrix[i][j]));
			str.append(" |");
			if (i < matrix.length-1)
				str.append("\n");
		}
		return str.toString();
	}
	
	/**
	 * Converts the contents of the double-vector to a string. The data is formatted
	 * to look nice (3 decimals) and readable.
	 * 
	 * @param matrix		The vector to be converted.
	 * @return				The string.
	 */
	public static String toString(double[] vector)
	{
		StringBuffer str = new StringBuffer();
		
		str.append("|");
		for (int i=0; i<vector.length; ++i)
			str.append(" " + String.format("%7.3f", vector[i]));
		str.append(" |");
		
		return str.toString();
	}
	
	/**
	 * Converts the contents of the float-vector to a string. The data is formatted
	 * to look nice (3 decimals) and readable.
	 * 
	 * @param matrix		The vector to be converted.
	 * @return				The string.
	 */
	public static String toString(float[] vector)
	{
		StringBuffer str = new StringBuffer();
		
		str.append("|");
		for (int i=0; i<vector.length; ++i)
			str.append(" " + String.format("%7.3f", vector[i]));
		str.append(" |");
		
		return str.toString();
	}
	
	/**
	 * Converts the contents of the integer-vector to a string. The data is formatted
	 * to look nice and readable.
	 * 
	 * @param matrix		The vector to be converted.
	 * @return				The string.
	 */
	public static String toString(int[] vector)
	{
		StringBuffer str = new StringBuffer();
		
		str.append("|");
		for (int i=0; i<vector.length; ++i)
			str.append(" " + String.format("%5d", vector[i]));
		str.append(" |");
		
		return str.toString();
	}
}
