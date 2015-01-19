package mzmatch.ipeak.sort;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mzmatch.ipeak.util.Common;

import peakml.chemistry.MolecularFormula;

public class MoleculeClusterer extends FormulaClusterer {
	private final RetentionTimeClusteringScorer<MoleculeClustering> rtScorer;
	private final double retentionTimePredPrecision;
	private final Map<Integer,List<Integer>> network;
	private final double delta = 1.0;
	
	public MoleculeClusterer(final MoleculeData data, final MoleculeParameters parameters, final Random random,
			final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final CorrelationMeasure measure, final String[] adducts, final PeakPosteriorScorer<MoleculeClustering> scorer,
			MassIntensityClusteringScorer miScorer, RetentionTimeClusteringScorer<MoleculeClustering> rtScorer,
			final boolean verbose) {
		super(data, parameters, random, inScorer, outScorer, measure, adducts, scorer, miScorer, verbose);
		this.rtScorer = rtScorer;
		this.retentionTimePredPrecision = 1 / (parameters.retentionTimePredictSD * parameters.retentionTimePredictSD);
		if ( parameters.options.connectivityNetwork != null ) {
			System.err.println("Loading connectivity network at " + parameters.options.connectivityNetwork);
			network = parseConnectivityNetwork(parameters.options.connectivityNetwork);
		} else {
			network = null;
		}
	}
	
	@Override
	public double singleSample(final MoleculeClustering currentClustering) {
		super.singleSample(currentClustering);
		
		if ( parameters.options.sampleClusterMoleculeAssignments ) {
			this.sampleClusterMoleculeAssignement(currentClustering);
		} else if ( parameters.options.useRetentionTimePrediction ) {
			this.doMoleculeStep(currentClustering);
		}
		//doMoleculeStep(currentClustering);
		// TODO Need to sample new cluster assignment and peak positions.
		//System.err.println(currentClustering);
		//currentClustering.doClusterPositionStep();
		currentClustering.postProcess();
		//assert false;
		return scorer.calculateLikelihood(currentClustering);
	}
	
