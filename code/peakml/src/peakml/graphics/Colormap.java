/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzMatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzMatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakml.graphics;


// java





/**
 * Defines different color models for visualization purposes (like heatmaps and
 * graphs). A specific colormap can be created by using one of the enumerated
 * integers defined in the class. An example of the use:
 * 
 * <pre>
 * // create the colormap
 * Colormap colormap = new Colormap(Colormap.JET);
 * 
 * // print all the colors
 * for (int i=0; i<colormap.getNrColors(); ++i)
 * {
 *    int color = colormap.getColor(i);
 *    // print the red, green and blue component
 *    System.out.println(((color>>16)&0xFF) + ", " + ((color>>8)&0xFF) + ", " + (color&0xFF));
 * }
 * </pre>
 */
public class Colormap
{
	/** A JET color model as used in Matlab. */
	public static final int JET				= 0;
	/** A Hue-Saturation-Value colormodel. */
	public static final int HSV				= 1;
	/** A heat color model used to create heatmaps. */
	public static final int HEAT			= 2;
	/** A standard excel color model, where each color tries to be as different from the rest as possible. */
	public static final int EXCEL			= 3;
	/** A greyscale color model. */
	public static final int GRAYSCALE		= 4;
	/** A color model ranging from red to white to green. */
	public static final int REDGREEN		= 5;
	/** A color model ranging from red to black to green. */
	public static final int REDBLACKGREEN	= 6;
	/** */
	public static final int RAINBOW			= 7;
	/** */
	public static final int WAVE			= 8;
	
	
	// constructor(s)
	/**
	 * Constructor for creating the specified color model.
	 * 
	 * @param type		The type of color-model to be created.
	 */
	public Colormap(int type) throws RuntimeException
	{
		this.type = type;
		if (type == JET)
			createJetColors();
		else if (type == HSV)
			createHSVColors();
		else if (type == HEAT)
			createHeatColors();
		else if (type == EXCEL)
			createExcelColors();
		else if (type == GRAYSCALE)
			createGrayscaleColors();
		else if (type == REDGREEN)
			createRedGreenColors();
		else if (type == REDBLACKGREEN)
			createRedBlackGreenColors();
		else if (type == RAINBOW)
			createRainbowColors();
		else if (type == WAVE)
			createWaveColors();
		else
			throw new RuntimeException("Unknown colormodel '" + type + "'");
	}
	
	
	// access
	/**
	 * Returns the type of color model stored in the instance.
	 * 
	 * @return			The type of the color model.
	 */
	public int getType()
	{
		return type;
	}
	
	/**
	 * Returns the number of colors stored in this color model. The number of
	 * colors is dependent on the color model chosen, so this value needs to
	 * be checked before retrieving colors.
	 * 
	 * @return			The number of colors stored in the color model.
	 */
	public int getNrColors()
	{
		return colors.length;
	}
	
	/**
	 * Returns an integer representation of the color at the given index. The
	 * index should be larger than 0 and smaller than {@link Colormap#getNrColors()}-1.
	 * 
	 * @param index		The index where the color is located.
	 * @return			The color at the given index represented in integer format.
	 * @throws IndexOutOfBoundsException
	 * 					Thrown when the index is out of bounds.
	 */
	public int getColor(int index) throws IndexOutOfBoundsException
	{
		if (index<0 || index>=colors.length)
			throw new IndexOutOfBoundsException("");
		
		return colors[index];
	}
	
	
	// data
	protected int type;
	protected int colors[] = null;
	
	
	// protected creation-functions
	protected int toInt(int red, int green, int blue)
	{
		return ((255&0xFF)<<24) + ((red&0xFF)<<16) + ((green&0xFF)<<8) + (blue&0xFF);
	}
	
