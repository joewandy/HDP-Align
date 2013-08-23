package com.joewandy.alignmentResearch.alignmentExperiment;

import java.util.List;

import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

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
	
}
