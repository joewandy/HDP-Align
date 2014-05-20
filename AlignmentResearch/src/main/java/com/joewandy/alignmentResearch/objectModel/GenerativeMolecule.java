package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mzmatch.ipeak.util.GeneralMassSpectrum;
import mzmatch.ipeak.util.GeneralMolecularFormula;
import peakml.chemistry.Molecule;

public class GenerativeMolecule {

	private String id;
	private Molecule molecule;
	private String[] adducts;
	private GeneralMassSpectrum ms;
	private double predictedRT;
	private double intensity;
	private List<Feature> theoFeatures;
	private int sourceFile;

	public GenerativeMolecule(Molecule molecule, String[] adducts,
			GeneralMassSpectrum ms, double predictedRT, double intensity, int sourceFile) {
		super();
		this.id = molecule.getDatabaseID();
		this.molecule = molecule;
		this.adducts = adducts;
		this.ms = ms;
		this.predictedRT = predictedRT;
		this.intensity = intensity;
		this.theoFeatures = new ArrayList<Feature>();
		this.sourceFile = sourceFile;
	}
	
	public List<Feature> generateTheoFeatures(int idStart) {
		
		// generate all its theoretical features
		List<Feature> molTheoFeatures = new ArrayList<Feature>();						
		for (int j = 0; j < size(); j++) {
								
			// get the theoretical mass and intensity
			double theoreticalMass = getMass(j);
			double intensityRatio = getDistribution(j);
			double theoreticalIntensity = getIntensity() * intensityRatio;
			
			Feature theoFeature = new Feature(idStart);
			theoFeature.setMass(theoreticalMass);
			theoFeature.setIntensity(theoreticalIntensity);
			theoFeature.setTheoPeakID(idStart);
			idStart++;
			molTheoFeatures.add(theoFeature);
			
		}
		
		this.theoFeatures = molTheoFeatures;
		assert(!molTheoFeatures.isEmpty());
		return molTheoFeatures;
		
	}

	public String getName() {
		return molecule.getName();
	}
	
	public String getFormula() {
		return molecule.getFormula().toString();
	}
	
	public String getPlainFormula() {
		return molecule.getPlainFormula();
	}
	
	public List<Double> getDistribution() {
		return ms.getDistribution();
	}
	
	public List<Double> getMasses() {
		return ms.getMasses();
	}
	
	public List<Integer> getAdducts() {
		return ms.getAdducts();
	}

	public double getDistribution(int index) {
		return ms.getDistribution(index);
	}
	
	public double getMass(int index) {
		return ms.getMass(index);
	}
	
	public int getAdduct(int index) {
		return ms.getAdduct(index);
	}
	
	public List<String> getAdductNames() {
		final List<String> msAdducts = new ArrayList<String>();
		for ( int a : ms.getAdducts() ) {
			msAdducts.add(adducts[a]);
		}
		return msAdducts;
	}
	
	public int size() {
		return ms.size();
	}
	
	public List<GeneralMolecularFormula> getGeneralMolecularFormulas() {
		return ms.getGeneralMolecularFormulas();
	}

	public double getPredictedRT() {
		return predictedRT;
	}

	public double getIntensity() {
		return intensity;
	}

	public List<Feature> getTheoFeatures() {
		return theoFeatures;
	}

	public int getSourceFile() {
		return sourceFile;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GenerativeMolecule other = (GenerativeMolecule) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GenerativeMolecule [id=" + id + "]";
	}
	
}
