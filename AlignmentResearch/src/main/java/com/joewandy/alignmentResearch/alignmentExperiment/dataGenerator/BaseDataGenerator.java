package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.noiseModel.AlignmentNoise;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;


public abstract class BaseDataGenerator implements AlignmentDataGenerator {

	protected List<AlignmentNoise> noiseModels;
	protected List<AlignmentFile> alignmentFiles;
	protected boolean verbose;
	protected int gtCombinationSize;
	
	public BaseDataGenerator() {
		this.noiseModels = new ArrayList<AlignmentNoise>();
		this.alignmentFiles = new ArrayList<AlignmentFile>();
	}
	
	public void addNoise(AlignmentNoise noiseModel) {
		this.noiseModels.add(noiseModel);
	}
	
	public AlignmentData generate() {
		
		//generate alignment data
		List<AlignmentFile> alignmentFiles = this.getAlignmentFiles(-1);
		GroundTruth groundTruth = this.getGroundTruth();
		AlignmentData alignmentData = new AlignmentData(alignmentFiles, groundTruth);
		
		// introduce noise to the generated data
		for (AlignmentNoise noiseModel : noiseModels) {
			noiseModel.addNoise(alignmentData);
		}
		
		// do post-processing
//		for (AlignmentFile file : alignmentFiles) {
//			Iterator<Feature> it = file.getFeatures().iterator();
//			while (it.hasNext()) {
//				Feature f = it.next();
//				if (f.getIntensity() < 1E8) {
//					it.remove();
//					// remove from gt too
//					groundTruth.clearFeature(f);
//				}
//			}
//		}
//		groundTruth.buildPairwise();
		
		return alignmentData;
		
	}

	public AlignmentData generateByIteration(int currentIter) {
		
		//generate alignment data
		List<AlignmentFile> alignmentFiles = this.getAlignmentFiles(currentIter);
		GroundTruth groundTruth = this.getGroundTruth();
		AlignmentData alignmentData = new AlignmentData(alignmentFiles, groundTruth);
		
		// introduce noise to the generated data
		for (AlignmentNoise noiseModel : noiseModels) {
			noiseModel.addNoise(alignmentData);
		}
		
		return alignmentData;
		
	}
	
	public AlignmentData generateByIndices(int[] indices) {

		// pick files by indices
		List<AlignmentFile> allFiles = this.getAlignmentFiles(-1);
		List<AlignmentFile> selectedAlignmentFiles = new ArrayList<AlignmentFile>();
		for (int i : indices) {
			int pos = i - 1; // substract 1 since we index from 0 .. n-1 files
			AlignmentFile ret = allFiles.get(pos);
			selectedAlignmentFiles.add(ret);
		}

		// must do this line first, because ground truth construction retains only entries in files that are used
		this.setAlignmentFiles(selectedAlignmentFiles); 
		GroundTruth groundTruth = this.getGroundTruth();

		AlignmentData alignmentData = new AlignmentData(selectedAlignmentFiles, groundTruth);
		
		// introduce noise to the generated data
		for (AlignmentNoise noiseModel : noiseModels) {
			noiseModel.addNoise(alignmentData);
		}
		
		return alignmentData;
		
	}
	
	protected List<AlignmentFile> getAlignmentFiles(int currentIter) {
		return alignmentFiles;
	}

	protected void setAlignmentFiles(List<AlignmentFile> alignmentFiles) {
		this.alignmentFiles = alignmentFiles;
	}

	protected abstract GroundTruth getGroundTruth();
	
}
