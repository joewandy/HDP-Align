package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import peakml.chemistry.Molecule;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.HDPAnnotation;
import com.joewandy.alignmentResearch.model.HDPAnnotationItem;
import com.joewandy.alignmentResearch.model.HDPClusteringParam;
import com.joewandy.alignmentResearch.model.HDPMassCluster;
import com.joewandy.alignmentResearch.model.HDPMetabolite;
import com.joewandy.alignmentResearch.model.HDPPrecursorMass;
import com.joewandy.alignmentResearch.precursorPrediction.AdductTransformComputer;
import com.joewandy.mzmatch.query.CompoundQuery;

public class HDPSampleHandler {
	
	private List<HDPMetabolite> hdpMetabolites;
	private int totalPeaks;
	private HDPAllSamples samplingResults;
		
	public HDPSampleHandler(int totalPeaks, List<HDPMetabolite> hdpMetabolites) {
	
		this.hdpMetabolites = hdpMetabolites;
		this.samplingResults = new HDPAllSamples();
		this.totalPeaks = totalPeaks;

	}
	
	public boolean initialiseResultsFromPath(String hdpClusteringResultsPath) {

		System.out.println("Loading previous clustering results from " + hdpClusteringResultsPath);
		File f = new File(hdpClusteringResultsPath);
		if(!f.exists()) {
			return false;
		}
		
		// use json
		boolean loadSuccess = false;
		ObjectMapper mapper = new ObjectMapper();
		try {
			samplingResults = mapper.readValue(new File(hdpClusteringResultsPath),
					HDPAllSamples.class);
			loadSuccess = true;
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// use java serialisation
//		boolean loadSuccess = false;
//		try (
//			InputStream file = new FileInputStream(hdpClusteringResultsPath);
//			InputStream buffer = new BufferedInputStream(file);
//			ObjectInput input = new ObjectInputStream(buffer);
//		) {
//			results = (HDPResults) input.readObject();
//			loadSuccess = true;
//		} catch (ClassNotFoundException ex) {
//			ex.printStackTrace();
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
		
	    return loadSuccess;

	}
	
	public void persistResultsToPath(String hdpClusteringResultsPath) {

		System.out.println("Saving previous clustering results to " + hdpClusteringResultsPath);
		
		// use json
		ObjectMapper mapper = new ObjectMapper();
		try {
			mapper.writeValue(new File(hdpClusteringResultsPath), samplingResults);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// use java serialisation
//		try (
//			OutputStream file = new FileOutputStream(hdpClusteringResultsPath);
//			OutputStream buffer = new BufferedOutputStream(file);
//			ObjectOutput output = new ObjectOutputStream(buffer);
//		) {
//			output.writeObject(results);
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
	
	}

	public void storeSample(int s, int peaksProcessed, double timeTaken, HDPClusteringParam hdpParam, boolean lastSample) {

		int I = hdpMetabolites.size();
		
		boolean printMsg = true;	
		if ((s+1) > hdpParam.getBurnIn()) {
			if (printMsg) {
				System.out.print(String.format("Sample S#%05d ", (s+1)));		
				samplingResults.store(hdpMetabolites);
			}
		} else {
			// ignore the burn-in samples
			if (printMsg) {
				System.out.print(String.format("Sample B#%05d ", (s+1)));					
			}
		}
		
		StringBuilder sb = new StringBuilder();
		if ((s+1) > hdpParam.getBurnIn()) {
			sb.append(String.format("(%5.2fs) peaks=%d/%d I=%d ", timeTaken, peaksProcessed, totalPeaks, I));
		} else {
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
	
	public HDPAllSamples getSamplingResults() {
		return samplingResults;
	}
			
}