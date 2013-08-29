package mzmatch.ipeak.combineMethod;

import java.util.List;

import mzmatch.ipeak.CombineOptions;
import peakml.IPeak;
import peakml.IPeakSet;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGenerator;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public class CombineCustomJoinMethod extends CombineBaseMethod implements CombineMethod {

	@Override
	protected List<IPeakSet<IPeak>> getMatches(
			List<IPeakSet<IPeak>> peaksets, CombineOptions options) {
		
		// convert List<IPeakSet<IPeak>> to List<AlignmentFile>
		AlignmentDataGenerator gen = new RealDataGenerator(peaksets);
		AlignmentData data = gen.generate();		
		List<AlignmentFile> dataList = data.getAlignmentDataList();
		assert(dataList.size() == peaksets.size());		
		
		// pass this to the alignment method
		AlignmentMethodParam.Builder paramBuilder = new AlignmentMethodParam.Builder(
				options.ppm, options.rtwindow);
		paramBuilder.usePpm(true);
		AlignmentMethod aligner = AlignmentMethodFactory.getAlignmentMethod(
				AlignmentMethodFactory.ALIGNMENT_METHOD_CUSTOM_JOIN, 
				paramBuilder, data);
		List<AlignmentRow> result = aligner.align();
		System.out.println("Total " + result.size() + " rows aligned");
		
		// map the result back to List<IPeakSet<IPeak>>
		List<IPeakSet<IPeak>> matches = mapAlignmentResult(peaksets, dataList,
				result);		

		return matches;

	}
	
}
