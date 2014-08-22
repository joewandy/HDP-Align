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
 * Utility functions for converting arrays of basic types into an array of bytes and
 * vica-versa, which is especially useful for storing large amounts of data into a
 * binary file. This provides a central point for platform independent storage of
 * data. The endiannes can be set for each conversion in order to support platform
 * independent conversion. Additionally, each conversion function accepts a precision
 * value, indicating the amount of bits to use for storage (ie either 32 or 64).
 */
public class ByteArray
{
	/** The data is stored in big endian mode. */
	public static final int ENDIAN_BIG		= 0;
	/** The data is stored in little endian mode. */
	public static final int ENDIAN_LITTLE	= 1;
	
	
	// access
	/**
	 * Converts the given integer array into a byte-array. The
	 * given value for endiannes (either {@link ByteArray#ENDIAN_BIG} or
	 * {@link ByteArray#ENDIAN_LITTLE}) indicates the byte-order the data should be
	 * stored in. The precision indicates the number of bits for each element
	 * in the integer array.
	 * <p />
	 * For the precision only 32 is supported, as 64 does not make sense on
	 * the Java platform.
	 * 
	 * @param array				The array with the integer-values.
	 * @param endiannes			The byte-order in which the data has been stored (either {@link ByteArray#ENDIAN_BIG} or {@link ByteArray#ENDIAN_LITTLE}).
	 * @param precision			The number of bits for each element in the array.
	 * @return					The array with the byte-values.
	 */
	public static byte[] toByteArray(int array[], int endiannes, int precision)
	{
		if (precision != 32)
			throw new RuntimeException("only 32 bits precision supported");
		if (endiannes!=ENDIAN_BIG && endiannes!=ENDIAN_LITTLE)
			throw new RuntimeException("unknown endiannes set: '" + endiannes + "'");
		
		byte bytes[] = null;
		if (precision==32 && endiannes==ENDIAN_BIG)
		{
			bytes = new byte[4*array.length];
			for (int i=0; i<array.length; ++i)
			{
				bytes[4*i + 3] = (byte) (array[i]>>24);
				bytes[4*i + 2] = (byte) (array[i]>>16);
				bytes[4*i + 1] = (byte) (array[i]>> 8);
				bytes[4*i + 0] = (byte) (array[i]>> 0);
			}
		}
		else if (precision==32 && endiannes==ENDIAN_LITTLE)
		{
			bytes = new byte[4*array.length];
			for (int i=0; i<array.length; ++i)
			{
				bytes[4*i + 0] = (byte) (array[i]>>24);
				bytes[4*i + 1] = (byte) (array[i]>>16);
				bytes[4*i + 2] = (byte) (array[i]>> 8);
				bytes[4*i + 3] = (byte) (array[i]>> 0);
			}
		}
		
		return bytes;
	}
	
	/**
	 * Converts the given long array into a byte-array. The
	 * given value for endiannes (either {@link ByteArray#ENDIAN_BIG} or
	 * {@link ByteArray#ENDIAN_LITTLE}) indicates the byte-order the data should be
	 * stored in. The precision indicates the number of bits for each element
	 * in the long array.
	 * 
	 * @param array				The array with the long-values.
	 * @param endiannes			The byte-order in which the data has been stored (either {@link ByteArray#ENDIAN_BIG} or {@link ByteArray#ENDIAN_LITTLE}).
	 * @param precision			The number of bits for each element in the array.
	 * @return					The array with the byte-values.
	 */
	public static byte[] toByteArray(long array[], int endiannes, int precision)
	{
		if (precision!=32 && precision!=64)
			throw new RuntimeException("only 32 and 64 bits precision supported");
		if (endiannes!=ENDIAN_BIG && endiannes!=ENDIAN_LITTLE)
			throw new RuntimeException("unknown endiannes set: '" + endiannes + "'");
		
		byte bytes[] = null;
		if (precision==32 && endiannes==ENDIAN_BIG)
		{
			bytes = new byte[4*array.length];
			for (int i=0; i<array.length; ++i)
			{
				bytes[4*i + 3] = (byte) (array[i]>>24);
				bytes[4*i + 2] = (byte) (array[i]>>16);
				bytes[4*i + 1] = (byte) (array[i]>> 8);
				bytes[4*i + 0] = (byte) (array[i]>> 0);
			}
		}
		else if (precision==32 && endiannes==ENDIAN_LITTLE)
		{
			bytes = new byte[4*array.length];
			for (int i=0; i<array.length; ++i)
			{
				bytes[4*i + 0] = (byte) (array[i]>>24);
				bytes[4*i + 1] = (byte) (array[i]>>16);
				bytes[4*i + 2] = (byte) (array[i]>> 8);
				bytes[4*i + 3] = (byte) (array[i]>> 0);
			}
		}
		else if (precision==64 && endiannes==ENDIAN_BIG)
		{
			bytes = new byte[4*array.length];
			for (int i=0; i<array.length; ++i)
			{
				bytes[4*i + 3] = (byte) (array[i]>>56);
				bytes[4*i + 2] = (byte) (array[i]>>48);
				bytes[4*i + 1] = (byte) (array[i]>>40);
				bytes[4*i + 0] = (byte) (array[i]>>32);
				bytes[4*i + 7] = (byte) (array[i]>>24);
				bytes[4*i + 6] = (byte) (array[i]>>16);
				bytes[4*i + 5] = (byte) (array[i]>> 8);
				bytes[4*i + 4] = (byte) (array[i]>> 0);
			}
		}
		else if (precision==64 && endiannes==ENDIAN_LITTLE)
		{
			bytes = new byte[4*array.length];
			for (int i=0; i<array.length; ++i)
			{
				bytes[4*i + 0] = (byte) (array[i]>>56);
				bytes[4*i + 1] = (byte) (array[i]>>48);
				bytes[4*i + 2] = (byte) (array[i]>>40);
				bytes[4*i + 3] = (byte) (array[i]>>32);
				bytes[4*i + 4] = (byte) (array[i]>>24);
				bytes[4*i + 5] = (byte) (array[i]>>16);
				bytes[4*i + 6] = (byte) (array[i]>> 8);
				bytes[4*i + 7] = (byte) (array[i]>> 0);
			}
		}
		
		return bytes;
	}
	
