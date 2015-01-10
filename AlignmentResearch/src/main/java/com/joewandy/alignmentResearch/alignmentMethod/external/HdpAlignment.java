package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.FeatureMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPAlignmentResults;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPMassRTClustering;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPAllSamples;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPPrinter;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HdpProbabilityMatching;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.HDPAnnotation;
import com.joewandy.alignmentResearch.model.HDPAnnotationItem;
import com.joewandy.alignmentResearch.model.HDPClustering;

/**
 * An alignment method using Hierarchical Dirichlet Process mixture model
 * @author joewandy
 *
 */
public class HdpAlignment extends BaseAlignment implements AlignmentMethod {

	protected List<AlignmentFile> dataList;			// The input files to be aligned
	private List<Feature> allFeatures;				// All the features across all files
	private AlignmentMethodParam param;				// Alignment parameters	

	private HDPAlignmentResults results;
	private HDPPrinter printer;
	
	/**
	 * Constructs an instance of HDPAlignment object
	 * @param dataList The input files
	 * @param param Alignment parameters
	 */
	public HdpAlignment(List<AlignmentFile> dataList,
			AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		this.allFeatures = new ArrayList<Feature>();
		for (AlignmentFile file : dataList) {
			this.allFeatures.addAll(file.getFeatures());
		}
		this.param = param;
		
		this.results = new HDPAlignmentResults();
		String databaseFile = param.getGroundTruthDatabase();
		Map<String, String> compoundGroundTruthDatabase = loadGroundTruthDB(databaseFile);
		this.printer = new HDPPrinter(allFeatures, compoundGroundTruthDatabase);

	}

	@Override
	public AlignmentList matchFeatures() {
		
		AlignmentList masterList = new AlignmentList("");
		int samplesTaken = 0;
		
		// use the java HDP Mass-RT clustering			
		if (MultiAlignConstants.SCORING_METHOD_HDP_MASS_RT_JAVA
				.equals(this.param.getScoringMethod())) {

			// run the HDP RT+mass clustering
			HDPClustering clustering = new HDPMassRTClustering(dataList,
					param);
			clustering.runClustering();

			// process the matching results
			samplesTaken = clustering.getSamplesTaken();
			results = clustering.getAlignmentResults();
			
			printer.printHeader();
						
			// print annotation results
			printer.printAnnotations(clustering);
			System.out.println();

			// print last samples
			printer.printLastSample(clustering);
			System.out.println();

		} else if (MultiAlignConstants.SCORING_METHOD_HDP_RT_JAVA
				.equals(this.param.getScoringMethod())) {

			// use the java HDP RT clustering

		}
		
		// construct the actual feature matching here 
		FeatureMatching matcher = new HdpProbabilityMatching(results,
				dataList, samplesTaken);
		masterList = matcher.getMatchedList();	
		return masterList;
		
	}
	
	/**
	 * Load identification database for evaluation 
	 * @param databaseFile the standards database file
	 * @return Map of database formula to the line
	 */
	private Map<String, String> loadGroundTruthDB(String databaseFile) {

		if (databaseFile == null) {
			return null;
		}
		
		Map<String, String> database = new HashMap<String, String>();
		try {
			List<String> lines = Files.readAllLines(Paths.get(databaseFile),
					Charset.defaultCharset());
			for (String line : lines) {
				String[] tokens = line.split(",");
				String formula = tokens[2].trim();
				database.put(formula, line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Ground truth database=" + database);
		return database;
	
	}
	
}
