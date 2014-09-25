package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

public class GenerativeModelParameter {

	public enum ExperimentType {
		TECHNICAL, BIOLOGICAL
	}
	
	// generate biological or technical replicates ??	
	private ExperimentType expType = ExperimentType.TECHNICAL;	
	private int S = 20;				// how many replicates to produce
	
	private double threshold_q = 0000;	// threshold for filtering low intensity peaks
	private double [] g = {1, 1, 1};	// warping scaling coeff in each replicate
	private double[] h = {0, 0, 0};		// warping translation coeff in each replicate

	private double[] as = {0.3, 0.5};			// proportion of the metabolites that are not identical across runs
	private double a = 0.3;
	
	private double b = 12;				// mean of metabolite's concentration (log-Normal dist)
	private double c = 1;				// standard deviation of metabolite concentration (log-Normal dist)
	private double alpha = 10;			// DP concentration parameter for local RT clusters
	private double d = 1000; 			// mean of predicted retention time of metabolite (Normal dist)
	private double e = 250;				// standard deviation of predicted retention time of metabolite (Normal dist)
	private double sigma_c = 20;  	  	// standard deviation of cluster's RT (Normal dist)
	private double sigma_t = 2;			// standard deviation of observed peak's RT (Normal dist)
	private double sigma_q = 0.1;		// standard deviation of observed peak's log intensity (Normal dist)
	private double sigma_m = getMassStdev(2);	// standard deviation of observed peak's log mass in ppm (Normal dist)

	// The minimum probability mass that a mass needs to be kept in the distribution of the spectrum
	private double minDistributionValue = 10e-6;
		
	// The maximum number of entries in a compound's spectrum
	private int maxValues = 10;

	// Option to specify which adducts to search for."
	//	private String adducts = "M+2H,M+H+NH4,M+ACN+2H,M+2ACN+2H,M+H,M+NH4,M+Na,M+CH3OH+H,M+ACN+H,M+ACN+Na,M+2ACN+H,2M+H,2M+Na,2M+ACN+H";
	private String adducts = "M+2H,M+H+NH4,M+ACN+2H,M+2ACN+2H,M+H,M+NH4,M+Na,M+CH3OH+H,M+ACN+H,M+ACN+Na,M+2ACN+H,2M+H,2M+Na,2M+ACN+H";
	//	private String adducts = "M+H,M+2H,M+Na";

	private String replacementMolsPath = "/home/joewandy/Dropbox/Project/mzMatch/scripts/standards/kegg.xml";

	private double getMassStdev(double massTol) {
		double logOnePpm = Math.log(1000001) - Math.log(1000000);
		double logDiff = logOnePpm * massTol; 
		double stdev = logDiff/2; // assume 2 stdev = logDiff
		return stdev;
	}
	
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

	public double[] getAs() {
		return as;
	}
	
	public double getAs(int i) {
		return as[i];			
	}	
	
	public void setAs(double[] as) {
		this.as = as;
	}
	
	public void setA(double a) {
		this.a = a;
	}

	public double getA() {
		return this.a;
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
