package com.joewandy.alignmentResearch.alignmentMethod.custom;

public class GreedyApprox {
	
	private double[][] E;
	private int nRows;
	private int nCols;
	
	public GreedyApprox(double[][] scoreArr) {
		this.E = scoreArr.clone();
		this.nRows = scoreArr.length;
		this.nCols = scoreArr[0].length;
	}
	
	public int[] execute() {
	
		int[] M = new int[nRows];
		for (int i = 0; i < nRows; i++) {
			M[i] = -1;
		}
		
		MaxValue e = new MaxValue();
		do {
			
			// let e be the heaviest edge in E
			e = findMax(E);	
			System.out.println(e);

			// add e to M			
			M[e.row] = e.col;
		
			// remove e and all edges adjacent to e from E
			E[e.row][e.col] = 0;
			for (int j = 0; j < nCols; j++) {
				E[e.row][j] = 0;
			}
			
		} while (e.value > 0);

		return M;
	
	}
	
	private MaxValue findMax(double[][] array) {
		MaxValue maxScore = new MaxValue();
		int m = array.length;
		int n = array[0].length;
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (array[i][j] > maxScore.value) {
					maxScore.value = array[i][j];
					maxScore.row = i;
					maxScore.col = j;
				}
			}
		}
		return maxScore;
	}
	
	private class MaxValue {
		private int row;
		private int col;
		private double value;
		public MaxValue() {
			this.row = -1;
			this.col = -1;
			this.value = -1;
		}
		@Override
		public String toString() {
			return "MaxValue [row=" + String.format("%5d", row) + ", col=" + String.format("%5d", col) 
					+ ", value=" + String.format("%.3f", value)
					+ "]";
		}
	}
	
}