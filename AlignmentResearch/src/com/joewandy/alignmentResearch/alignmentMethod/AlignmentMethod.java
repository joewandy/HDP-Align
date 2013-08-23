package com.joewandy.alignmentResearch.alignmentMethod;

import java.io.PrintStream;
import java.util.List;

import net.sf.mzmine.taskcontrol.TaskStatus;

import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public interface AlignmentMethod {
	
	public List<AlignmentRow> align();

	public void addFilter(AlignmentResultFilter sizeFilter);

	public List<AlignmentRow> getAlignmentResult();
	
	public void writeAlignmentResult(PrintStream alignmentOutput);
		
}
