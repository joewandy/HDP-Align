package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

public class GenerativeModelParameter {

	public enum ExperimentType {
		TECHNICAL, BIOLOGICAL
	}
	
	// generate biological or technical replicates ??	
	private ExperimentType expType = ExperimentType.TECHNICAL;	
	private int S = 2;					// how many replicates to produce
	
	private double probP = 0.8;			// probaiblity of each individiual observed peak
	private double threshold_q = 1000;	// threshold for filtering low intensity peaks
	private double [] g = {1, 1, 1};	// warping scaling coeff in each replicate
	private double[] h = {0, 0, 0};		// warping translation coeff in each replicate

	private double a = -1;				// proportion of the metabolites that are not identical across runs
	private double alpha_a = 4;			// Beta distribution parameter to draw a from
	private double alpha_b = 2;			// Beta distribution parameter to draw b from
	
	private double b = 11;				// mean of metabolite's concentration (log-Normal)
	private double c = 1;				// standard deviation of metabolite concentration (log-Normal)
	private double alpha = 10;			// DP concentration parameter for clustering
	private double d = 1000; 			// mean of predicted retention time of metabolite (Normal)
	private double e = 250;				// standard deviation of predicted retention time of metabolite (Normal)
	private double sigma_c = 2;  	  	// standard deviation of cluster's RT (Normal)
	private double sigma_q = 1000;		// standard deviation of observed peak's intensity (Normal)
	private double sigma_t = 1;			// standard deviation of observed peak's RT (Normal)
	private double sigma_m = 0.015;		// standard deviation of observed peak's mass (Normal)

	// The minimum probability mass that a mass needs to be kept in the distribution of the spectrum
	private double minDistributionValue = 10e-6;
		
	// The maximum number of entries in a compound's spectrum
	private int maxValues = 10;

	// Option to specify which adducts to search for."
	private String adducts = "M+2H,M+H+NH4,M+ACN+2H,M+2ACN+2H,M+H,M+NH4,M+Na,M+CH3OH+H,M+ACN+H,M+ACN+Na,M+2ACN+H,2M+H,2M+Na,2M+ACN+H";
//	private String adducts = "M+2H,M+H+NH4,M+ACN+2H,M+H,M+Na";
//	private String adducts = "M+H,M+2H,M+Na";

	private String replacementMolsPath = "/home/joewandy/Dropbox/Project/mzMatch/scripts/standards/kegg.xml";

	/* LOTS OF GETTERS AND SETTERS **/
	
	public ExperimentType getExpType() {
		return expType;
	}

	public void setExpType(ExperimentType expType) {
		this.expType = expType;
	}

	public int getS() {
		return S;
	}

	public void setS(int s) {
		S = s;
	}

	public double getProbP() {
		return probP;
	}

	public void setProbP(double probP) {
		this.probP = probP;
	}

	public double getThreshold_q() {
		return threshold_q;
	}

	public void setThreshold_q(double threshold_q) {
		this.threshold_q = threshold_q;
	}

	public double[] getG() {
		return g;
	}

	public void setG(double[] g) {
		this.g = g;
	}

	public double[] getH() {
		return h;
	}

	public void setH(double[] h) {
		this.h = h;
	}

	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
	}

	public double getAlpha_a() {
		return alpha_a;
	}

	public void setAlpha_a(double alpha_a) {
		this.alpha_a = alpha_a;
	}

	public double getAlpha_b() {
		return alpha_b;
	}

	public void setAlpha_b(double alpha_b) {
		this.alpha_b = alpha_b;
	}

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}

	public double getC() {
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public double getD() {
		return d;
	}

	public void setD(double d) {
		this.d = d;
	}

	public double getE() {
		return e;
	}

	public void setE(double e) {
		this.e = e;
	}

	public double getSigma_c() {
		return sigma_c;
	}

	public void setSigma_c(double sigma_c) {
		this.sigma_c = sigma_c;
	}

	public double getSigma_q() {
		return sigma_q;
	}

	public void setSigma_q(double sigma_q) {
		this.sigma_q = sigma_q;
	}

	public double getSigma_t() {
		return sigma_t;
	}

	public void setSigma_t(double sigma_t) {
		this.sigma_t = sigma_t;
	}

	public double getSigma_m() {
		return sigma_m;
	}

	public void setSigma_m(double sigma_m) {
		this.sigma_m = sigma_m;
	}

	public double getMinDistributionValue() {
		return minDistributionValue;
	}

	public void setMinDistributionValue(double minDistributionValue) {
		this.minDistributionValue = minDistributionValue;
	}

	public int getMaxValues() {
		return maxValues;
	}

	public void setMaxValues(int maxValues) {
		this.maxValues = maxValues;
	}

	public String getAdducts() {
		return adducts;
	}

	public void setAdducts(String adducts) {
		this.adducts = adducts;
	}

	public String getReplacementMolsPath() {
		return replacementMolsPath;
	}

	public void setReplacementMolsPath(String replacementMolsPath) {
		this.replacementMolsPath = replacementMolsPath;
	}
		
}
