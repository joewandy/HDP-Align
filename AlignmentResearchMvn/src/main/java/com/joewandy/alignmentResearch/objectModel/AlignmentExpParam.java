package com.joewandy.alignmentResearch.objectModel;

public class AlignmentExpParam {

	private String label;
	
	private double ppm;
	private double rtWindow;
	
	private boolean randomise;
	private int iteration;
	
	private boolean multigraph;
	private boolean visualise;
	
	private int dataFileCount;

	// use builder pattern later ... ?
	public AlignmentExpParam(String label, double ppm, double rtWindow, 
			boolean randomise, int iteration, boolean multigraph, boolean visualise,
			int dataFileCount) {
		super();
		this.label = label;
		this.ppm = ppm;
		this.rtWindow = rtWindow;
		this.randomise = randomise;
		this.iteration = iteration;
		this.multigraph = multigraph;
		this.visualise = visualise;
		this.dataFileCount = dataFileCount;
	}

	// use builder pattern later ... ?
	public AlignmentExpParam(String label) {
		super();
		this.label = label;
	}
	
	public String getLabel() {
		return label;
	}

	public double getPpm() {
		return ppm;
	}

	public double getRtWindow() {
		return rtWindow;
	}
	
	public boolean isRandomise() {
		return randomise;
	}
	
	public boolean isMultigraph() {
		return multigraph;
	}

	public int getIteration() {
		return iteration;
	}

	public boolean isVisualise() {
		return visualise;
	}

	public int getDataFileCount() {
		return dataFileCount;
	}

	@Override
	public String toString() {
		return "AlignmentExpParam [label=" + label + ", ppm=" + ppm
				+ ", rtWindow=" + rtWindow + ", randomise=" + randomise
				+ ", iteration=" + iteration + ", multigraph=" + multigraph
				+ ", visualise=" + visualise + "]";
	}
	
}
