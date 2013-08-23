package mzmatch.ipeak.combineMethod;

import java.util.ArrayList;
import java.util.List;

import peakml.IPeak;
import peakml.IPeakSet;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGenerator;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.BaseDataGenerator;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

public class RealDataGenerator extends BaseDataGenerator implements AlignmentDataGenerator {

	private static final String PREFIX_SAMPLE = "SAMPLE_";
	private List<IPeakSet<IPeak>> samples;
	
	public RealDataGenerator(List<IPeakSet<IPeak>> samples) {
		super();
		this.samples = samples;
	}
	
	@Override
	protected List<AlignmentFile> getAlignmentFiles() {

		List<AlignmentFile> dataList = new ArrayList<AlignmentFile>();
		int sampleId = 0;
		for (IPeakSet<IPeak> sample : samples) {

			List<IPeak> massChromatograms = sample.getPeaks();
			List<Feature> featureList = new ArrayList<Feature>();
			
			for (IPeak massChromatogram : massChromatograms) {
				int peakId = massChromatogram.getPatternID();
				double mass = massChromatogram.getMass();
				double rt = massChromatogram.getRetentionTime();
				double intensity = massChromatogram.getIntensity();
				Feature feature = new Feature(peakId, mass, rt, intensity);
				featureList.add(feature);
			}
			
			AlignmentFile data = new AlignmentFile(sampleId, 
					RealDataGenerator.PREFIX_SAMPLE + sampleId, featureList);
			dataList.add(data);
			sampleId++;
			
		}
		
		return dataList;

	}

	@Override
	protected GroundTruth getGroundTruth() {
		// TODO Auto-generated method stub
		return null;
	}

}
