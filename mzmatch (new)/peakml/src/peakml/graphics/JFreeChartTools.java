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
import java.io.*;
import java.awt.image.*;

// itext
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

// jfreechart
import org.jfree.chart.*;





/**
 * This class offers an interface for dealing with JFreeChart graphs. Most notably
 * methods are provided for exporting the graphs to files.
 */
public class JFreeChartTools
{
	/** Write in the PDF format, which has the graphics in vector format. */
	public static final int PDF = 0;
	/** Write in the PNG format, which is pixel-based but readable anywhere. */
	public static final int PNG = 1;
	
	
	// chart write functions
	/**
	 * General access method, which can be plugged with the filetype to write to. This
	 * method links through to {@link JFreeChartTools#writeAsPDF(OutputStream, JFreeChart, int, int)}
	 * and {@link JFreeChartTools#writeAsPNG(OutputStream, JFreeChart, int, int)}.
	 * 
	 * @param filetype		The type of file to write.
	 * @param out			The output stream to write to.
	 * @param chart			The chart to be written.
	 * @param width			The width of the image.
	 * @param height		The height of the image.
	 * @throws IOException	Thrown when an error occurs with the IO.
	 */
	public static void writeAs(int filetype, OutputStream out, JFreeChart chart, int width, int height) throws IOException
	{
		if (filetype == PDF)
			writeAsPDF(out, chart, width, height);
		else if (filetype == PNG)
			writeAsPNG(out, chart, width, height);
		else
			throw new IOException("Unknown file format.");
	}
	
	/**
	 * This method writes the given graph to the output stream in the PDF format. As a vector
	 * based file format it allows some freedom to be changed (e.g. colors, line thickness, etc.),
	 * which can be convenient for presentation purposes.
	 * 
	 * @param out			The output stream to write to.
	 * @param chart			The chart to be written.
	 * @param width			The width of the image.
	 * @param height		The height of the image.
	 * @throws IOException	Thrown when an error occurs with the IO.
	 */
	public static void writeAsPDF(OutputStream out, JFreeChart chart, int width, int height) throws IOException
	{
		Document document = new Document(new Rectangle(width, height), 50, 50, 50, 50);
		document.addAuthor("");
		document.addSubject("");
		
		try
		{
			PdfWriter writer = PdfWriter.getInstance(document, out);
			document.open();
			PdfContentByte cb = writer.getDirectContent();
			PdfTemplate tp = cb.createTemplate(width, height);
			
			java.awt.Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
			chart.draw(g2, new java.awt.geom.Rectangle2D.Double(0, 0, width, height));
			
			g2.dispose();
			cb.addTemplate(tp, 0, 0);
		}
		catch (DocumentException de)
		{
			throw new IOException(de.getMessage());
		}
		
		document.close();
	}
	
	/**
	 * This method writes the given graph to the output stream in the PNG format.
	 * 
	 * @param out			The output stream to write to.
	 * @param chart			The chart to be written.
	 * @param width			The width of the image.
	 * @param height		The height of the image.
	 * @throws IOException	Thrown when an error occurs with the IO.
	 */
	public static void writeAsPNG(OutputStream out, JFreeChart chart, int width, int height) throws IOException
	{
		BufferedImage graph_img = new BufferedImage(800, 500, BufferedImage.TYPE_INT_ARGB);
		chart.draw(graph_img.createGraphics(), new java.awt.Rectangle(0, 0, 800, 500));
		javax.imageio.ImageIO.write(graph_img, "png", out);
	}
}