	protected void createJetColors()
	{
		// allocate and initialize the colors-array
		colors = new int[256];
		for (int i=0; i<colors.length; ++i)
			colors[i] = 0;
		
		// create the colors
		double r = 0.;
		double g = 0.;
		double b = 0.;
		
		int n = colors.length / 4;
		for (int i=0; i<colors.length; ++i)
		{
			if (i < n/2.)
			{
				r = 0.;
				g = 0.;
				b = 0.5 + (double) i/n;
			}
			else if (i>=n/2. && i<3.*n/2.)
			{
				r = 0.;
				g = (double) i/n - 0.5;
				b = 1.;
			}
			else if (i>=3.*n/2. && i<5.*n/2.)
			{
				r = (double) i/n - 1.5;
				g = 1.;
				b = 1. - (double) i/n + 1.5;
			}
			else if (i>=5.*n/2. && i<7.*n/2.)
			{
				r = 1.;
				g = 1. - (double) i/n + 2.5;
				b = 0.;
			}
			else if (i >= 7.*n/2.)
			{
				r = 1. - (double) i/n + 3.5;
				g = 0.;
				b = 0.;
			}
			
			colors[i] = toInt((int) (r * 255), (int) (g * 255), (int) (b * 255));
		}
	}
	
	protected void createHSVColors()
	{
		// allocate and initialize the colors-array
		colors = new int[256];
		for (int i=0; i<colors.length; ++i)
			colors[i] = 0;
		
		// create the colors
		double r = 0.;
		double g = 0.;
		double b = 0.;
		
		for (int i=0; i<colors.length; ++i)
		{
			double h = (6./(double)colors.length) * (double) i;
			switch ((int) h)
			{
			case 0:
				r = 1.;
				g = h - (int) h;
				b = 0.;
				break;
			case 1:
				r = 1. - (h - (int) h);
				g = 1.;
				b = 0.;
				break;
			case 2:
				r = 0.;
				g = 1.;
				b = h - (int) h;
				break;
			case 3:
				r = 0.;
				g = 1. - (h - (int) h);
				b = 1.;
				break;
			case 4:
				r = h - (int) h;
				g = 0.;
				b = 1.;
				break;
			case 5:
				r = 1.;
				g = 0.;
				b = 1. - (h - (int) h);
				break;      
			}
			
			colors[i] = toInt((int) (r * 255), (int) (g * 255), (int) (b * 255));
		}
	}
	
	protected void createHeatColors()
	{
		// allocate and initialize the colors-array
		colors = new int[256];
		for (int i=0; i<colors.length; ++i)
			colors[i] = 0;
		
		// create the colors
		int n = (int) (3./8. * colors.length);
		for (int i=0; i<colors.length; ++i)
		{
			double r = (1./n)*(i+1);
			double g = 0.;
			double b = 0.;
			
			if (i >= n)
			{
				r = 1.;
				g = (1./n)*(i+1-n);
				b = 0.;
			}
			if (i >= 2*n)
			{
				r = 1.;
				g = 1.;
				b = 1./(colors.length - 2*n) * (i+1 - 2*n);
			}
			
			colors[i] = toInt((int) (r * 255), (int) (g * 255), (int) (b * 255));
		}
	}
	
	protected void createExcelColors()
	{
		// http://www.geocities.com/davemcritchie/excel/colors.htm
		colors = new int[] {
				toInt(255,	0,		0),
				toInt(0,	255,	0),
				toInt(0,	0,		255),
				toInt(255,	255,	0),
				toInt(255,	0,		255),
				toInt(0,	255,	255),
				toInt(128,	0,		0),
				toInt(0,	128,	0),
				toInt(0,	0,		128),
				toInt(128,	128,	0),
				toInt(128,	0,		128),
				toInt(0,	128,	128),
				toInt(192,	192,	192),
				toInt(128,	128,	128),
				toInt(153,	153,	255),
				toInt(153,	51,		102),
				toInt(255,	255,	204),
				toInt(204,	255,	255),
				toInt(102,	0,		102),
				toInt(255,	128,	128),
				toInt(0,	102,	204),
				toInt(204,	204,	255),
				toInt(0,	0,		128),
				toInt(255,	0,		255),
				toInt(255,	255,	0),
				toInt(0,	255,	255),
				toInt(128,	0,		128),
				toInt(128,	0,		0),
				toInt(0,	128,	128),
				toInt(0,	0,		255),
				toInt(0,	204,	255),
				toInt(204,	255,	255),
				toInt(204,	255,	204),
				toInt(255,	255,	153),
				toInt(153,	204,	255),
				toInt(255,	153,	204),
				toInt(204,	153,	255),
				toInt(255,	204,	153),
				toInt(51,	102,	255),
				toInt(51,	204,	204),
				toInt(153,	204,	0),
				toInt(255,	204,	0),
				toInt(255,	153,	0),
				toInt(255,	102,	0),
				toInt(102,	102,	153),
				toInt(150,	150,	150),
				toInt(0,	51,		102),
				toInt(51,	153,	102),
				toInt(0,	51,		0),
				toInt(51,	51,		0),
				toInt(153,	51,		0),
				toInt(153,	51,		102),
				toInt(51,	51,		153),
				toInt(51,	51,		51)
			};
	}
	
