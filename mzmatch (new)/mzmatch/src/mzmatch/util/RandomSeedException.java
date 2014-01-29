package mzmatch.util;

public class RandomSeedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RandomSeedException(final long seed) {
		super("Random seed is: " + seed);
	}
	
	public RandomSeedException(final long seed, final Throwable t) {
		super("Random seed is: " + seed, t);
	}
}
