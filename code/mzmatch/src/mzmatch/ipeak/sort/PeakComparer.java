package mzmatch.ipeak.sort;

import java.util.HashMap;

import mzmatch.ipeak.util.Common;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.Peak;
import peakml.io.Header;
import peakml.io.MeasurementInfo;
import peakml.math.Signal;
import peakml.math.Statistical;
import peakml.util.Pair;

public class PeakComparer implements IPeak.RelationCompare<IPeak> {
	private final HashMap<Integer,double[]> intensity_courses;
	private final Header header;
	private final CorrelationMeasure measure;
	private final boolean ignoreIntensity;
	private final double minCorrSignals;
	
	public PeakComparer(final HashMap<Integer,double[]> intensity_courses, final Header header,
			final CorrelationMeasure measure, final boolean ignoreIntensity, final double minCorrSignals) {
		this.intensity_courses = intensity_courses;
		this.header = header;
		this.measure = measure;
		this.ignoreIntensity = ignoreIntensity;
		this.minCorrSignals = minCorrSignals;
	}
	
	public boolean related(IPeak peak1, IPeak peak2)
	{
		// retrieve the intensity-courses
		double intensity_course1[] = intensity_courses.get(peak1.getPatternID());
		if (intensity_course1 == null)
		{
			intensity_course1 = RelatedPeaks.getIntensityCourse(peak1, header);
			Statistical.normalize(intensity_course1);
			intensity_courses.put(peak1.getPatternID(), intensity_course1);
		}
		double intensity_course2[] = intensity_courses.get(peak2.getPatternID());
		if (intensity_course2 == null)
		{
			intensity_course2 = RelatedPeaks.getIntensityCourse(peak2, header);
			Statistical.normalize(intensity_course2);
			intensity_courses.put(peak2.getPatternID(), intensity_course2);
		}
		assert intensity_course1.length == intensity_course2.length;
		
		double stddev_course1 = Statistical.stddev(intensity_course1);
		double stddev_course2 = Statistical.stddev(intensity_course2);
		if (stddev_course1<0.1 && stddev_course2>0.15)
			return false;
		double corr_course = Statistical.pearsonsCorrelation(intensity_course1, intensity_course2)[Statistical.PEARSON_CORRELATION];
		
		// retrieve the correlations of the signals
		IPeakSet<MassChromatogram<Peak>> peakset1 = (IPeakSet<MassChromatogram<Peak>>) peak1;
		IPeakSet<MassChromatogram<Peak>> peakset2 = (IPeakSet<MassChromatogram<Peak>>) peak2;
		
		double corr_signals[] = new double[header.getNrMeasurementInfos()];
		for (int i=0; i<header.getNrMeasurementInfos(); ++i)
		{
			MassChromatogram<Peak> mc1=null, mc2=null;
			MeasurementInfo measurement = header.getMeasurementInfo(i);
			int measurementid = measurement.getID();
			for (MassChromatogram<Peak> _mc1 : peakset1)
				if (measurementid == _mc1.getMeasurementID()) { mc1=_mc1; break; }
			for (MassChromatogram<Peak> _mc2 : peakset2)
				if (measurementid == _mc2.getMeasurementID()) { mc2=_mc2; break; }
			
			if (mc1==null || mc2==null)
				corr_signals[i] = 0;
			else {
				corr_signals[i] = measure.correlation(mc1.getSignal(), mc2.getSignal());
				//corr_signals[i] = mc1.getSignal().pearsonsCorrelation(mc2.getSignal())[Statistical.PEARSON_CORRELATION];
			}
		}
		
		// check whether the correlation is significant
		double max_corr_signals = Statistical.max(corr_signals);
		if (stddev_course1 < 0.1) {
			return max_corr_signals>minCorrSignals;
		} else if ( ignoreIntensity ) {
			return max_corr_signals>minCorrSignals;
		} else if ( intensity_course1.length == 1 ) {
			return max_corr_signals>minCorrSignals;
		} else {
			return max_corr_signals>minCorrSignals && corr_course>0.75;
		}
	}
	
