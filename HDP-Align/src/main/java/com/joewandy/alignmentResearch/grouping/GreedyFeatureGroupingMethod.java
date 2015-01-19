package com.joewandy.alignmentResearch.grouping;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mzmatch.ipeak.sort.CorrelationMeasure;
import mzmatch.ipeak.sort.IdentifyPeaksets;
import mzmatch.ipeak.sort.PeakComparer;
import mzmatch.ipeak.sort.RelatedPeaks;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.jmatio.types.MLSparse;
import com.joewandy.alignmentResearch.matrix.LinkedSparseMatrix;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.FeatureGroup;

import domsax.XmlParserException;

public class GreedyFeatureGroupingMethod extends BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	private static final String MATRIX_SAVE_PATH = "/home/joewandy/mat/";
	private static final int MASS_TOLERANCE_PPM = 3;
	private double rtTolerance;
	private boolean usePeakShape;
	private double minCorrSignal;
	
	/**
	 * Creates a simple grouper
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 */
	public GreedyFeatureGroupingMethod(double rtTolerance, boolean usePeakShape, double minCorrSignal) {
		this.rtTolerance = rtTolerance;
		this.usePeakShape = usePeakShape;
		this.minCorrSignal = minCorrSignal;
	}
	
	@Override
	public List<FeatureGroup> group(List<AlignmentFile> dataList) {
		System.out.println("============ Grouping = " + dataList.size() + " files ============");
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();
		for (AlignmentFile data : dataList) {
			List<FeatureGroup> fileGroups = this.group(data);
			groups.addAll(fileGroups);							
		}
		return groups;
	}

	@Override
	public List<FeatureGroup> group(AlignmentFile data) {

		// wipe out all existing grouping information first
		for (Feature feature : data.getFeatures()) {
			feature.clearGroupID();
		}
		
		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();
		double rtTolerance = this.rtTolerance; 
		if (!usePeakShape) {
			// do greedy grouping, based on retention time only
			fileGroups = greedyRTGrouping(data, rtTolerance);			
		} else {
			// call mzmatch to do greedy peakshape correlation grouping
			fileGroups = greedyPeakShapeGrouping(data, rtTolerance);
		}		
		System.out.println("fileGroups.size() = " + fileGroups.size());

		// create assignment matrix of peaks vs groups
		setAssignmentMatrix(data, fileGroups);
		return fileGroups;
					
	}

	private List<FeatureGroup> greedyPeakShapeGrouping(AlignmentFile data, double rtTolerance) {

		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();
		
		final String filename = data.getFilenameWithoutExtension() + ".filtered.peakml";	
		final String fullPath = data.getParentPath() + "/" + filename;	

		try {

			ParseResult result = PeakMLParser.parseIPeakSet(new FileInputStream(fullPath), null);
			final Header header = result.header;
			final IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
			IdentifyPeaksets.identify(peaks);
			
			// annotate the peaks with an id for the hashing
			int id = 0;
			for (IPeak peak : peaks) {
				peak.setPatternID(id++);
			}
			
			// match the peaks
			final HashMap<Integer,double[]> intensity_courses = new HashMap<Integer,double[]>();
			CorrelationMeasure measure = new PeakComparer.PearsonMeasure();

			final PeakComparer comparer = new PeakComparer(intensity_courses, header, measure, false, minCorrSignal);
			List<IPeak> basePeaks = IPeak.findRelatedPeaks(peaks.getPeaks(), -1, rtTolerance, comparer);
			assert basePeaks != null;
			RelatedPeaks.labelRelationships(peaks, true, MASS_TOLERANCE_PPM);
			
			// the group ids must be unique across all input files ?!
			int groupId = 1;			
			for (IPeak basePeak : basePeaks) {

				// find related peaks to this basePeak (including itself)
				Set<Feature> relatedFeatures = new HashSet<Feature>();
				int basePeakCluster = basePeak.getAnnotation(IPeak.relationid).getValueAsInteger();
				for (IPeak relatedPeak : peaks) {
					int relatedPeakCluster = relatedPeak.getAnnotation(IPeak.relationid).getValueAsInteger();
					if (basePeakCluster == relatedPeakCluster) {
						int patternId = relatedPeak.getPatternID();
						Feature relatedFeature = data.getFeatureByIndex(patternId);
						relatedFeatures.add(relatedFeature);
					}
				}				
				
				FeatureGroup group = new FeatureGroup(groupId);
				groupId++;
				group.addFeatures(relatedFeatures);
				fileGroups.add(group);
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlParserException e) {
			e.printStackTrace();
		}
		
		return fileGroups;
	
	}

	private List<FeatureGroup> greedyRTGrouping(AlignmentFile data, double rtTolerance) {

		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();
		
		// the group ids must be unique across all input files ?!
		int groupId = 1;
		
		System.out.println("Grouping " + data.getFilename() + " rtTolerance " + rtTolerance);
		for (Feature feature : data.getFeatures()) {

			// process ungrouped features
			if (!feature.isGrouped()) {
				FeatureGroup group = new FeatureGroup(groupId);
				Set<Feature> nearbyFeatures = findNearbyFeatures(data, feature, rtTolerance);
				if (!nearbyFeatures.isEmpty()) {
					group.addFeatures(nearbyFeatures);
				} 
				groupId++;
				fileGroups.add(group);
			}
						
		}				

		return fileGroups;
		
	}
			
	private Set<Feature> findNearbyFeatures(AlignmentFile data, Feature referenceFeature, double rtTolerance) {
		Set<Feature> nearbyFeatures = new HashSet<Feature>();
		// find matching feature
		Set<Feature> unmatched = data.getNextUngroupedFeatures(referenceFeature, rtTolerance);
		nearbyFeatures.addAll(unmatched);
		return nearbyFeatures;
	}

	private void setAssignmentMatrix(AlignmentFile data,
			List<FeatureGroup> fileGroups) {

		if (data.getZZProb() == null) {
		
			System.out.println("Computing Z");
			Matrix Z = null;
			if (data.getFeaturesCount() < 1000) {
				Z = new DenseMatrix(data.getFeaturesCount(), fileGroups.size());				
			} else {
				Z = new LinkedSparseMatrix(data.getFeaturesCount(), fileGroups.size());				
			}
			for (int j = 0; j < fileGroups.size(); j++) {
				FeatureGroup group = fileGroups.get(j);
				for (Feature f : group.getFeatures()) {
					int i = f.getPeakID(); // starts from 0
					Z.set(i, j, 1);
				}
			}
			
			System.out.println("Computing ZZprob");
			if (data.getFeaturesCount() < 1000) {
				ZZprob = new DenseMatrix(data.getFeaturesCount(), data.getFeaturesCount());
			} else {
				ZZprob = new LinkedSparseMatrix(data.getFeaturesCount(), data.getFeaturesCount());				
			}
			Z.transBmult(Z, ZZprob);		
			data.setZZProb(ZZprob);

		}
			
		int groupedCount = 0;
		for (Feature feature : data.getFeatures()) {
			if (feature.isGrouped()) {
				groupedCount++;
			}
		}			
		System.out.println("groupedCount = " + groupedCount);

		// save the clustering output to mat file as well
		String filename = null;
		String rtToleranceStr = String.format("%.1f", rtTolerance);
		String minCorrSignalStr = String.format("%.2f", minCorrSignal);
		if (usePeakShape) {
			filename = data.getFilenameWithoutExtension() + ".greedy_peakshape." + rtToleranceStr + "_" + minCorrSignalStr + ".mat";	
		} else {
			filename = data.getFilenameWithoutExtension() + ".greedy_rt." + rtToleranceStr + ".mat";	
		}			
		String fullPath = MATRIX_SAVE_PATH + filename;
					
		System.err.println("Saving clustering output");
		int nzmax = getNnz(ZZprob);
		int nRows = ZZprob.numRows();
		int nCols = ZZprob.numColumns();

//		System.out.println("nzmax = " + nzmax);
//		System.out.println("nRows = " + nRows);
//		System.out.println("nCols = " + nCols);
		MLSparse ZZProbMat = new MLSparse("ZZ_all", new int[]{nRows, nCols}, 0, nzmax);
		setValue(ZZprob, ZZProbMat);
		final Collection<MLArray> output1 = new ArrayList<MLArray>();
		output1.add(ZZProbMat);
		final MatFileWriter writer = new MatFileWriter();
		try {
			writer.write(fullPath, output1);
			System.out.println("Written to " + fullPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private int getNnz(Matrix matrix) {
		int nnz = 0;
		for (MatrixEntry e : matrix) {
			nnz++;
		}
		return nnz;
	}
	
	private void setValue(Matrix matrix, MLSparse mat) {
		for (MatrixEntry e : matrix) {
			int m = e.row();
			int n = e.column();
			double val = e.get();
			mat.set(val, m, n);
		}
	}
	
	public static void main(String[] args) {
		
		int nRows = 10;
		int nCols = 10;
		MLSparse test = new MLSparse("ZZ_all", new int[]{nRows, nCols}, 0, 6);
		test.set(1.0, 0, 0);
		test.set(2.0, 1, 0);
		test.set(3.0, 2, 0);
		test.set(4.0, 0, 1);
		test.set(5.0, 1, 1);
		test.set(6.0, 2, 1);
		System.out.println(test.contentToString());

		final Collection<MLArray> output1 = new ArrayList<MLArray>();
		output1.add(test);
		final MatFileWriter writer = new MatFileWriter();
		try {
			String fullPath = "/home/joewandy/mat/test.mat";
			writer.write(fullPath, output1);
			System.err.println("Written to " + fullPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
}
