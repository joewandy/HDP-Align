package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.apporiented.algorithm.clustering.Cluster;
import com.apporiented.algorithm.clustering.ClusteringAlgorithm;
import com.apporiented.algorithm.clustering.CompleteLinkageStrategy;
import com.apporiented.algorithm.clustering.DefaultClusteringAlgorithm;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;

public class DendogramBuilder {

	private List<AlignmentFile> dataList;
	private ExtendedLibrary library;
	private double[][] scores;
	private double[][] dists;
	private String[] labels;
	private double massTol;
	private double rtTol;
	private boolean useGroup;
	private double alpha;
	
	public DendogramBuilder(List<AlignmentFile> dataList,
			ExtendedLibrary library, double massTol, double rtTol, boolean useGroup, double alpha) {

		this.dataList = dataList;
		this.library = library;
		this.massTol = massTol;
		this.rtTol = rtTol;
		this.useGroup = useGroup;
		this.alpha = alpha;

		int n = dataList.size();
		scores = new double[n][n];
		labels = new String[n];
		dists = new double[n][n];

		for (int i = 0; i < n; i++) {

			// store the labels of file for later use
			AlignmentFile file1 = dataList.get(i);
			labels[i] = file1.getFilenameWithoutExtension();

			for (int j = 0; j < n; j++) {

				AlignmentFile file2 = dataList.get(j);
				double score = 0;
				// skip if the same files
				if (i != j) {
					score = library.getScoresByFiles(file1, file2);					
				}
				scores[i][j] = score;

			}
		}

		// find the max score, used to normalise score to (0, 1)
		double maxScore = 0;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (scores[i][j] > maxScore) { 
					maxScore = scores[i][j];
				}
			}
		}

		// convert score to distance
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {

				scores[i][j] = scores[i][j] / maxScore;
				dists[i][j] = 1 - scores[i][j];

				System.out.println("(" + dataList.get(i).getFilenameWithoutExtension() + ", "
						+ dataList.get(j).getFilenameWithoutExtension() + ") score="
						+ String.format("%.3f", scores[i][j]) + " dist=" + String.format("%.3f", dists[i][j]));

				
			}
		}

	}

	public AlignmentList align() {

		System.out.println("Hierarchical clustering ...");
		ClusteringAlgorithm alg = new DefaultClusteringAlgorithm();
		Cluster root = alg.performClustering(dists, labels,
				new CompleteLinkageStrategy());

		Map<String, AlignmentFile> dataMap = new HashMap<String, AlignmentFile>();
		for (AlignmentFile file : dataList) {
			dataMap.put(file.getFilenameWithoutExtension(), file);
		}
		DendogramParser parser = new DendogramParser(root, dataMap, 
				library, massTol, rtTol, useGroup, alpha);
		String output = parser.traverse(2);
		System.out.println("tree");
		System.out.println(output);

		AlignmentList alignedList = parser.buildAlignment();
		return alignedList;

	}

}
