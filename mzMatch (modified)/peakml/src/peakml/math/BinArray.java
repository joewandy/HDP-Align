//


package peakml.math;


// java





/**
 * This class can be used for collecting numerical information into bins. 
 * 
 * PS:
 * I miss c++. It has such nice mechanisms for handling loose type binding with the
 * templates, making the Java implementation feeling shameful. Now I'm sad :(.
 */
public class BinArray
{
	// constructor
	public BinArray(int size, double min, double max)
	{
		this.min = min;
		this.max = max;
		this.array = new double[size];
		
		this.step = (max-min) / array.length;
		for (int i=0; i<size; ++i)
			this.array[i] = 0;
	}
	
	
	// index access
	public int size()
	{
		return array.length;
	}
	
	public int indexOf(double val) throws RuntimeException
	{
		if (val<min || val>max)
			throw new RuntimeException("parameter val outside of range: " + min + "<" + val + ">" + max);
		
		return (int) (((val-min) / (max-min)) * array.length);
	}
	
	public double getIndex(int index)
	{
		return array[index];
	}
	
	public double getMin()
	{
		return min;
	}
	
	public double getIndexMin(int index)
	{
		return min + index*step;
	}
	
	public double getMax()
	{
		return max;
	}
	
	public double getIndexMax(int index)
	{
		return min + (index+1)*step;
	}
	
	public String getIndexLabel(int index)
	{
		return String.format("%5.2f - %5.2f", getIndexMin(index), getIndexMax(index));
	}
	
	public double getMaxValue()
	{
		double max = 0;
		for (double a : array)
			max = Math.max(max, a);
		return max;
	}
	
	
	// value access
	public double get(double val)
	{
		return array[indexOf(val)];
	}
	
	public void set(double val, double value)
	{
		array[indexOf(val)] = value;
	}
	
	public void add(double val, double value)
	{
		array[indexOf(val)] += value;
	}
	
	public void subtract(double val, double value)
	{
		array[indexOf(val)] -= value;
	}
	
	public void minmin(double val)
	{
		array[indexOf(val)]--;
	}
	
	public void plusplus(double val)
	{
		array[indexOf(val)]++;
	}
	
	
	// data
	protected double min = -1;
	protected double max = -1;
	protected double step = -1;
	protected double array[] = null;
}
