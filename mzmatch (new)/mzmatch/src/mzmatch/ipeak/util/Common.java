package mzmatch.ipeak.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import mzmatch.ipeak.sort.Parameters;

import org.apache.commons.math3.stat.StatUtils;

import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.chemistry.Polarity;
import peakml.io.Header;
import peakml.io.MeasurementInfo;
import peakml.io.ScanInfo;

public class Common {
	
	public static final double onePPM = Math.log(1000001) - Math.log(1000000);
	public static final double log2PI = Math.log(2 * Math.PI);
	
	public static double sum(final int[] array) {
		int accum = 0;
		
		for (int d : array) {
			accum += d;
		}
		return accum;
	}
	
	public static double sum(final double[] array) {
		double accum = 0.0;
		
		for (double d : array) {
			accum += d;
		}
		return accum;
	}
	
	public static void normalise(final double[] array) {
		final double s = sum(array);
		
		for (int i = 0; i < array.length; ++i) {
			array[i] = array[i] / s;
		}
	}
	
	public static String join(String[] strings, CharSequence separator) {
		return join(Arrays.asList(strings), separator);
	}
	
	public static String join(Iterable<? extends Object> elements, CharSequence separator) {
	    StringBuilder builder = new StringBuilder();

	    if (elements != null) {
	        Iterator<? extends Object> iter = elements.iterator();
	        if ( iter.hasNext() ) {
	            builder.append( String.valueOf( iter.next() ) );
	            while ( iter.hasNext() ) {
	                builder
	                    .append( separator )
	                    .append( String.valueOf( iter.next() ) );
	            }
	        }
	    }
	    return builder.toString();
	}

	public static boolean isBPorLabelled(IPeak p) {
		final Annotation a = p.getAnnotation(Annotation.relationship);
		if ( a == null ) {
			return false;
		}
		final String peakLabel = a.getValueAsString();
		boolean retval = ! (peakLabel.equals("fragment?") || peakLabel.equals("potential bp") ||
				peakLabel.equals("centroid artefact"));
		return retval;
	}
	
	public static String getId(final IPeak peak) {
		final Annotation a = peak.getAnnotation("id");
		if ( a == null ) {
			return null;
		}
		return a.getValue();
	}

	public static int getIdAsInt(final IPeak peak) {
		return Integer.parseInt(getId(peak));
	}
	
	public static <T extends IPeak> List<T> getBasepeaks(final IPeakSet<T> peaks) {
		final List<T> retval = new ArrayList<T>();
		getRelationship(peaks, "bp", retval, false);
		return retval;
	}
	
	public static <T extends IPeak> void getBasepeaks(final IPeakSet<T> peaks, Collection<T> retval) {
		getRelationship(peaks, "bp", retval, false);
	}
	
	public static <T extends IPeak> void getNonBasepeaks(final IPeakSet<T> peaks, Collection<T> retval) {
		getRelationship(peaks, "bp", retval, true);
	}
	
	public static <T extends IPeak> void getRelationship(final IPeakSet<T> peaks,
			String relationName, Collection<T> retval, boolean complement) {
		for (T peak : peaks) {
			Annotation a = peak.getAnnotation(Annotation.relationship);
			boolean hasLabel = a != null && a.getValueAsString().equals(relationName);
			if ( complement != hasLabel ) {
				retval.add(peak);
			}
		}
	}
	
	public static <T> List<T> listDifference(final Collection<T> list1, final Collection<T> list2) {
		ArrayList<T> retval = new ArrayList<T>(list1);
		retval.removeAll(list2);
		return retval;
	}
	
	public static <T> List<T> listIntersection(final Collection<T> list1, final Collection<T> list2) {
		ArrayList<T> retval = new ArrayList<T>(list1);
		retval.retainAll(list2);
		return retval;
	}
	
	public static <T extends IPeak> Map<T, T> getBasepeakMapping(IPeakSet<T> peaks,
			Map<Integer,T> clusterBasepeakMapping, String relationName) {
		Map<T, T> basepeakMapping = new HashMap<T, T>();
		
		for (T peak : peaks) {
			int clusterId = peak.getAnnotation(relationName).getValueAsInteger();
			T basepeak = clusterBasepeakMapping.get(clusterId);
			basepeakMapping.put(peak, basepeak);
		}
		return basepeakMapping;
	}
	
