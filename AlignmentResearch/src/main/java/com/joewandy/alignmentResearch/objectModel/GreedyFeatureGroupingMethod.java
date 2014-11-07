package com.joewandy.alignmentResearch.objectModel;

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
import com.joewandy.alignmentResearch.matrix.LinkedSparseMatrix;

import domsax.XmlParserException;

public class GreedyFeatureGroupingMethod extends BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	private static final String MATRIX_SAVE_PATH = "/home/joewandy/mat/";
	private static final int MASS_TOLERANCE_PPM = 3;
	private static final double MIN_CORR_SIGNALS = 0.90;
	private double rtTolerance;
	private boolean usePeakShape;
	
	/**
	 * Creates a simple grouper
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 */
	public GreedyFeatureGroupingMethod(double rtTolerance, boolean usePeakShape) {
		this.rtTolerance = rtTolerance;
		this.usePeakShape = usePeakShape;
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
			feature.clearGroups();
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

			final PeakComparer comparer = new PeakComparer(intensity_courses, header, measure, false, MIN_CORR_SIGNALS);
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
			if (data.getFeaturesCount() < 10000) {
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
			if (data.getFeaturesCount() < 10000) {
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
		if (usePeakShape) {
			
			String filename = data.getFilenameWithoutExtension() + ".greedy." + rtTolerance + ".mat";	
			String fullPath = MATRIX_SAVE_PATH + filename;
					
			System.err.println("Saving clustering output");
			MLDouble ZZProbMat = new MLDouble("ZZ_all", toArray(ZZprob));
			final Collection<MLArray> output1 = new ArrayList<MLArray>();
			output1.add(ZZProbMat);
			final MatFileWriter writer = new MatFileWriter();
			try {
				writer.write(fullPath, output1);
				System.err.println("Written to " + fullPath);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
	}
	
	private double[][] toArray(Matrix matrix) {
		double[][] arr = new double[matrix.numRows()][matrix.numColumns()];
		for (MatrixEntry e : matrix) {
			int i = e.row();
			int j = e.column();
			double val = e.get();
			arr[i][j] = val;
		}
		return arr;
	}
	
}