	protected void createGrayscaleColors()
	{
		// allocate and initialize the colors-array
		colors = new int[256];
		for (int i=0; i<colors.length; ++i)
			colors[i] = toInt(i, i, i);
	}
	
	protected void createRedGreenColors()
	{
		colors = new int[256];
		
		double half = colors.length/2.;
		for (int i=0; i<=half; ++i)
			colors[i] = toInt(255, (int) ((i/half) * 255), (int) ((i/half) * 255));
		for (int i=(int) half+1; i<colors.length; ++i)
			colors[i] = toInt(255-(int) (((i-half)/half) * 255), 255, 255-(int) (((i-half)/half) * 255));
	}
	
	protected void createRedBlackGreenColors()
	{
		colors = new int[256];
		
		double half = colors.length/2.;
		for (int i=0; i<=half; ++i)
			colors[i] = toInt(255 - (int) (((i)/half) * 255), 0, 0);
		for (int i=(int) half+1; i<colors.length; ++i)
			colors[i] = toInt(0, (int) (((i-half)/half) * 255), 0);
	}
	
	protected void createRainbowColors()
	{
		colors = new int[256];
		
		for (int i=0; i<colors.length; ++i)
		{
            if (i<=29)
            	colors[i] = toInt((byte)(129.36-i*4.36), 0, 255);
            else if (i<=86)
            	colors[i] = toInt(0, (byte)(-133.54+i*4.52), 255);
            else if (i<=141)
            	colors[i] = toInt(0, 255, (byte)(665.83-i*4.72));
            else if (i<=199)
            	colors[i] = toInt((byte)(-635.26+i*4.47), 255, 0);
            else
            	colors[i] = toInt(255, (byte)(1166.81-i*4.57), 0);
		}
	}
	
	protected void createWaveColors()
	{
		colors = new int[256];
		
		for (int i=0; i<colors.length; ++i)
		{
			colors[i] = toInt(
					(byte) ((Math.sin(((double)i/40-3.2))+1)*128),
					(byte) ((1-Math.sin((i/2.55-3.1)))*70+30),
					(byte) ((1-Math.sin(((double)i/40-3.1)))*128)
				);
		}
	}
	
	
	// test
	public static java.awt.image.BufferedImage createImage(int type, int width, int height)
	{
		Colormap colormap = new Colormap(type);
		
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = img.createGraphics();
		
		double step = height / (double) colormap.getNrColors();
		for (int c=0; c<height; ++c)
		{
			g.setColor(new java.awt.Color(colormap.getColor((int) Math.floor(c / step))));
			g.drawLine(0, height-(c+1), width, height-(c+1));
		}
		
		return img;
	}
	
	public static void main(String args[])
	{
		java.awt.image.BufferedImage img = createImage(Colormap.EXCEL, 50, 511);
		try {
			javax.imageio.ImageIO.write(img, "png", new java.io.File("d:/legend.png"));
		} catch (Exception e) { e.printStackTrace(); }
	}
}