	/**
	 * Converts the given double array into a byte-array. The
	 * given value for endiannes (either {@link ByteArray#ENDIAN_BIG} or
	 * {@link ByteArray#ENDIAN_LITTLE}) indicates the byte-order the data should be
	 * stored in. The precision indicates the number of bits for each element
	 * in the integer array.
	 * 
	 * @param array				The array with the double-values.
	 * @param endiannes			The byte-order in which the data has been stored (either {@link ByteArray#ENDIAN_BIG} or {@link ByteArray#ENDIAN_LITTLE}).
	 * @param precision			The number of bits for each element in the array.
	 * @return					The array with the byte-values.
	 */
	public static byte[] toByteArray(double array[], int endiannes, int precision)
	{
		if (precision!=32 && precision!=64)
			throw new RuntimeException("only 32 and 64 bits precision supported");
		if (endiannes!=ENDIAN_BIG && endiannes!=ENDIAN_LITTLE)
			throw new RuntimeException("unknown endiannes set: '" + endiannes + "'");
		
		byte bytes[] = null;
		if (precision==32 && endiannes==ENDIAN_BIG)
		{
			bytes = new byte[4*array.length];
			for (int i=0; i<array.length; ++i)
			{
				int intbits = Float.floatToIntBits((float) array[i]);
				
				bytes[4*i + 3] = (byte) (intbits>> 0);
				bytes[4*i + 2] = (byte) (intbits>> 8);
				bytes[4*i + 1] = (byte) (intbits>>16);
				bytes[4*i + 0] = (byte) (intbits>>24);
			}
		}
		else if (precision==32 && endiannes==ENDIAN_LITTLE)
		{
			bytes = new byte[4*array.length];
			for (int i=0; i<array.length; ++i)
			{
				int intbits = Float.floatToIntBits((float) array[i]);
				
				bytes[4*i + 0] = (byte) (intbits>> 0);
				bytes[4*i + 1] = (byte) (intbits>> 8);
				bytes[4*i + 2] = (byte) (intbits>>16);
				bytes[4*i + 3] = (byte) (intbits>>24);
			}
		}
		else if (precision==64 && endiannes==ENDIAN_BIG)
		{
			bytes = new byte[8*array.length];
			for (int i=0; i<array.length; ++i)
			{
				long intbits = Double.doubleToLongBits(array[i]);
				
				bytes[8*i + 3] = (byte) (intbits>> 0);
				bytes[8*i + 2] = (byte) (intbits>> 8);
				bytes[8*i + 1] = (byte) (intbits>>16);
				bytes[8*i + 0] = (byte) (intbits>>24);
				bytes[8*i + 7] = (byte) (intbits>>32);
				bytes[8*i + 6] = (byte) (intbits>>40);
				bytes[8*i + 5] = (byte) (intbits>>48);
				bytes[8*i + 4] = (byte) (intbits>>56);
			}
		}
		else if (precision==64 && endiannes==ENDIAN_LITTLE)
		{
			bytes = new byte[8*array.length];
			for (int i=0; i<array.length; ++i)
			{
				long intbits = Double.doubleToLongBits(array[i]);
				
				bytes[8*i + 0] = (byte) (intbits>> 0);
				bytes[8*i + 1] = (byte) (intbits>> 8);
				bytes[8*i + 2] = (byte) (intbits>>16);
				bytes[8*i + 3] = (byte) (intbits>>24);
				bytes[8*i + 4] = (byte) (intbits>>32);
				bytes[8*i + 5] = (byte) (intbits>>40);
				bytes[8*i + 6] = (byte) (intbits>>48);
				bytes[8*i + 7] = (byte) (intbits>>56);
			}
		}
		
		return bytes;
	}
	
