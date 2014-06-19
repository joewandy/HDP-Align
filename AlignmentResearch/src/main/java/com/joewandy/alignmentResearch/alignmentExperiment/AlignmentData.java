package com.joewandy.alignmentResearch.alignmentExperiment;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;
import com.joewandy.alignmentResearch.objectModel.GroundTruthFeatureGroup;

public class AlignmentData {

	private List<AlignmentFile> alignmentDataList;
	private GroundTruth groundTruth;
	
	public AlignmentData(List<AlignmentFile> alignmentDataList,
			GroundTruth groundTruth) {
		this.alignmentDataList = alignmentDataList;
		this.groundTruth = groundTruth;
	}

	public List<AlignmentFile> getAlignmentDataList() {
		return alignmentDataList;
	}
	
	public AlignmentFile getAlignmentFileByName(String filename) {
		for (AlignmentFile file : alignmentDataList) {
			if (file.getFilename().equals(filename)) {
				return file;
			}
		}
		return null;
	}

	public AlignmentFile getAlignmentFileById(int id) {
		for (AlignmentFile file : alignmentDataList) {
			if (file.getId() == id) {
				return file;
			}
		}
		return null;
	}
	
	public GroundTruth getGroundTruth() {
		return groundTruth;
	}	
	
	public int getNoOfFiles() {
		return alignmentDataList.size();
	}
	
	public String[] getFileNames() {
		String[] fileNames = new String[getNoOfFiles()];
		int i = 0;
		for (AlignmentFile file : alignmentDataList) {
			fileNames[i] = file.getFilename();
			i++;
		}
		return fileNames;
	}

	public String[] getFileNamesNoExt() {
		String[] fileNames = new String[getNoOfFiles()];
		int i = 0;
		for (AlignmentFile file : alignmentDataList) {
			fileNames[i] = file.getFilenameWithoutExtension();
			i++;
		}
		return fileNames;
	}
	
	public void saveGroundTruth(String path) throws IOException {

		// write header
		PrintWriter pw = new PrintWriter(new FileOutputStream(path));
		Map<AlignmentFile, Integer> fileMap = new HashMap<AlignmentFile, Integer>();
		int counter = 1;
		for (AlignmentFile file : alignmentDataList) {
			fileMap.put(file, counter);
			pw.println("> " + counter + " " + file.getFilenameWithoutExtension());
			counter++;
		}
		
		// write features
		List<GroundTruthFeatureGroup> groundTruthList = this.groundTruth.getGroundTruthFeatureGroups();		
		for (GroundTruthFeatureGroup gt : groundTruthList) {
			StringBuilder sb = new StringBuilder();
			sb.append("# ");
			for (Feature f : gt.getFeatures()) {
				AlignmentFile file = f.getData();
				int fileIdx = fileMap.get(file);
				int peakID = f.getPeakID() + 1; // starts from 1 in the ground truth
				sb.append(fileIdx + " " + peakID + " ");
			}
			pw.println(sb.toString());
		}
		pw.close();
		
	}
	
}