	public void sampleClusterMoleculeAssignement(final MoleculeClustering currentClustering) {
		final int K = currentClustering.numberOfClusters();
		//System.err.println("Sampling CM!");
		final double[] clusterMeans = rtScorer.getClusterMeans(currentClustering);
		
		for ( int k = 0; k < K; ++k ) {
			final double[] likes = calculateMassLikelihoods(currentClustering, k);
			
			final Sample<Integer> sample = miScorer.sampleDistribution(likes);
			final int proposalMolecule = sample.v1;
			final int currentMolecule = currentClustering.getMolecule(k);
			if ( proposalMolecule == currentMolecule ) {
				continue;
			}
			//System.err.println(String.format("prop:%s curr:%s clus:%s", proposalMolecule, currentMolecule, k));
			//final double proposalLikelihood = sample.v2;
			//final int[] proposalPositions = new int[clusterPeaks.size()];
			final List<Integer> proposalPositions = new ArrayList<Integer>();
			double proposalPositionLikelihoodSum = 0.0;
			double currentPositionLikelihoodSum = 0.0;
			//double proposalPosteriorSum = 0.0;
			final List<Integer> clusterPeaks = new ArrayList<Integer>(currentClustering.getClusterPeaks(k));
			for ( int i = 0; i < clusterPeaks.size(); ++i ) {
				final int peak = clusterPeaks.get(i);
				final Sample<Integer> posSample = miScorer.samplePosition(peak, proposalMolecule);
				proposalPositions.add(posSample.v1);
				//proposalPositions[i] = posSample.v1;
				proposalPositionLikelihoodSum += posSample.v2;
				//System.err.println(String.format("Props: %s,%s,%s,%s", posSample.v1, miScorer.massCache.get(peak, proposalMolecule, posSample.v1), posSample.v2, peak));
				
				int currentPeakPosition = currentClustering.getPosition(peak);
				
				currentPositionLikelihoodSum += miScorer.calculateMassLikelihood(currentMolecule, peak, currentPeakPosition);
				//System.err.println(String.format("Props2: %s,%s,%s", currentPeakPosition, miScorer.massCache.get(peak, currentMolecule, currentPeakPosition), miScorer.calculateMassLikelihood(currentMolecule, peak, currentPeakPosition)));
						//miScorer.massCache.get(peak, currentMolecule, currentPeakPosition);
			}
			
			//System.err.println(String.format("Stuff: %s,%s,%s,%s", likes[proposalMolecule], proposalPositionLikelihoodSum,
			//			likes[currentMolecule], currentPositionLikelihoodSum));
			
			//System.exit(1);
			
			//final double totalProposalSum = likes[proposalMolecule] + proposalPositionLikelihoodSum;
			//final double totalCurrentSum = likes[currentMolecule] + currentPositionLikelihoodSum;
			final double totalProposalSum = proposalPositionLikelihoodSum;
			final double totalCurrentSum = currentPositionLikelihoodSum;
			final double[] sampleDist = new double[] { totalCurrentSum, totalProposalSum };
			final Sample<Integer> secondSample = miScorer.sampleDistribution(sampleDist);
			
			if ( secondSample.v1 == 0 ) {
				continue;
			}

			//System.err.println("Proposal: " + proposalMolecule);
			double proposalPosteriorSum = calculateLogPosterior(proposalMolecule, clusterPeaks, proposalPositions,
					clusterMeans, k, currentClustering);
			//System.err.println("Current: " + currentMolecule);
			double currentPosteriorSum = calculateLogPosterior(currentMolecule, clusterPeaks,
					currentClustering.getClusterPositions(k), clusterMeans, k, currentClustering);
			
			double currentNetworkPrior = 0.0;
			double proposalNetworkPrior = 0.0;
			if ( network != null ) {
				final int currentBeta = calculateBeta(currentMolecule, k, currentClustering);
				final int proposalBeta = calculateBeta(proposalMolecule, k, currentClustering);
				
				currentNetworkPrior = Math.log(currentBeta + delta) - Math.log(2 * delta + currentBeta + proposalBeta);
				proposalNetworkPrior = Math.log(proposalBeta + delta) - Math.log(2 * delta + currentBeta + proposalBeta);
			}
			currentPosteriorSum += currentNetworkPrior;
			proposalPosteriorSum += proposalNetworkPrior;
			/*
			double proposalPosteriorSum = 0.0;
			double currentPosteriorSum = 0.0;
			final List<Integer> currentPositions = new ArrayList<Integer>(currentClustering.getClusterPositions(k));
			final List<Integer> proposalPositionsCopy = new ArrayList<Integer>(proposalPositions);
			for ( int i = 0; i < clusterPeaks.size(); ++i ) {
				final int peak = clusterPeaks.get(i);
				final int proposedPosition = proposalPositionsCopy.get(i);
				final int currentPosition = currentPositions.get(i);
				
				for ( int rep = 0; rep < data.numReplicates; ++rep ) {
					proposalPosteriorSum += calculateLogPosterior(proposalMolecule, rep, peak, proposedPosition, clusterPeaks,
							proposalPositionsCopy);
					currentPosteriorSum += calculateLogPosterior(currentMolecule, rep, peak, currentPosition, clusterPeaks,
							currentPositions);
				}
				clusterPeaks.remove(i);
				proposalPositionsCopy.remove(i);
				currentPositions.remove(i);
			}
			*/
			if ( doMove(totalProposalSum, totalCurrentSum, proposalPosteriorSum, currentPosteriorSum ) ) {
			//		System.err.format("%s from %s to %s\n", k, currentMolecule, proposalMolecule);
				currentClustering.setMolecule(k, proposalMolecule, currentClustering.getClusterPeaks(k), proposalPositions);
			}
			//System.exit(1);
		}
		//System.exit(1);
	}
	
	private int calculateBeta(final int m, final int k, final MoleculeClustering currentClustering) {
		final List<Integer> connections = network.get(m);
		int beta = 0;
		for ( int c : connections ) {
			final List<Integer> assignedClusters = currentClustering.getClustersFromMolecule(c);
			if ( assignedClusters.size() > 1 || ( assignedClusters.size() == 1 && ! assignedClusters.contains(k)) ) {
				beta++;
			}
		}
		return beta;
	}
	
	private double calculateLogPosterior(final int molecule, final List<Integer> peaks, final List<Integer> positions,
			final double[] clusterMeans, final int k, final MoleculeClustering currentClustering) {
		final List<Integer> peaksCopy = new ArrayList<Integer>(peaks);
		final List<Integer> positionsCopy = new ArrayList<Integer>(positions);
		final int numberPeaks = peaks.size();
		
		//System.err.println("Starting");
		
		double posteriorSum = 0.0;
		for ( int i = numberPeaks - 1; i >= 0; i-- ) {
		//for ( int i = 0; i < numberPeaks; ++i ) {
			final int peak = peaksCopy.get(i);
			final int position = positionsCopy.get(i);
			for ( int rep = 0; rep < data.numReplicates; ++rep ) {
				posteriorSum += calculateLogPosterior(molecule, rep, peak, position, peaksCopy,
						positionsCopy);
			//	System.err.println("Posterior sum: " + posteriorSum + " peak: " + peak + " position: " + position);
			}
			peaksCopy.remove(i);
			positionsCopy.remove(i);
		}
		if ( parameters.options.useRetentionTimePrediction ) {
			posteriorSum += retentionTimeLikelihood(molecule, clusterMeans, k);
		}
		return posteriorSum;
	}
	
