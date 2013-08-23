package mzmatch.ipeak.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.commons.math3.util.ArithmeticUtils;

import com.google.common.primitives.Ints;

public class MultinomialDistribution implements Iterable<int[]> {
	private final int n;
	private final double[] p;
	private final int k;
	//private final double[] logP;
	
	public MultinomialDistribution(final int n, final double[] p) {
		this.n = n;
		Common.normalise(p);
		this.p = p;
		k = p.length;
		//for (int i = 0; i < p.length; ++i) {
		//	logP[i] = Math.log(p[i]);
		//}
	}
	
	public Iterator<int[]> iterator() {
		return new MDIterator(n, k);
	}
	
	public int size() {
		final long s = ArithmeticUtils.binomialCoefficient(n + k - 1, k);
		return Ints.checkedCast(s);
	}
	
	public double getProbability(int[] x) {
		assert x.length == p.length;
		double accum = ArithmeticUtils.factorialLog(n);
		for (int i = 0; i < x.length; ++i) {
			final int x_i = x[i];
			assert x_i >= 0;
			accum -= ArithmeticUtils.factorialLog(x_i);
			accum += x_i * Math.log(p[i]);
		}
		final double retval = Math.exp(accum);
		assert retval >= 0.0 && retval <= 1.0;
		return retval;
	}
	
	public static class MDIterator implements Iterator<int[]> {
		private final int n;
		private final int[] state;
		private boolean first = true;
		
		public MDIterator(final int n, final int k) {
			assert n > 0;
			this.n = n;
			state = new int[k];
			state[0] = n;
		}

		public boolean hasNext() {
			if ( first ) {
				return true;
			}
			return state[state.length - 1] != n;
		}

		public int[] next() {
			if ( first ) {
				first = false;
				return state;
			}
			if ( state[state.length - 1] == n ) {
				throw new NoSuchElementException();
			}
			// Find first non-zero element starting from k - 2 down
			int element = 0;
			for (int i = state.length - 2; i > 0; i--) {
				assert state[i] >= 0;
				if ( state[i] != 0 ) {
					element = i;
					break;
				}
			}
			assert element != 0 || state[0] != 0 :
				"Element: " + element + " state[0]: " + state[0] + " state[end]: " + state[state.length - 1];
			state[element]--;
			assert state[element] >= 0;
			state[element + 1] = state[state.length - 1] + 1;
			if ( element != state.length - 2) {
				state[state.length - 1] = 0;
			}
			return state;
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
}
