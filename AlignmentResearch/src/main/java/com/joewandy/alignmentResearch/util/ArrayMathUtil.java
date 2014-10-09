package com.joewandy.alignmentResearch.util;

import org.apache.commons.math3.random.RandomData;

public class ArrayMathUtil {

	public static double sum(double[] arr) {
		double sum = 0;
		for (double elem : arr) {
			sum += elem;
		}
		return sum;
	}

	public static double sum(int[] arr) {
		double sum = 0;
		for (double elem : arr) {
			sum += elem;
		}
		return sum;
	}
	
	public static double max(double[] arr) {
		double max = Double.NEGATIVE_INFINITY;
		for (double elem : arr) {
			if (elem > max) {
				max = elem;
			}
		}
		return max;
	}
	
	public static double[] normalise(double[] arr, double denum) {
		double[] result = arr.clone();
		for (int i = 0; i < result.length; i++) {
			double currVal = result[i];
			double newVal = currVal / denum;
			result[i] = newVal;
		}
		return result;
	}
	
	public static double[] addArray(double[] arr1, double[] arr2) {
		assert(arr1.length == arr2.length);
		double[] result = arr1.clone();
		for (int i = 0; i < arr2.length; i++) {
			result[i] += arr2[i];
		}
		return result;
	}

	public static double[] subsArray(double[] arr, double scalar) {
		double[] result = arr.clone();
		for (int i = 0; i < arr.length; i++) {
			result[i] -= scalar;
		}
		return result;
	}

	public static double[] multArray(double[] arr1, int[] arr2) {
		assert(arr1.length == arr2.length);
		double[] result = arr1.clone();
		for (int i = 0; i < arr2.length; i++) {
			result[i] *= arr2[i];
		}
		return result;
	}
	
	public static double[] append(double[] arr, double scalar) {
		double[] result = new double[arr.length+1];
		int end = result.length-1;
		for (int i = 0; i < result.length; i++) {
			if (i == end) {
				result[i] = scalar;
			} else {
				result[i] = arr[i];
			}
		}
		return result;	
	}
	
	public static double[] logArray(double[] arr) {
		double[] result = arr.clone();
		for (int i = 0; i < arr.length; i++) {
			result[i] = Math.log(arr[i]);
		}
		return result;
	}

	public static double[] expArray(double[] arr) {
		double[] result = arr.clone();
		for (int i = 0; i < arr.length; i++) {
			result[i] = Math.exp(arr[i]);
		}
		return result;
	}
	
	public static double[] cumsumArray(double[] arr) {
		double[] result = new double[arr.length];
		double sum = 0;
		for (int i = 0; i < arr.length; i++) {
			sum += arr[i];
			result[i] = sum;
		}
		return result;
	}
	
	public static int sample(double[] distribution, RandomData randomData) {
		double randomNumber = randomData.nextUniform(0, 1);
		double[] cumsum = cumsumArray(distribution);
		int selectedIndex = 0;
		for (selectedIndex = 0; selectedIndex < cumsum.length; selectedIndex++) {
			double c = cumsum[selectedIndex];
			if (randomNumber <= c) {
				break;
			}
		}
		return selectedIndex;
	}
		
	public static double computeLogLikelihood(double x, double mu, double prec) {
		/*
		 * f(x) 	= sqrt(prec/2pi)*e^((-prec(x-mu)^2)/2)
		 * log f(x) = log (sqrt(prec/2pi)*e^((-prec(x-mu)^2)/2))
		 * 			= log(sqrt(prec/2pi)) + log(e^((-prec(x-mu)^2)/2))
		 * 			= 0.5 log(prec) - 0.5 log(2pi) + ((-prec(x-mu)^2)/2)
		 * 			= 0.5 log(prec) - 0.5 log(2pi) - 0.5 * prec * (x-mu)^2
		 */
		double logLikelihood = -0.5 * Math.log(2*Math.PI);
		logLikelihood += 0.5 * Math.log(prec);
		logLikelihood -= 0.5 * prec * Math.pow(x - mu, 2);
		return logLikelihood;
	}
		
	public static double[] toDouble(int[] arr) {
		double[] res = new double[arr.length];
		for (int i = 0; i < arr.length; i++) {
			res[i] = arr[i];
		}
		return res;
	}
	
}