	public static <T extends IPeak> Map<T,Vector<T>> getClusterMapping(IPeakSet<T> peaks,
			Map<T, T> basepeakMapping, String relationName) {
		Map<T,Vector<T>> clusterMapping = new HashMap<T,Vector<T>>();
		List<T> basepeaks = new ArrayList<T>();
		getBasepeaks(peaks, basepeaks);
		
		for (T basepeak : basepeaks) {
			clusterMapping.put(basepeak, new Vector<T>());
		}
		
		for (T peak : peaks) {
			final T basepeak = basepeakMapping.get(peak);
			if ( peak.getPatternID() != basepeak.getPatternID() ) {
				Annotation a = peak.getAnnotation(Annotation.relationship);
				if (a != null) {
					assert ! a.getValueAsString().equals("bp");
				}
				clusterMapping.get(basepeak).add(peak);
			}
		}
		for (Vector<T> cPeaks : clusterMapping.values()) {
			for (T cPeak: cPeaks) {
				Annotation a = cPeak.getAnnotation(Annotation.relationship);
				if (a != null) {
					assert ! a.getValueAsString().equals("bp");
				}
			}
		}
		
		return clusterMapping;
	}
	
	public static <T extends IPeak> Map<Integer,T> getClusterBasepeakMapping(IPeakSet<T> peaks, String relationName) {
		Map<Integer,T> clusterBasepeakMapping = new HashMap<Integer,T>();
		
		for (T peak : peaks) {
			assert peak.getAnnotation(Annotation.relationship) == null;
			int clusterId = peak.getAnnotation(relationName).getValueAsInteger();
			IPeak basepeak = clusterBasepeakMapping.get(clusterId);
			if (basepeak == null) {
				clusterBasepeakMapping.put(clusterId, peak);
				peak.addAnnotation(Annotation.relationship, "bp");
			}
		}
		return clusterBasepeakMapping;
	}


