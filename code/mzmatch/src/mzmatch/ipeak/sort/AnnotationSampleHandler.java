package mzmatch.ipeak.sort;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Joiner;

import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralMassSpectrum;
import mzmatch.ipeak.util.GeneralMassSpectrumDatabase;
import mzmatch.ipeak.util.GeneralMolecularFormula;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.chemistry.Polarity;

public abstract class AnnotationSampleHandler implements SampleHandler<MoleculeData, MoleculeClustering> {
	public static final String unfilteredAnnotation = "posteriorIdentification";
	public static final String filteredAnnotation = "filteredIdentification";
	
	int numSamples;
	
	public abstract void writeAnnotations();
	
	public static void doAnnotation(final int molecule, final int isotope, final GeneralMassSpectrumDatabase theoreticalSpectrums,
			final double value, final String[] adducts, final List<String> outputs ) {
		String annotation = null;
		if ( molecule == theoreticalSpectrums.size() ) {
			annotation = String.format("junk, %.5f", value);
		} else {
			final GeneralMassSpectrum gms = theoreticalSpectrums.get(molecule);
			final String name = theoreticalSpectrums.getName(molecule);
			final String realName = theoreticalSpectrums.getRealName(molecule);
			
			if ( isotope >= gms.size() ) {
				annotation = String.format("%s, %s, default, %.5f", name, realName, value);
				//annotation = String.format("%s, default, %.2f", nameOutput, (double) ns / numSamples);
			} else {
				final GeneralMolecularFormula f = gms.getIsotopicFormula(isotope);
				final String adduct = adducts[gms.getAdduct(isotope)];
				//annotation = String.format("%s, %s, %s, %.2f", nameOutput, adduct, f, (double) ns / numSamples);
				annotation = String.format("%s, %s, %s, %s, %.5f", name, realName, adduct, f, value);
			}
		}
		//System.out.println("BLAH: " + annotation);
		outputs.add(annotation);
	}
	
	public static class IdentifyingAnnotationSampleHandler extends AnnotationSampleHandler {
		final List<String> moleculeNames;
		final GeneralMassSpectrumDatabase theoreticalSpectrums;
		private final String[] adducts;
		private final IPeakSet<IPeak> peaks;
		
		private final Map<MoleculeIsotope,Integer>[] peakCounts;
		private final Map<MoleculeIsotope,Integer>[] properPeakCounts;
		private final MoleculeParameters parameters;
		
		@SuppressWarnings("unchecked")
		public IdentifyingAnnotationSampleHandler(final List<String> moleculeNames, final GeneralMassSpectrumDatabase theoreticalSpectrums,
				final Polarity[] polarities, final String[] adducts, final IPeakSet<IPeak> peaks, final MoleculeParameters parameters) {
			this.moleculeNames = moleculeNames;
			this.theoreticalSpectrums = theoreticalSpectrums;
			this.adducts = adducts;
			this.peaks = peaks;
			this.peakCounts = (Map<MoleculeIsotope,Integer>[])Array.newInstance(Map.class, polarities.length);
			this.properPeakCounts = (Map<MoleculeIsotope,Integer>[])Array.newInstance(Map.class, polarities.length);
			for ( int peak = 0; peak < peakCounts.length; ++peak ) {
				this.peakCounts[peak] = new HashMap<MoleculeIsotope,Integer>();
				this.properPeakCounts[peak] = new HashMap<MoleculeIsotope,Integer>();
			}
			this.parameters = parameters;
		}
		
		public void writeAnnotations() {
			for ( int peak = 0; peak < peakCounts.length; ++peak ) {
				writeAnnotations(peakCounts[peak], peak, unfilteredAnnotation);
				writeAnnotations(properPeakCounts[peak], peak, filteredAnnotation);
			}
		}
		
		public void writeAnnotations(final Map<MoleculeIsotope,Integer> counts, final int peak, final String annotationName) {
			//for ( int peak = 0; peak < peakCounts.length; ++peak ) {
			//	final Map<MoleculeIsotope,Integer> counts = peakCounts[peak];
				final List<Entry<MoleculeIsotope,Integer>> entryList = new ArrayList<Entry<MoleculeIsotope,Integer>>();
				for ( Entry<MoleculeIsotope,Integer> e : counts.entrySet() ) {
					entryList.add(e);
				}
				Collections.sort(entryList, new Comparator<Entry<MoleculeIsotope,Integer>>() {
					public int compare(Entry<MoleculeIsotope,Integer> e1, Entry<MoleculeIsotope,Integer> e2) {
						return e2.getValue() - e1.getValue();
					}
				});
				final int numAnnotations = Math.min(entryList.size(), 100);
				assert numAnnotations > 0;
				//System.err.println("Peak: " + peak);
				final List<String> outputs = new ArrayList<String>();
				int totalValue = 0;
				for ( int i = 0; i < numAnnotations; ++i) {
					final Entry<MoleculeIsotope,Integer> e = entryList.get(i);
					final MoleculeIsotope mi = e.getKey();
					final int ns = e.getValue();
					totalValue += ns;
					//System.err.println("peak: " + peak + " molecule: " + mi.molecule + " " + theoreticalSpectrums.getName(mi.molecule) + " " +
					//		peaks.get(peak).getAnnotation("id").getValue());
					doAnnotation(mi.molecule, mi.isotope, theoreticalSpectrums, (double) ns / numSamples, adducts, outputs);
					
					
					/*
					String annotation = null;
					if ( mi.molecule == theoreticalSpectrums.size() ) {
						annotation = String.format("junk\u001F%.2f", (double) ns / numSamples);
					} else {
						final GeneralMassSpectrum gms = theoreticalSpectrums.get(mi.molecule);
						final String name = theoreticalSpectrums.getName(mi.molecule);
						final String realName = theoreticalSpectrums.getRealName(mi.molecule);
						
						if ( mi.isotope >= gms.size() ) {
							annotation = String.format("%s\u001F%s\u001Fdefault\u001F%.2f", name, realName, (double) ns / numSamples);
							//annotation = String.format("%s, default, %.2f", nameOutput, (double) ns / numSamples);
						} else {
							final GeneralMolecularFormula f = gms.getIsotopicFormula(mi.isotope);
							final String adduct = adducts[gms.getAdduct(mi.isotope)];
							//annotation = String.format("%s, %s, %s, %.2f", nameOutput, adduct, f, (double) ns / numSamples);
							annotation = String.format("%s\u001F%s\u001F%s\u001F%s\u001F%.2f", name, realName, adduct, f, (double) ns / numSamples);
						}
					}
					outputs.add(annotation);
					*/
					//peaks.get(peak).addAnnotation("probabilityIdentification", annotation);
				}
				assert totalValue == numSamples : "totalValue: " + totalValue + " numSamples: " + numSamples;
				final String output = Joiner.on("; ").join(outputs);
				peaks.get(peak).addAnnotation(annotationName, output);
		//	} 
		}
		
