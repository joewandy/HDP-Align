package com.joewandy.alignmentResearch.alignmentMethod.custom.maxWeight;

import java.util.List;

import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public class GreedyApprox {
	
	private List<AlignmentRow> men;
	private List<AlignmentRow> women;
	private double[][] E;
	private int nRows;
	private int nCols;
	private double massTol;
	private double rtTol;
	
	public GreedyApprox(double[][] scoreArr) {

		this.nRows = scoreArr.length;
		this.nCols = scoreArr[0].length;
		this.E = scoreArr.clone();
		
		this.men = men;
		this.women = women;
		this.massTol = massTol;
		this.rtTol = rtTol;
	
	}
	
	public int[] execute() {
	
		int[] M = new int[nRows];
		for (int i = 0; i < nRows; i++) {
			M[i] = -1;
		}
		
		MaxValue e = new MaxValue();
		int counter = 0;
		do {
			
			// let e be the heaviest edge in E
			e = findMax(E);	

			if (counter % 1000 == 0) {
				System.out.print('.');
			}
			counter++;

			// add e to M			
			M[e.row] = e.col;
		
			// remove e and all edges adjacent to e from E
			E[e.row][e.col] = 0;
			for (int j = 0; j < nCols; j++) {
				E[e.row][j] = 0;
			}
						
		} while (e.value > 0);
		System.out.println();
		
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