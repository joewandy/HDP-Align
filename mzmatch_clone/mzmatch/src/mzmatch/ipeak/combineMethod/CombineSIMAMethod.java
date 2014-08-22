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
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public class CombineSIMAMethod extends CombineBaseMethod implements CombineMethod {

	@Override
	protected List<IPeakSet<IPeak>> getMatches(
			List<IPeakSet<IPeak>> peaksets, CombineOptions options) {
		
		// convert List<IPeakSet<IPeak>> to List<AlignmentFile>
		AlignmentDataGenerator gen = new PeakMLDataGenerator(peaksets);
		AlignmentData data = gen.generate();		
		List<AlignmentFile> dataList = data.getAlignmentDataList();
		assert(dataList.size() == peaksets.size());		
		
		// pass this to the alignment method
		AlignmentMethodParam param = new AlignmentMethodParam();
		param.setMassTolerance(options.ppm);
		param.setRtTolerance(options.rtwindow);
		AlignmentMethod aligner = AlignmentMethodFactory.getAlignmentMethod(
				AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA, 
				param, data, null);
		AlignmentList result = aligner.align();
		List<AlignmentRow> resultRows = result.getRows();
		System.out.println("Total " + result.getRowsCount() + " rows aligned");
		
		// map the result back to List<IPeakSet<IPeak>>
		List<IPeakSet<IPeak>> matches = mapAlignmentResult(peaksets, dataList,
				resultRows);		

		return matches;

	}
	
}
