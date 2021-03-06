package com.joewandy.alignmentResearch.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;


public class AlignmentList {

	private List<AlignmentRow> rows;
	private AlignmentFile data;
	private String id;
	
	/**
	 * Constructs a new simple alignment list
	 */
	public AlignmentList(String id) {
		this.rows = new ArrayList<AlignmentRow>();
		this.id = id;
	}
	
	/**
	 * Constructs a new simple alignment list, with rows initialised
	 * to each feature inside data
	 * @param data The data file
	 */
	public AlignmentList(AlignmentFile data) {

		this.rows = new ArrayList<AlignmentRow>();
		this.data = data;
		this.id = data.getFilenameWithoutExtension();
		
		// puts each feature inside data into a new row
		int rowId = 0;
		for (Feature f : data.getFeatures()) {
			AlignmentRow row = new AlignmentRow(this, rowId);
			row.addFeature(f);
			rowId++;
			rows.add(row);
		}
		
	}
		
	/**
	 * Constructs a new simple alignment list from entries inside inputFilePath
	 * Currently used for reading back the results from running SIMA as an external process
	 * @param inputFilePath Path to input file generated by other tools
	 * @param dataList List of all data files
	 * @throws FileNotFoundException 
	 */
	public AlignmentList(String inputFilePath, List<AlignmentFile> dataList, String id) throws FileNotFoundException {

		// we'll keep the alignment results here
		this.rows = new ArrayList<AlignmentRow>();
		
		// id is not really used for anything now
		this.id = id;
		
		// create a map of filename -> data, for convenience
		Map<String, AlignmentFile> map = new HashMap<String, AlignmentFile>();
		for (AlignmentFile data : dataList) {
			map.put(data.getFilenameWithoutExtension(), data);
		}
		
		File inputFile = new File(inputFilePath);
		Scanner in = new Scanner(inputFile);
		
		int currentId = Integer.MIN_VALUE;
		int prevId = currentId;
		boolean firstLine = true;
		
		Set<Feature> alignedFeatures = new HashSet<Feature>();
		int rowId = 0;
		while (in.hasNextLine()) {

			String line = in.nextLine();
			Scanner lineSplitter = new Scanner(line);

			// keep track of previous alignment group id, except for the first line read
			if (!firstLine) {
				prevId = currentId;
			}
			
			// first column in the line is the alignment group id
			currentId = lineSplitter.nextInt();
			
			// second column in the line is the originating file name
			String filename = lineSplitter.next();
			
			// must remember to remove extension to match the map key
			String fileNameWithOutExt = AlignmentFile.removeExtension(filename);
			AlignmentFile data = map.get(fileNameWithOutExt);
			
			// third column in the line is position of feature in data
			int peakIdx = lineSplitter.nextInt();
			Feature feature = null;
			double prob = 0.0;
			String annot = "";
			try {
				feature = data.getFeatureByIndex(peakIdx);		
				if (feature == null) {
					continue;
				}
				// read the 4th and 5th column too if present
				if (lineSplitter.hasNext()) {
					double mass = lineSplitter.nextDouble(); // ignored
					double rt = lineSplitter.nextDouble(); // ignored
				}
				// read some more if present
				if (lineSplitter.hasNext()) {
					prob = lineSplitter.nextDouble();
					annot = lineSplitter.next();					
				}
			} catch (IndexOutOfBoundsException e) {
				// invalid entry in the alignment result, skipping ...
//				System.out.println("Invalid index " + peakIdx + " for " + data.getFilename() + " (" + data.getFeaturesCount() + ")");
				continue;
			} finally {
				lineSplitter.close();				
			}			
			
			// is this line the start of a new alignment group (row) ?
			if (!firstLine && prevId != currentId) {

				// starting a new alignment group, store the previously aligned features 
				AlignmentRow row = new AlignmentRow(this, rowId);
				row.addAlignedFeatures(alignedFeatures);
				rowId++;
				this.addRow(row);
				
				// keep the current feature in a new set
				alignedFeatures = new HashSet<Feature>();
				feature.setAnnotation(annot);
				alignedFeatures.add(feature);		
				
				// the probability is the same for all member features in this alignment row
				row.setScore(prob);
				
			} else {
				
				// still continuing the previous alignment group
				alignedFeatures.add(feature);

			}
			
			firstLine = false;
			
		}
		in.close();
		
		// store all the remaining aligned features in the final row
		AlignmentRow row = new AlignmentRow(this, rowId);
		row.addAlignedFeatures(alignedFeatures);
		this.addRow(row);		
		
//		System.out.println("Alignment entries loaded = " + this.rows.size() + " rows");

	}
	