	public static class PearsonMeasure implements CorrelationMeasure {
		public double correlation(Signal signal1, Signal signal2) {
			if ( signal1 == null || signal2 == null ) {
				return 0.0;
			}
			return signal1.pearsonsCorrelation(signal2)[Statistical.PEARSON_CORRELATION];
		}
	}
	
	public static class CosineMeasure implements CorrelationMeasure {
		public double correlation(final Signal signal1, final Signal signal2) {
			final Pair<float[],float[]> signals = synchronizedValues(signal1, signal2);
			final float[] xvals = signals.v1;
			final float[] yvals = signals.v2;
			
			final float xy = dotProduct(xvals, yvals);
			final float xx = dotProduct(xvals, xvals);
			final float yy = dotProduct(yvals, yvals);
			assert ! Float.isNaN(xy);
			assert ! Float.isNaN(xx);
			assert ! Float.isNaN(yy);
			
			float corr = xy / (float)Math.sqrt(xx * yy);
			if ( xx == 0.0f || yy == 0.0f ) {
				corr = 0.0f;
			}
			assert ! Float.isNaN(corr) : xy + " " + xx + " " + yy;
			return corr;
		}
		
		private Pair<float[],float[]> synchronizedValues(final Signal signal1, final Signal signal2) {
			final Pair<float[],float[]> signal1values = preparedValues(signal1);
			final float[] signal1x = signal1values.v1;
			final float[] signal1y = signal1values.v2;
			
			final Pair<float[],float[]> signal2values = preparedValues(signal2);
			final float[] signal2x = signal2values.v1;
			final float[] signal2y = signal2values.v2;
			
			final int totalSize = signal1.getSize() + signal2.getSize();
			
			float xvals[] = new float[totalSize];
			float yvals[] = new float[totalSize];
			
			for (int i = 0; i < signal1x.length; ++i) {
				xvals[i] = signal1x[i];
				yvals[i] = getY(signal1x[i], signal2x, signal2y);
			}
			
			for (int i = 0; i < signal2x.length; ++i) {
				xvals[i + signal1x.length] = signal2x[i];
				yvals[i + signal1x.length] = getY(signal2x[i], signal1x, signal1y);
			}
			
			return new Pair<float[],float[]>(xvals, yvals);
		}
		
		private static Pair<float[],float[]> preparedValues(final Signal signal) {
			final Signal signalCopy = new Signal(signal);
			signalCopy.normalize();
			final double[][] signalvalues = signalCopy.getXY();
			final double[] signalx = signalvalues[0];
			final double[] signaly = signalvalues[1];
			final int[] signalIndices = Common.sortedIndices(signalx);
			final float[] sortedSignalx = new float[signalx.length];
			final float[] sortedSignaly = new float[signaly.length];
			for (int i = 0; i < signalx.length; ++i) {
				sortedSignalx[i] = (float)signalx[signalIndices[i]];
				sortedSignaly[i] = (float)signaly[signalIndices[i]];
			}
			return new Pair<float[],float[]>(sortedSignalx, sortedSignaly);
		}
		
		private static float getY(final float x, final float[] xvals, final float[] yvals) {
			if (x<xvals[0] || xvals[xvals.length-1]<x) {
				return 0.0f;
			}
		
			float ymin = 0.0f;
			float ymax = 0.0f;
			float xmin = 0.0f;
			float xmax = 0.0f;
			for (int i=0; i<xvals.length; ++i)
			{
				if (xvals[i] == x) {
					return yvals[i];
				} else if (xvals[i] < x) {
					ymin = yvals[i];
					xmin = xvals[i];
				} else {
					ymax = yvals[i];
					xmax = xvals[i];
					break;
				}
			}
			final float xdiff = xmax - xmin;
			final float otherdiff = x - xmin;
			final float ydiff = ymax - ymin;
			final float yval = otherdiff / xdiff * ydiff + ymin;
			
			return yval;
		}
		
		private float dotProduct(final float[] a, final float[] b) {
			assert a.length == b.length;
			
			float accum = 0.0f;
			
			for (int i = 0; i < a.length; ++i) {
				accum += a[i] * b[i];
			}
			return accum;
		}
	}
}