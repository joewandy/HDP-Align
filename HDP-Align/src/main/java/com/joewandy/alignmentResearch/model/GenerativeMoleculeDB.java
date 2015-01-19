package com.joewandy.alignmentResearch.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mzmatch.ipeak.util.GeneralMassSpectrum;

import org.apache.commons.math3.distribution.LogNormalDistribution;

import peakml.chemistry.MolecularFormula;
import peakml.chemistry.Molecule;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.GenerativeModelParameter;
import com.joewandy.alignmentResearch.rtPrediction.RTPredictor;

public class GenerativeMoleculeDB {

	private GenerativeModelParameter params;
	private String[] adducts;
	private RTPredictor retentionTimePredictor;
	Map<Integer, List<GenerativeMolecule>> molDB;
	
	public GenerativeMoleculeDB(String adductsStr, GenerativeModelParameter params) {
		this.adducts = adductsStr.split(",");
		this.params = params;
		this.retentionTimePredictor = new RTPredictor(params);
		this.molDB = new HashMap<Integer, List<GenerativeMolecule>>();
	}
	
	public void insert(int sourceFile, Molecule molecule) {
		
		final MolecularFormula formula = molecule.getFormula();
		final GeneralMassSpectrum ms = GeneralMassSpectrum.getGeneralMassSpectrum(formula, adducts,
			params.getMinDistributionValue(), params.getMaxValues());
		
		// predict retention time here
		double predictedRT = retentionTimePredictor.predict(molecule);
		double intensity = this.computeIntensity(molecule);
		GenerativeMolecule molInfo = new GenerativeMolecule(molecule, adducts, ms, predictedRT, intensity, sourceFile);

		if (molDB.containsKey(sourceFile)) {
			// just add
			molDB.get(sourceFile).add(molInfo);
		} else {
			// make new list
			List<GenerativeMolecule> molList = new ArrayList<GenerativeMolecule>();
			molList.add(molInfo);
			molDB.put(sourceFile, molList);			
		}
		
	}
		
	public List<GenerativeMolecule> getAllMoleculeInfo(int sourceFile) {
		return molDB.get(sourceFile);
	}

	public List<GenerativeMolecule> getAllMoleculeInfo() {
		List<GenerativeMolecule> all = new ArrayList<GenerativeMolecule>();
		for (Entry<Integer, List<GenerativeMolecule>> e : molDB.entrySet()) {
			all.addAll(e.getValue());
		}
		return all;
	}
	
	public void initializeTheoreticalPeaks() {
		Map<GenerativeMolecule, Integer> molID = new HashMap<GenerativeMolecule, Integer>();
		int theoPeakID = 0;
		for (Entry<Integer, List<GenerativeMolecule>> e : molDB.entrySet()) {
			for (GenerativeMolecule mol : e.getValue()) {
				if (molID.containsKey(mol)) {
					// reuse the last ID for this molecule
					int lastID = molID.get(mol);
					mol.generateTheoFeatures(lastID);
				} else {
					// create new ID for this molecule
					molID.put(mol, theoPeakID);
					List<Feature> theoFeatures = mol.generateTheoFeatures(theoPeakID);
					// advance the counter for the next molecule
					theoPeakID = theoPeakID + theoFeatures.size();								
				}
			}
		}		
	}
	
	public int countTheoreticalPeaks(List<GenerativeMolecule> molecules) {
		int total = 0;
		for (GenerativeMolecule mol : molecules) {
			total += mol.size();
		}
		return total;
	}
	
	public int size(int sourceFile) {
		List<GenerativeMolecule> mols = molDB.get(sourceFile);
		if (mols == null) {
			return 0;
		} else {
			return mols.size();
		}
	}
	
	private double computeIntensity(Molecule mol) {
		LogNormalDistribution dist = new LogNormalDistribution(params.getB(), params.getC());
		double sample = dist.sample();
		return sample;
	}
		
}
