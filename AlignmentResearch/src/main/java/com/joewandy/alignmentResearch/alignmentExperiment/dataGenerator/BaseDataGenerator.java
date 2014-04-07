package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.comparator.NaturalOrderFilenameComparator;
import com.joewandy.alignmentResearch.noiseModel.AlignmentNoise;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;


public abstract class BaseDataGenerator implements AlignmentDataGenerator {

	private List<AlignmentNoise> noiseModels;
	
	public BaseDataGenerator() {
		this.noiseModels = new ArrayList<AlignmentNoise>();
	}
	
	public void addNoise(AlignmentNoise noiseModel) {
		this.noiseModels.add(noiseModel);
	}
	
	public AlignmentData generate() {
		
		//generate alignment data
		List<AlignmentFile> alignmentFiles = this.getAlignmentFiles();
		GroundTruth groundTruth = this.getGroundTruth();
		AlignmentData alignmentData = new AlignmentData(alignmentFiles, groundTruth);
		
		// introduce noise to the generated data
		for (AlignmentNoise noiseModel : noiseModels) {
			noiseModel.addNoise(alignmentData);
		}
		
		return alignmentData;
		
	}
	
	public AlignmentData generateRandomly(int n) {
		
		// shuffle and take only the top n entries
		Map<File, AlignmentFile> fileMap = new HashMap<File, AlignmentFile>();
		List<File> fileList = new ArrayList<File>();
		for (AlignmentFile af : this.getAlignmentFiles()) {
			fileMap.put(af.getFile(), af);
			fileList.add(af.getFile());
		}
		Collections.shuffle(fileList);
		List<File> sublist = fileList.subList(0, n);
		Collections.sort(sublist, new NaturalOrderFilenameComparator()); 			
		
		List<AlignmentFile> selectedAlignmentFiles = new ArrayList<AlignmentFile>();
		for (File f : sublist) {
			AlignmentFile ret = fileMap.get(f);
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
	
	public AlignmentData generateByIndices(int[] indices) {

		// pick files by indices
		List<AlignmentFile> allFiles = this.getAlignmentFiles();
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
	
	protected abstract List<AlignmentFile> getAlignmentFiles();

	protected abstract void setAlignmentFiles(List<AlignmentFile> files);
	
	protected abstract GroundTruth getGroundTruth();
	
}
