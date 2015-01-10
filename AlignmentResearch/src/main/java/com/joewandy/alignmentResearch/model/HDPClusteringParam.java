package com.joewandy.alignmentResearch.model;


public class HDPClusteringParam {

	private int nsamps;
	private int burnIn;
	
	private double mu_0;
	private double sigma_0_prec;
	private double psi_0;
	private double rho_0_prec;
	
	private double alpha_rt;
	private double alpha_mass;
	private double top_alpha;
	
	private double delta_prec;
	private double gamma_prec;
	private double rho_prec;
	
	private boolean speedUpHacks;
	private int speedUpNumSample;
	private int refFileIdx;
		
	public int getNsamps() {
		return nsamps;
	}

	public void setNsamps(int nsamps) {
		this.nsamps = nsamps;
	}

	public int getBurnIn() {
		return burnIn;
	}

	public void setBurnIn(int burnIn) {
		this.burnIn = burnIn;
	}

	public double getMu_0() {
		return mu_0;
	}

	public void setMu_0(double mu_0) {
		this.mu_0 = mu_0;
	}

	public double getSigma_0_prec() {
		return sigma_0_prec;
	}

	public void setSigma_0_prec(double sigma_0_prec) {
		this.sigma_0_prec = sigma_0_prec;
	}

	public double getPsi_0() {
		return psi_0;
	}

	public void setPsi_0(double psi_0) {
		this.psi_0 = psi_0;
	}

	public double getRho_0_prec() {
		return rho_0_prec;
	}

	public void setRho_0_prec(double rho_0_prec) {
		this.rho_0_prec = rho_0_prec;
	}

	public double getAlpha_rt() {
		return alpha_rt;
	}

	public void setAlpha_rt(double alpha_rt) {
		this.alpha_rt = alpha_rt;
	}

	public double getAlpha_mass() {
		return alpha_mass;
	}

	public void setAlpha_mass(double alpha_mass) {
		this.alpha_mass = alpha_mass;
	}

	public double getTop_alpha() {
		return top_alpha;
	}

	public void setTop_alpha(double top_alpha) {
		this.top_alpha = top_alpha;
	}

	public double getDelta_prec() {
		return delta_prec;
	}

	public void setDelta_prec(double delta_prec) {
		this.delta_prec = delta_prec;
	}

	public double getGamma_prec() {
		return gamma_prec;
	}

	public void setGamma_prec(double gamma_prec) {
		this.gamma_prec = gamma_prec;
	}

	public double getRho_prec() {
		return rho_prec;
	}

	public void setRho_prec(double rho_prec) {
		this.rho_prec = rho_prec;
	}

	public boolean isSpeedUpHacks() {
		return speedUpHacks;
	}

	public void setSpeedUpHacks(boolean speedUpHacks) {
		this.speedUpHacks = speedUpHacks;
	}

	public int getSpeedUpNumSample() {
		return speedUpNumSample;
	}

	public void setSpeedUpNumSample(int speedUpNumSample) {
		this.speedUpNumSample = speedUpNumSample;
	}

	public int getRefFileIdx() {
		return refFileIdx;
	}

	public void setRefFileIdx(int refFileIdx) {
		this.refFileIdx = refFileIdx;
	}
	
}
