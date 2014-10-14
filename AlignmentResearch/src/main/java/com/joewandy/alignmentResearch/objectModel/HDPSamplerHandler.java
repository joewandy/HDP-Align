package com.joewandy.alignmentResearch.objectModel;

import java.util.List;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

public class HDPSamplerHandler {

	private List<HDPMetabolite> hdpMetabolites;
	private Matrix resultMap;
	private int samplesTaken;
	private int totalPeaks;
	
	public HDPSamplerHandler(List<HDPMetabolite> hdpMetabolites, int totalPeaks) {
		this.hdpMetabolites = hdpMetabolites;
		this.totalPeaks = totalPeaks;
		this.resultMap = new FlexCompRowMatrix(totalPeaks, totalPeaks);
	}

	public void handleSample(int s, int peaksProcessed, double timeTaken, HDPClusteringParam hdpParam) {

		int I = hdpMetabolites.size();
		
		boolean printMsg = true;	
		if ((s+1) > hdpParam.getBurnIn()) {
			if (printMsg) {
				System.out.print(String.format("Sample S#%05d ", (s+1)));					
			}
		} else {
			if (printMsg) {
				System.out.print(String.format("Sample B#%05d ", (s+1)));					
			}
		}
		
		StringBuilder sb = new StringBuilder();
		if ((s+1) > hdpParam.getBurnIn()) {
			// store the actual samples
			sb.append(String.format("(%5.2fs) peaks=%d/%d I=%d ", timeTaken, peaksProcessed, totalPeaks, I));
			updateResultMap();
			samplesTaken++;
		} else {
			// discard the burn-in samples
			sb.append(String.format("(%5.2fs) peaks=%d/%d I=%d ", timeTaken, peaksProcessed, totalPeaks, I));			
		}
		
		sb.append("all_A = [");
		for (int i = 0; i < I; i++) {
			HDPMetabolite met = hdpMetabolites.get(i);
			int A = met.getA();
			String formatted = String.format(" %3d", A);
			sb.append(formatted);
		}
		sb.append(" ]");

		if (printMsg) {
			System.out.println(sb.toString());				
		}
		
	}
	
	public Matrix getResultMap() {
		return resultMap;
	}

	public int getSamplesTaken() {
		return samplesTaken;
	}

	private void updateResultMap() {
		
		// for all metabolite
		for (int i = 0; i < hdpMetabolites.size(); i++) {

			HDPMetabolite met = hdpMetabolites.get(i);

			// for all mass clusters
			for (int a = 0; a < met.getA(); a++) {
				List <Feature> peaksInside = met.getPeaksInMassCluster(a);
				for (Feature f1 : peaksInside) {
					for (Feature f2 : peaksInside) {
						int m = f1.getSequenceID();
						int n = f2.getSequenceID();
						double currentValue = resultMap.get(m, n);
						double newValue = currentValue+1;
						resultMap.set(m, n, newValue);
					}
				}
			}
			
		}
		
	}
	
}