	public static final double[] getIntensityCourse(IPeak peak, Header header)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			double intensitycourse[] = new double[header.getNrMeasurementInfos()];
			for (int i=0; i<intensitycourse.length; ++i)
				intensitycourse[i] = 0;
			for (IPeak p : (IPeakSet<? extends IPeak>) peak)
				intensitycourse[header.indexOfMeasurementInfo(p.getMeasurementID())] = p.getIntensity();
			return intensitycourse;
		}
		
		return new double[]{peak.getIntensity()};
	}
	
	public static String arrayToString(final double[] array) {
		final StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < array.length; ++i) {
			builder.append(array[i]);
			if ( i != array.length - 1 ) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	public static String arrayToString(final float[] array) {
		final StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < array.length; ++i) {
			builder.append(array[i]);
			if ( i != array.length - 1 ) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	public static String arrayToString(final int[] array) {
		final StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < array.length; ++i) {
			builder.append(array[i]);
			if ( i != array.length - 1 ) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	public static String arrayToString(final boolean[] array) {
		final StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < array.length; ++i) {
			builder.append(array[i]);
			if ( i != array.length - 1 ) {
				builder.append(",");
			}
		}
		builder.append("]");
		return builder.toString();
	}
	
	public static int[] sortedIndices(final double[] values) {
		final Integer[] idx = new Integer[values.length];
		for (int i = 0; i < idx.length; ++i) {
			idx[i] = i;
		}
		
		Arrays.sort(idx, new Comparator<Integer>() {
		    public int compare(final Integer o1, final Integer o2) {
		        return Double.compare(values[o1], values[o2]);
		    }
		});
		final int[] retval = new int[values.length];
		for (int i = 0; i < idx.length; ++i) {
			retval[i] = idx[i];
		}
		return retval;
	}
	
	public static <T extends Comparable<T>> int argmax(final List<T> numbers) {
		T n = null;
		int pos = -1;
		
		for (int i = 0; i < numbers.size(); ++i) {
			final T number = numbers.get(i);
			if ( n == null || number.compareTo(n) > 0 ) {
				n = number;
				pos = i;
			}
		}
		return pos;
	}
	
	public static double sum(double[] array, final boolean ignoreNaN) {
		double sum = 0.0;
		if ( ignoreNaN ) {
			for (int i = 0; i < array.length; ++i) {
				if ( ! Double.isNaN(array[i]) ) {
					sum += array[i];
				}
			}
		} else {
			for (int i = 0; i < array.length; ++i) {
				if ( Double.isNaN(array[i]) ) {
					return Double.NaN;
				} else {
					sum += array[i];
				}
			}
		}
		return sum;
	}
	
	public static int nanCount(double[] array) {
		int nanCount = 0;
		
		for (int i = 0; i < array.length; ++i) {
			if ( Double.isNaN(array[i]) ) {
				nanCount++;
			}
		}
		return nanCount;
	}
	
	public static double[] removeNaN(double[] array) {
		int nanCount = nanCount(array);

		final double[] retval = new double[array.length - nanCount];
		int index = 0;
		for (int i = 0; i < array.length; ++i) {
			if ( ! Double.isNaN(array[i]) ) {
				retval[index++] = array[i];
			}
		}
		return retval;
	}
	
	public static double[] flatten(double[][] array) {
		final int numRows = array.length;
		final int numColumns = array[0].length;
		final double[] retval = new double[numRows * numColumns];
		
		int index = 0;
		for ( int i = 0; i < numRows; ++i ) {
			for ( int j = 0; j < numColumns; ++j ) {
				retval[index++] = array[i][j];
			}
		}
		return retval;
	}
	
	public static double mean(double[] array, final boolean ignoreNaN) {
		if ( ignoreNaN ) {
			return StatUtils.mean(removeNaN(array));
		}
		return StatUtils.mean(array);
	}
	
	public static double mean(double[][] array, final boolean ignoreNaN) {
		return mean(flatten(array), ignoreNaN);
	}
	
	public static double variance(double[] array, final boolean ignoreNaN) {
		if ( ignoreNaN ) {
			return StatUtils.variance(removeNaN(array));
		}
		return StatUtils.variance(array);
	}
	
	public static double variance(double[][] array, final boolean ignoreNaN) {
		return variance(flatten(array), ignoreNaN);
	}
 	
	public static double max(double[][] array, final boolean ignoreNaN) {
		final double[] tempArray = new double[array.length];
		
		for (int i = 0; i < array.length; ++i) {
			tempArray[i] = max(array[i], ignoreNaN);
		}
		return max(tempArray, ignoreNaN);
	}
	
	public static double min(double[][] array, final boolean ignoreNaN) {
		final double[] tempArray = new double[array.length];
		
		for (int i = 0; i < array.length; ++i) {
			tempArray[i] = min(array[i], ignoreNaN);
		}
		return min(tempArray, ignoreNaN);
	}
	
	public static double max(double[] array, boolean ignoreNaN) {
		assert array.length > 0;
	    double max = array[0];
	    for (int i = 1; i < array.length; i++) {
	    	max = ignoreNaN ? nanmax(max, array[i]) : Math.max(max, array[i]);
	    }
	    return max;
	}
	
	public static double min(double[] array, boolean ignoreNaN) {
		assert array.length > 0;
	    double min = array[0];
	    for (int i = 1; i < array.length; i++) {
	    	min = ignoreNaN ? nanmin(min, array[i]) : Math.max(min, array[i]);
	    }
	    return min;
	}
	
	public static double nanmax(final double x, final double y) {
		if ( Double.isNaN(x) || x < y ) {
			return y;
		}
		return x;
	}
	
	public static double nanmin(final double x, final double y) {
		if ( Double.isNaN(x) || x > y ) {
			return y;
		}
		return x;
	}
	
	public static Polarity getPeakPolarity(final IPeak peak, final Header header) {
		final int measurementId = peak.getMeasurementID();
		final MeasurementInfo measurement = header.getMeasurementInfo(measurementId);
		assert measurement != null : "Measurement ID: " + measurementId + " Type: " + peak.getClass();
		final int scanId = peak.getScanID();
		final ScanInfo scan = measurement.getScanInfo(scanId);
		assert scan != null;
		return scan.getPolarity();
	}
	
	public static double normalDensity(final double x, final double mu, final double precision, final boolean logged) {
		final double xMinusMu = x - mu;
		final double logDensity = 0.5 * ( Math.log(precision) - log2PI - precision * xMinusMu * xMinusMu );
		return logged ? logDensity : Math.exp(logDensity);
	}
	
	public static void printlnIfDebug(final String string, final Parameters parameters) {
		if ( parameters.debug ) System.err.println(string);
	}

}
