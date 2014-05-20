package com.joewandy.alignmentResearch.objectModel;

import cern.colt.Arrays;

public class ChineseRestaurant {

	/**
	 * Generates a sample from Chinese Restaurant Process
	 * @param alpha DP concentration parameter
	 * @param N num of items
	 * @return assignment vectors of the N items
	 */
	public int[] sample(double alpha, int N) {
		
		int[] assignments = new int[N]; 	// assignments for each object
		double[] counts = new double[N+1]; 	// table count (N is the max possible K)
		assignments[0] = 1; 				// assign object 1 to table 1
		counts[0] = 1; 						// adjust count
		counts[1] = alpha; 					// 'fake' counts for table K+1
		int K = 1; 							// number of unique clusters
		
		// sequentially assign other objects via CRP
		for (int i = 1; i < N; i++) {
			
			// generate random number, and convert it to a 'quasi-count'
			double u = Math.random();
			u = u * (i - 1 + alpha);		// multiply by the CRP normalising constant
			
			// find the corresponding table
			int z = 0;						// indexing variable for the table
			while (u > counts[z]) {
				u = u - counts[z];			// subtract off that probability mass
				z = z + 1;
			}
			
			// record the outcome and adjust
			assignments[i] = z;				// make the assignment
			if (z == K) { 					// if it's a new table
				counts[z] = 1;				// assign real count
				counts[z+1] = alpha;		// move the 'fake' counts to the next table
				K = K + 1;					// update the number of clusters
			} else {						// if it's an old table
				counts[z] = counts[z] + 1;	// increment count
			}			
			
		}
		
		return assignments;
		
	}
	
	public static void main(String[] args) {
		
		double alpha = 10;
		int N = 10;
		
		ChineseRestaurant res = new ChineseRestaurant();
		int[] assignments = res.sample(alpha, N);
		System.out.println(Arrays.toString(assignments));
		
	}
	
}