	private double calculateLogPosterior(final int molecule, final int rep, final int peak, final int position,
			final List<Integer> peaks, final List<Integer> positions) {
		//final double cSize = peaks.size() - 1 == 0 ? parameters.alpha : peaks.size() - 1;

		//System.err.println(String.format("Posterior: %s,%s", miScorer.calculateIntensityLikelihood(molecule, rep, peak, position, peaks, positions),
		//		miScorer.calculateMassLikelihood(molecule, rep, peak, position)));
		
		
		final double retval =  miScorer.calculateIntensityLikelihood(molecule, rep, peak, position, peaks, positions) +
				miScorer.calculateMassLikelihood(molecule, rep, peak, position);
		//System.err.println("Retval	: " + retval);
		return retval;
	}
	
	private double[] calculateMassLikelihoods(final MoleculeClustering currentClustering, final int k) {
		final List<Integer> clusterPeaks = currentClustering.getClusterPeaks(k);
		final double[] likes = new double[data.numMolecules];
		for ( int m = 0; m < data.numMolecules; ++m ) {
			double llSum = 0.0;
			for ( int peak : clusterPeaks ) {
				final double ll = miScorer.flatml.get(peak, m);
				//System.err.println(String.format("%s,%s,%s", m, peak, ll));
				llSum += ll;
			}
			likes[m] = llSum;
		}
		return likes;
	}
	
	private boolean doMove(final double proposalFunction, final double currentFunction, final double proposalPosterior,
			final double currentPosterior) {
		final double value = currentFunction - proposalFunction + proposalPosterior - currentPosterior;
		//System.err.println(String.format("%s,%s,%s,%s\n", currentFunction, proposalFunction, proposalPosterior, currentPosterior));
		final double acceptence = Math.exp(Math.min(0, value));
		
		final double test = random.nextDouble();
		if ( acceptence >= test) {
			// Accept
		//	System.err.println("Accepted with probability: " + acceptence);
			return true;
		}
		// Reject
		//System.err.println("Rejected with probability: " + acceptence);
		return false;
	}
	
	public double retentionTimeLikelihood(final int molecule, final double[] clusterMeans, final int k) {
		final double moleculeRetentionTime = data.theoreticalSpectrums.getRetentionTime(molecule);
		return Common.normalDensity(clusterMeans[k], moleculeRetentionTime, this.retentionTimePredPrecision, true);
	}
	
	private Map<Integer,List<Integer>> parseConnectivityNetwork(final String networkFile) {
		try {
			final Map<Integer,List<Integer>> retval = new HashMap<Integer,List<Integer>>();
			BufferedReader br = new BufferedReader(new FileReader(networkFile));
			String line;
			while ((line = br.readLine()) != null) {
			   if ( line.startsWith("#") ) {
				   continue;
			   }
			   final String[] sline = line.split(" ");
			   final String node = sline[0];
			   final int m = data.theoreticalSpectrums.getIndex(node);
			   final List<Integer> connections = new ArrayList<Integer>();
			   for ( int i = 1; i < sline.length; ++i ) {
				   final int mi = data.theoreticalSpectrums.getIndex(sline[i]);
				   connections.add(mi);
			   }
			   retval.put(m, connections);
			}
			br.close();
			return retval;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public void doMoleculeStep(final MoleculeClustering currentClustering) {
		//final double[] kappa = rtScorer.getClusterPrecisions(currentClustering);
		final double[][] mu = new double[data.numReplicates][];
		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			mu[rep] = rtScorer.getClusterMeans(currentClustering, rep);
		}
		
		for ( int k = 0; k < currentClustering.numberOfClusters(); ++k ) {
			final int molecule = currentClustering.getMolecule(k);
			final MolecularFormula formula = data.theoreticalSpectrums.getFormula(molecule);
			final List<Integer> sameFormulas = new ArrayList<Integer>();
			final List<Double> retentionTime = new ArrayList<Double>();
			for ( int j = 0; j < data.theoreticalSpectrums.size(); ++j ) {
				final MolecularFormula otherFormula = data.theoreticalSpectrums.getFormula(j);
				if ( otherFormula.equals(formula) ) {
					sameFormulas.add(j);
					retentionTime.add(data.theoreticalSpectrums.getRetentionTime(j));
				}
			}
			final double[] dist = new double[sameFormulas.size()];
			for ( int j = 0; j < sameFormulas.size(); ++j ) {
				final double rt = retentionTime.get(j);
				
				for ( int rep = 0; rep < data.numReplicates; ++rep ) {
					 dist[j] += Common.normalDensity(mu[rep][k], rt, this.retentionTimePredPrecision, true);
				}
			}
			//System.err.println(Arrays.toString(dist));
			final Sample<Integer> sample = miScorer.sampleDistribution(dist);
			final int newMolecule = sameFormulas.get(sample.v1);
			//System.err.println("Old molecule: " + molecule + " new molecule: " + newMolecule);
			final List<Integer> peaks = currentClustering.getClusterPeaks(k);
			final List<Integer> positions = currentClustering.getClusterPositions(k);
			currentClustering.setMolecule(k, newMolecule, peaks, positions);
		}
	}
}