	public List<AlignmentRow> getRows() {
		return rows;
	}
	
	public int getLastRowId() {
		// find last accepted row id
		int lastRowId = 0;
		for (AlignmentRow acc : rows) {
			if (acc.getRowId() > lastRowId) {
				lastRowId = acc.getRowId();
			}
		}
		return lastRowId;
	}
	
	public AlignmentFile getRowsAsFile() {
		List<Feature> features = new ArrayList<Feature>();
		for (AlignmentRow row : rows) {
			features.add(row.asFeature());
		}
		AlignmentFile file = new AlignmentFile(0, "", features);
		return file;
	}
	
	public AlignmentRow getRandomRow() {
		List<AlignmentRow> copy = new ArrayList<AlignmentRow>(rows);
		Collections.shuffle(copy);
		return copy.get(0);
	}

	public List<AlignmentRow> getUnalignedRows() {
		List<AlignmentRow> result = new ArrayList<AlignmentRow>();
		for (AlignmentRow row : rows) {
			if (!row.isAligned()) {
				result.add(row);
			}
		}
		return result;
	}
	
	public void clearRows() {
		rows.clear();
	}
	
	public void addRow(AlignmentRow row) {
		rows.add(row);
	}

	public void addRows(List<AlignmentRow> rows) {
		this.rows.addAll(rows);
	}
	
	public void setRows(List<AlignmentRow> rows) {
		this.rows = rows;
	}
	
	public int getRowsCount() {
		return rows.size();
	}
	
	public AlignmentFile getData() {
		return data;
	}
	
	public String getId() {
		return id;
	}
	
	public boolean isAligned(Feature f1, Feature f2) {
		for (AlignmentRow row : getRows()) {
			if (row.contains(f1) && row.contains(f2)) {
				return true;
			}
		}
		return false;
	}

	public Set<AlignmentRow> getRowsInRange(AlignmentRow reference, double massTol, double rtTol, 
			boolean usePpm) {
		
		Set<AlignmentRow> result = new HashSet<AlignmentRow>();
		
		for (AlignmentRow toCheck : this.rows) {
			if (reference.rowInRange(toCheck, massTol, rtTol, usePpm)) {
				result.add(toCheck);				
			}
		}

		return result;
		
	}

	public Set<AlignmentRow> getUnalignedRowsInRange(AlignmentRow reference, double massTol, double rtTol, 
			boolean usePpm) {
		
		Set<AlignmentRow> result = new HashSet<AlignmentRow>();
		
		for (AlignmentRow toCheck : this.rows) {
			if (reference.rowInRange(toCheck, massTol, rtTol, usePpm) && !toCheck.isAligned()) {
				result.add(toCheck);				
			}
		}

		return result;
		
	}
	
	public Set<AlignmentRow> getRowsInRange(AlignmentRow reference, double massTol, boolean usePpm) {
		
		Set<AlignmentRow> result = getRowsInRange(reference, massTol, -1, usePpm);
		return result;
		
	}

	public Set<AlignmentRow> getUnalignedRowsInRange(AlignmentRow reference, double massTol, boolean usePpm) {
		
		Set<AlignmentRow> result = getUnalignedRowsInRange(reference, massTol, -1, usePpm);
		return result;
		
	}
	
	public AlignmentRow getRowContaining(Feature feature) {
		for (AlignmentRow row : this.rows) {
			Set<Feature> rowFeatures = row.getFeatures();
			if (rowFeatures.contains(feature)) {
				return row;
			}
		}
		return null;
	}
			
	@Override
	public String toString() {
		return "SimpleAlignmentList [rows=" + rows.size() + ", data=" + data + "]";
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
		AlignmentList other = (AlignmentList) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