	/**
	 * Converts a byte array containing integer-values into a long array. The
	 * given value for endiannes (either {@link ByteArray#ENDIAN_BIG} or
	 * {@link ByteArray#ENDIAN_LITTLE}) indicates the byte-order the data has been
	 * stored in. The precision indicates the number of bits for each element
	 * in the integer array.
	 * <p />
	 * For the precision only 32 is supported, as 64 does not make sense on
	 * the Java platform.
	 * 
	 * @param array				The byte-array to be converted.
	 * @param endiannes			The byte-order in which the data has been stored (either {@link ByteArray#ENDIAN_BIG} or {@link ByteArray#ENDIAN_LITTLE}).
	 * @param precision			The number of bits for each element in the long-array.
	 * @return					The array with the integer-values.
	 */
	public static int[] toIntArray(byte array[], int endiannes, int precision)
	{
		if (precision != 32)
			throw new RuntimeException("only 32 bits precision supported");
		if (endiannes!=ENDIAN_BIG && endiannes!=ENDIAN_LITTLE)
			throw new RuntimeException("unknown endiannes set: '" + endiannes + "'");
		
		int length = array.length / (precision/8);
		int intarray[] = new int[length];
		
		if (precision==32 && endiannes==ENDIAN_BIG)
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				intarray[i] =
					((int) (array[pos+0]&0xFF))<< 0 |
					((int) (array[pos+1]&0xFF))<< 8 |
					((int) (array[pos+2]&0xFF))<<16 |
					((int) (array[pos+3]&0xFF))<<24;
	
				pos += 4;
			}
		}
		else if (precision==32 && endiannes==ENDIAN_LITTLE)
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				intarray[i] =
					((int) (array[pos+3]&0xFF))<< 0 |
					((int) (array[pos+2]&0xFF))<< 8 |
					((int) (array[pos+1]&0xFF))<<16 |
					((int) (array[pos+0]&0xFF))<<24;
	
				pos += 4;
			}
		}
	
		return intarray;
	}
	
	/**
	 * Converts a byte array containing long-values into a long array. The
	 * given value for endiannes (either {@link ByteArray#ENDIAN_BIG} or
	 * {@link ByteArray#ENDIAN_LITTLE}) indicates the byte-order the data has been
	 * stored in. The precision indicates the number of bits for each element
	 * in the long array.
	 * 
	 * @param array				The byte-array to be converted.
	 * @param endiannes			The byte-order in which the data has been stored (either {@link ByteArray#ENDIAN_BIG} or {@link ByteArray#ENDIAN_LITTLE}).
	 * @param precision			The number of bits for each element in the long-array.
	 * @return					The array with the long-values.
	 */
	public static long[] toLongArray(byte array[], int endiannes, int precision)
	{
		if (precision!=32 && precision!=64)
			throw new RuntimeException("only 32 and 64 bits precision supported");
		if (endiannes!=ENDIAN_BIG && endiannes!=ENDIAN_LITTLE)
			throw new RuntimeException("unknown endiannes set: '" + endiannes + "'");
		
		int length = array.length / (precision/8);
		long longarray[] = new long[length];
		
		if (precision==32 && endiannes==ENDIAN_BIG)
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				longarray[i] =
					((int) (array[pos+3]&0xFF))<< 0 |
					((int) (array[pos+2]&0xFF))<< 8 |
					((int) (array[pos+1]&0xFF))<<16 |
					((int) (array[pos+0]&0xFF))<<24;
	
				pos += 4;
			}
		}
		else if (precision==32 && endiannes==ENDIAN_LITTLE)
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				longarray[i] =
					((int) (array[pos+0]&0xFF))<< 0 |
					((int) (array[pos+1]&0xFF))<< 8 |
					((int) (array[pos+2]&0xFF))<<16 |
					((int) (array[pos+3]&0xFF))<<24;
	
				pos += 4;
			}
		}
		if (precision==64 && endiannes==ENDIAN_BIG)
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				longarray[i] =
					((int) (array[pos+3]&0xFF))<< 0 |
					((int) (array[pos+2]&0xFF))<< 8 |
					((int) (array[pos+1]&0xFF))<<16 |
					((int) (array[pos+0]&0xFF))<<24 |
					((int) (array[pos+7]&0xFF))<<32 |
					((int) (array[pos+6]&0xFF))<<40 |
					((int) (array[pos+5]&0xFF))<<48 |
					((int) (array[pos+4]&0xFF))<<56;
	
				pos += 8;
			}
		}
		else if (precision==32 && endiannes==ENDIAN_LITTLE)
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				longarray[i] =
					((int) (array[pos+0]&0xFF))<< 0 |
					((int) (array[pos+1]&0xFF))<< 8 |
					((int) (array[pos+2]&0xFF))<<16 |
					((int) (array[pos+3]&0xFF))<<24 |
					((int) (array[pos+4]&0xFF))<<32 |
					((int) (array[pos+5]&0xFF))<<40 |
					((int) (array[pos+6]&0xFF))<<48 |
					((int) (array[pos+7]&0xFF))<<56;
	
				pos += 8;
			}
		}
		
		return longarray;
	}
	
	/**
	 * Converts a byte array containing double-values into a double array. The
	 * given value for endiannes (either {@link ByteArray#ENDIAN_BIG} or
	 * {@link ByteArray#ENDIAN_LITTLE}) indicates the byte-order the data has been
	 * stored in. The precision indicates the number of bits for each element
	 * in the double array.
	 * 
	 * @param array				The byte-array to be converted.
	 * @param endiannes			The byte-order in which the data has been stored (either {@link ByteArray#ENDIAN_BIG} or {@link ByteArray#ENDIAN_LITTLE}).
	 * @param precision			The number of bits for each element in the double-array.
	 * @return					The array with the double-values.
	 */
	public static double[] toDoubleArray(byte array[], int endiannes, int precision)
	{
		if (precision!=32 && precision!=64)
			throw new RuntimeException("only 32 and 64 bits precision supported");
		if (endiannes!=ENDIAN_BIG && endiannes!=ENDIAN_LITTLE)
			throw new RuntimeException("unknown endiannes set: '" + endiannes + "'");
		
		int length = array.length / (precision/8);
		double doublearray[] = new double[length];
		
		if (precision==32 && endiannes==ENDIAN_BIG)
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				doublearray[i] = Float.intBitsToFloat(
						((int) (array[pos+3]&0xFF))<< 0 |
						((int) (array[pos+2]&0xFF))<< 8 |
						((int) (array[pos+1]&0xFF))<<16 |
						((int) (array[pos+0]&0xFF))<<24
					);
				
				pos += 4;
			}
		}
		else if (precision==32 && endiannes==ENDIAN_LITTLE)
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				doublearray[i] = Float.intBitsToFloat(
						((int) (array[pos+0]&0xFF))<< 0 |
						((int) (array[pos+1]&0xFF))<< 8 |
						((int) (array[pos+2]&0xFF))<<16 |
						((int) (array[pos+3]&0xFF))<<24
					);
				
				pos += 4;
			}
		}
		else if (precision==64 && endiannes==ENDIAN_BIG) // TODO BROKEN!
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				doublearray[i] = Double.longBitsToDouble(
						((long) (array[pos+3]&0xFF))<< 0 |
						((long) (array[pos+2]&0xFF))<< 8 |
						((long) (array[pos+1]&0xFF))<<16 |
						((long) (array[pos+0]&0xFF))<<24 |
						((long) (array[pos+7]&0xFF))<<32 |
						((long) (array[pos+6]&0xFF))<<40 |
						((long) (array[pos+5]&0xFF))<<48 |
						((long) (array[pos+4]&0xFF))<<56
					);
				
				pos += 8;
			}
		}
		else if (precision==64 && endiannes==ENDIAN_LITTLE) // TODO BROKEN!
		{
			int pos = 0;
			for (int i=0; i<length; ++i)
			{
				doublearray[i] = Double.longBitsToDouble(
						((long) (array[pos+0]&0xFF))<< 0 |
						((long) (array[pos+1]&0xFF))<< 8 |
						((long) (array[pos+2]&0xFF))<<16 |
						((long) (array[pos+3]&0xFF))<<24 |
						((long) (array[pos+4]&0xFF))<<32 |
						((long) (array[pos+5]&0xFF))<<40 |
						((long) (array[pos+6]&0xFF))<<48 |
						((long) (array[pos+7]&0xFF))<<56
					);
				
				pos += 8;
			}
		}
		
		return doublearray;
	}
}