		public void handleSample(final MoleculeClustering clustering) {
			//System.err.println(clustering);
			
			numSamples++;
			
			for ( int peak = 0; peak < clustering.numberOfPeaks(); ++peak ) {
				/*
				String peakId = this.peaks.get(peak).getAnnotation("id").getValue();
				
				if ( peakId.equals("6975") ) {
					final GeneralMassSpectrum gms = theoreticalSpectrums.get(clustering.getPeakMolecule(peak));
					System.err.println("peak: " + peak + " id: " +
							this.peaks.get(peak).getAnnotation("id").getValue() + " mass: " + peaks.get(peak).getMass() +
							" otherMass: " + Math.exp(clustering.data.masses[0][peak]) + " molecule: " +
							clustering.getPeakMolecule(peak) + " " + theoreticalSpectrums.getName(clustering.getPeakMolecule(peak)) +
							" position: " + clustering.getPosition(peak) + " formula: " + gms.getIsotopicFormula(clustering.getPosition(peak)));
				}
				*/

				final MoleculeIsotope mai = new MoleculeIsotope(clustering.getPeakMolecule(peak), clustering.getPosition(peak));
				if ( peakCounts[peak].containsKey(mai) ) {
					final int count = peakCounts[peak].get(mai);
					peakCounts[peak].put(mai, count + 1);
				} else {
					peakCounts[peak].put(mai, 1);
				}
				final MoleculeIsotope pmai = clustering.junk[peak] ?
						new MoleculeIsotope(theoreticalSpectrums.size(), clustering.getPosition(peak)) : mai;

				if ( properPeakCounts[peak].containsKey(pmai) ) {
					final int count = properPeakCounts[peak].get(pmai);
					properPeakCounts[peak].put(pmai, count + 1);
				} else {
					properPeakCounts[peak].put(pmai, 1);
				}
				
			}
		}
		
		public static class MoleculeIsotope {
			final int molecule;
			final int isotope;
			
			public MoleculeIsotope(final int molecule, final int isotope) {
				this.molecule = molecule;
				this.isotope = isotope;
			}
			
			@Override
			public boolean equals(Object other) {
				if ( this == other ) {
					return true;
				}
				if ( ! (other instanceof MoleculeIsotope) ) {
					return false;
				}
				final MoleculeIsotope mai = (MoleculeIsotope)other;
				return this.molecule == mai.molecule && this.isotope == mai.isotope;
			}
			
			@Override
			public int hashCode() {
				int result = 17;
				result = 31 * result + molecule;
				result = 31 * result + isotope;
				return result;
			}
		}
	}
	
	public static class ClusteringAnnotationSampleHandler extends AnnotationSampleHandler {
		private final int[][] clusterCounts;
		private final IPeakSet<IPeak> peaks;
		
		public ClusteringAnnotationSampleHandler(final IPeakSet<IPeak> peaks) {
			this.clusterCounts = new int[peaks.size()][peaks.size()];
			this.peaks = peaks;
		}
		
		public void writeAnnotations() {
			for ( int peak = 0; peak < peaks.size(); ++peak ) {
				final List<String> outputs = new ArrayList<String>();
				final int[] counts = this.clusterCounts[peak];
				for ( int otherPeak = 0; otherPeak < peaks.size(); ++otherPeak ) {
					final int count = counts[otherPeak];
					if ( count > 0 ) {
						final double prob = (double) count / numSamples;
						if ( prob > 0.01 ) {
							final String peakId = peaks.get(otherPeak).getAnnotation("id").getValue();
							final String annotation = String.format("%s,%.2f", peakId, prob);
							outputs.add(annotation);
						}
					}
				}
				final String output = Joiner.on(";").join(outputs);
				peaks.get(peak).addAnnotation("probabilityRelationship", output);
			}
		}
		
		public void handleSample(final MoleculeClustering clustering) {
			//System.err.println(clustering);
			
			numSamples++;
			
			for ( int peak = 0; peak < clustering.numberOfPeaks(); ++peak ) {
				final int cluster = clustering.getCluster(peak);
				final List<Integer> clusterPeaks = clustering.getClusterPeaks(cluster);
				for ( int otherPeak : clusterPeaks ) {
					if ( otherPeak == peak ) {
						continue;
					}
					clusterCounts[peak][otherPeak]++;
				}
			}
		}
	}
	

}