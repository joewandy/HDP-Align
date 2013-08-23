/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakmlviewer.util;


// java
//import java.awt.Color;
//import java.awt.Rectangle;
//import java.awt.Graphics2D;
//import java.awt.RenderingHints;
//
//import java.awt.image.ColorModel;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.awt.image.IndexColorModel;
import java.awt.image.DirectColorModel;

// swt
//import org.eclipse.swt.*;
//import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;




public class ImageConvert {
//	  public static BufferedImage convertToAWT(ImageData data) {
//		    ColorModel colorModel = null;
//		    PaletteData palette = data.palette;
//		    if (palette.isDirect) {
//		      colorModel = new DirectColorModel(data.depth, palette.redMask,
//		          palette.greenMask, palette.blueMask);
//		      BufferedImage bufferedImage = new BufferedImage(colorModel,
//		          colorModel.createCompatibleWritableRaster(data.width,
//		              data.height), false, null);
//		      WritableRaster raster = bufferedImage.getRaster();
//		      int[] pixelArray = new int[3];
//		      for (int y = 0; y < data.height; y++) {
//		        for (int x = 0; x < data.width; x++) {
//		          int pixel = data.getPixel(x, y);
//		          RGB rgb = palette.getRGB(pixel);
//		          pixelArray[0] = rgb.red;
//		          pixelArray[1] = rgb.green;
//		          pixelArray[2] = rgb.blue;
//		          raster.setPixels(x, y, 1, 1, pixelArray);
//		        }
//		      }
//		      return bufferedImage;
//		    } else {
//		      RGB[] rgbs = palette.getRGBs();
//		      byte[] red = new byte[rgbs.length];
//		      byte[] green = new byte[rgbs.length];
//		      byte[] blue = new byte[rgbs.length];
//		      for (int i = 0; i < rgbs.length; i++) {
//		        RGB rgb = rgbs[i];
//		        red[i] = (byte) rgb.red;
//		        green[i] = (byte) rgb.green;
//		        blue[i] = (byte) rgb.blue;
//		      }
//		      if (data.transparentPixel != -1) {
//		        colorModel = new IndexColorModel(data.depth, rgbs.length, red,
//		            green, blue, data.transparentPixel);
//		      } else {
//		        colorModel = new IndexColorModel(data.depth, rgbs.length, red,
//		            green, blue);
//		      }
//		      BufferedImage bufferedImage = new BufferedImage(colorModel,
//		          colorModel.createCompatibleWritableRaster(data.width,
//		              data.height), false, null);
//		      WritableRaster raster = bufferedImage.getRaster();
//		      int[] pixelArray = new int[1];
//		      for (int y = 0; y < data.height; y++) {
//		        for (int x = 0; x < data.width; x++) {
//		          int pixel = data.getPixel(x, y);
//		          pixelArray[0] = pixel;
//		          raster.setPixel(x, y, pixelArray);
//		        }
//		      }
//		      return bufferedImage;
//		    }
//		  }

	public static ImageData convertToSWT(BufferedImage bufferedImage)
	{
		if (bufferedImage.getColorModel() instanceof DirectColorModel)
		{
			DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(
					colorModel.getRedMask(), colorModel.getGreenMask(), colorModel.getBlueMask()
				);
			
			ImageData data = new ImageData(
					bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel.getPixelSize(), palette
				);
			
			for (int y = 0; y < data.height; y++)
			{
				for (int x = 0; x < data.width; x++)
				{
					int rgb = bufferedImage.getRGB(x, y);
					int pixel = palette.getPixel(new RGB((rgb>>16)&0xFF, (rgb>>8)&0xFF, (rgb>>0)&0xFF)); 
					data.setPixel(x, y, pixel);
				}
			}
			
			return data;		
		}
		else if (bufferedImage.getColorModel() instanceof IndexColorModel)
		{
	      IndexColorModel colorModel = (IndexColorModel) bufferedImage
	          .getColorModel();
	      int size = colorModel.getMapSize();
	      byte[] reds = new byte[size];
	      byte[] greens = new byte[size];
	      byte[] blues = new byte[size];
	      colorModel.getReds(reds);
	      colorModel.getGreens(greens);
	      colorModel.getBlues(blues);
	      RGB[] rgbs = new RGB[size];
	      for (int i = 0; i < rgbs.length; i++) {
	        rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
	            blues[i] & 0xFF);
	      }
	      PaletteData palette = new PaletteData(rgbs);
	      ImageData data = new ImageData(bufferedImage.getWidth(),
	          bufferedImage.getHeight(), colorModel.getPixelSize(),
	          palette);
	      data.transparentPixel = colorModel.getTransparentPixel();
	      WritableRaster raster = bufferedImage.getRaster();
	      int[] pixelArray = new int[1];
	      for (int y = 0; y < data.height; y++) {
	        for (int x = 0; x < data.width; x++) {
	          raster.getPixel(x, y, pixelArray);
	          data.setPixel(x, y, pixelArray[0]);
	        }
	      }
	      return data;
	    }
	    return null;
	  }

}
