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



package peakml.math.wavelet;


// java





/**
 * 
 */
public class MarrWavelet extends Wavelet
{
	// constructor(s)
	/**
	 * scale - peak width
	 * spacing - 0.001
	 */
	public MarrWavelet(double scale, double spacing)
	{
		this.scale = scale/5;
		this.spacing = spacing;
		
		// build the wavelet (only the right hand part is needed as the wavelet is mirrored)
		wavelet = new double[(int) Math.ceil(5*this.scale/spacing)];
		for (int i=0; i<this.wavelet.length; ++i)
		{
			double x = i*this.spacing/this.scale;
			this.wavelet[i] = (1-x*x) * Math.exp(-x*x/2);
		}
	}
	
	public static void main(String args[])
	{
		new MarrWavelet(100, 0.01);
	}
	
	
	// Wavelet overrides
	public double getScale()
	{
		return scale;
	}
	
	public double integrate(double values[], int index)
	{
	    double v = 0.;
	    
	    int half_width = wavelet.length-1;
	    int index_in_data = floor(half_width * spacing);
	    int offset_data_left = ((index - index_in_data) < 0) ? 0 : (index-index_in_data);
	    int offset_data_right = ((index + index_in_data) > values.length-1) ? values.length-2 : (index+index_in_data);

	    // integrate from i until offset_data_left
	    for (int i=index; i>offset_data_left; --i)
	    {
	    	int index_w_r = round((index-i)/spacing);
			int index_w_l = round((index-(i-1))/spacing);
			v += 1. / 2.*(values[i]*wavelet[index_w_r] + values[i-1]*wavelet[index_w_l]);
	    }

	    // integrate from i+1 until offset_data_right
	    for (int i=index; i<offset_data_right; ++i)
	    {
			int index_w_r = round(((i+1)-index)/spacing);
			int index_w_l = round((i-index)/spacing);
			v += 1. / 2.*(values[i+1]*wavelet[index_w_r] + values[i]*wavelet[index_w_l]);
	    }

	    return v / Math.sqrt(scale);
	}
	private static int floor(double v)	{ return (int) v; }
	private static int round(double v)	{ return (int) (v + 0.5); }
	
	
	
	// data
	protected double scale;
	protected double spacing;
	protected double wavelet[];
}
