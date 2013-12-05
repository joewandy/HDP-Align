package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.JFrame;

import net.sf.mzmine.data.ChromatographicPeak;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.PeakStatus;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.impl.SimpleChromatographicPeak;
import net.sf.mzmine.data.impl.SimpleDataPoint;
import net.sf.mzmine.data.impl.SimplePeakList;
import net.sf.mzmine.data.impl.SimplePeakListAppliedMethod;
import net.sf.mzmine.data.impl.SimplePeakListRow;
import net.sf.mzmine.desktop.Desktop;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.project.MZmineProject;
import net.sf.mzmine.project.impl.RawDataFileImpl;
import net.sf.mzmine.taskcontrol.Task;
import net.sf.mzmine.taskcontrol.TaskEvent;
import net.sf.mzmine.taskcontrol.TaskListener;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.ExitCode;
import net.sf.mzmine.util.Range;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public abstract class MzMineAlignment extends BaseAlignment implements
		AlignmentMethod, TaskListener {

	public static boolean INIT = false;
	
	// the value is set inside matchFeatures(), used by statusChanged()
	private Task alignerTask;
	
	private Map<AlignmentFile, RawDataFile> dataMap;
	private BlockingQueue<AlignmentList> resultQueue;


	/**
	 * Creates a simple aligner
	 * 
	 * @param dataList
	 *            List of feature data to align
	 * @param massTolerance
	 *            Mass tolerance in ppm
	 * @param rtTolerance
	 *            Retention time tolerance in seconds
	 * @param rtDrift
	 */
	public MzMineAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		dataMap = new HashMap<AlignmentFile, RawDataFile>();
		resultQueue = new ArrayBlockingQueue<AlignmentList>(1);

		if (MzMineAlignment.INIT == false) {
			// launch mzmine
			String[] args = {};
			MZmineCore.main(args);
			// just to this once
			MzMineAlignment.INIT = true;
		}
				
	}

	/**
	 * Clone of joinAlign implemented in mzMine
	 * 
	 * @return
	 */
	public AlignmentList matchFeatures() {

		System.out.println("Hiding main window");
		Desktop desktop = MZmineCore.getDesktop();
		JFrame mainFrame = desktop.getMainFrame();
		mainFrame.setVisible(false);			
		mainFrame.dispose();
		
		removeShutdownHooks();

		// convert our dataList into mzMine peakList
		MZmineProject currentProject = MZmineCore.getCurrentProject();
		PeakList peakLists[] = new PeakList[dataList.size()];

		// map from our data file to mzmine raw data file
		for (int i = 0; i < dataList.size(); i++) {
			AlignmentFile data = dataList.get(i);
			PeakList peakList = this.initialisePeakList(data, dataMap);
			peakLists[i] = peakList;
			currentProject.addPeakList(peakList); // not necessary ?
		}

		// concrete subclass implementation should prepare the parameter set
		ParameterSet params = prepareParameterSet(peakLists);
		AlignmentList alignmentResult = new AlignmentList("");
		
		// show alignment dialog
		MZmineProcessingModule module = this.getAlignerModule();
		System.out.println("Setting parameters for module " + module.getName());
		ExitCode exitCode = ExitCode.OK;
		if (AlignmentMethodParam.SHOW_PARAM_SETUP_DIALOG) {
			exitCode = params.showSetupDialog();			
		}
		
		if (exitCode == ExitCode.OK) {
			
			ParameterSet parametersCopy = params.cloneParameter();
			System.out.println("Starting module " + module.getName()
					+ " with parameters " + parametersCopy);

			List<Task> tasks = new ArrayList<Task>();

			// the first entry in tasks is an instance of the active task
			module.runModule(parametersCopy, tasks);
			alignerTask = tasks.get(0);

			// register ourself as listener
			alignerTask.addTaskListener(this);

			// actually run the task here
			MZmineCore.getTaskController().addTasks(tasks.toArray(new Task[0]));
			
			/*
			 * take the alignment result from queue when it's available
			 * (i.e. when statusChanged FINISHED has been called
			 */
			try {
				alignmentResult = this.resultQueue.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		
//		mainFrame.dispose();
		mainFrame = null; // for gc

		PeakList[] allPeakLists = currentProject.getPeakLists();
		for (PeakList pl : allPeakLists) {
			currentProject.removePeakList(pl);
		}
		
		return alignmentResult;

	}

	
	
	public void statusChanged(TaskEvent e) {

		if (e.getStatus() == TaskStatus.FINISHED) {

			Object[] createdObjects = alignerTask.getCreatedObjects();

			// we know that createdObjects hold one single PeakList
			PeakList alignedPeakList = (PeakList) createdObjects[0];
			AlignmentList alignmentResult = new AlignmentList("");

			// convert back from peaklist to our feature list
			PeakListRow[] allRows = alignedPeakList.getRows();
			for (int i = 0; i < allRows.length; i++) {

				PeakListRow plRow = allRows[i];
				AlignmentRow alignedRow = new AlignmentRow(alignmentResult, i);
				alignmentResult.addRow(alignedRow);

				for (AlignmentFile key : dataList) {
					
					RawDataFile rawData = dataMap.get(key);
					ChromatographicPeak peak = plRow.getPeak(rawData);
					
					// not all data are present in every row
					if (peak != null) {
						double mass = peak.getMZ();
						double rt = peak.getRT() * 60; // convert from minute back to second !!
						// height is the intensity
						double intense = peak.getHeight(); 
						Feature feature = key.getFeatureByProperties(mass, rt, intense);
						if (feature == null) {
							throw new InvalidParameterException("Feature cannot be null");
						}
						alignedRow.addAlignedFeature(feature);
					}
				}

			}
			
			// done processing, put into result queue for matchFeatures() to consume
			try {
				this.resultQueue.put(alignmentResult);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}

		}

	}
	
	protected abstract ParameterSet prepareParameterSet(PeakList[] peakLists);
	
	protected abstract MZmineProcessingModule getAlignerModule();

	private PeakList initialisePeakList(AlignmentFile data,
			Map<AlignmentFile, RawDataFile> dataMap) {

		String peakListname = data.getFilename();
		System.out.println("Processing " + peakListname);

		RawDataFile[] dataFiles = new RawDataFile[1];
		try {
			dataFiles[0] = new RawDataFileImpl(peakListname);
			dataMap.put(data, dataFiles[0]);
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		PeakList buildingPeakList = new SimplePeakList(peakListname, dataFiles);
		String[] process = {};
		for (String description : process) {
			((SimplePeakList) buildingPeakList)
					.addDescriptionOfAppliedTask(new SimplePeakListAppliedMethod(
							description));
		}

		// Add task description to peakList
		((SimplePeakList) buildingPeakList)
				.addDescriptionOfAppliedTask(new SimplePeakListAppliedMethod(
						getTaskDescription(peakListname)));

		DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
		String dateCreated = df.format(new Date());
		((SimplePeakList) buildingPeakList).setDateCreated(dateCreated);

		// copy all the features into each row in peaklist
		int rowId = 1;
		for (Feature feature : data.getFeatures()) {

			SimplePeakListRow buildingRow = new SimplePeakListRow(rowId);
			rowId++;

			double mass = feature.getMass();
			double rt = feature.getRt() / 60d; // must be in minute !!
			double height = feature.getIntensity(); // height is the intensity
			double area = 0.0; // no area ...
			int[] scanNumbers = new int[] {};

			Range peakRTRange = new Range(rt);
			Range peakMZRange = new Range(mass);
			Range peakIntensityRange = new Range(height);
			DataPoint dataPoint = new SimpleDataPoint(mass, height);
			DataPoint[] mzPeaks = new DataPoint[] { dataPoint };

			SimpleChromatographicPeak peak = new SimpleChromatographicPeak(
					dataFiles[0], mass, rt, height, area, scanNumbers, mzPeaks,
					PeakStatus.DETECTED, -1, -1, peakRTRange, peakMZRange,
					peakIntensityRange);

			buildingRow.addPeak(dataFiles[0], peak);
			buildingPeakList.addRow(buildingRow);
			buildingRow = null;

		}

		System.out.println("Processed " + (rowId - 1) + " features");

		return buildingPeakList;

	}

	private String getTaskDescription(String peakListname) {
		return "Loading peak list from " + peakListname;
	}
	
	/**
	 * Hack-ish reflection voodoo here to remove the shutdown hooks that 
	 * were originally registered by MZmineCore. We use this as a workaround 
	 * to get rid of error messages due to config file not found when shutting down MZmine core.
	 * 
	 * If stuffs break, remove this !
	 * 
	 * http://stackoverflow.com/questions/6865408/i-need-to-list-the-hooks-registered-with-java-lang-applicationshutdownhooks
	 */
	private void removeShutdownHooks() {

		try {

			Class<?> clazz = Class
					.forName("java.lang.ApplicationShutdownHooks");
			Field field = clazz.getDeclaredField("hooks");
			field.setAccessible(true);
			Object hooks = field.get(null);

			System.out.println(hooks); // hooks is a Map<Thread, Thread>
			@SuppressWarnings("unchecked")
			Map<Thread, Thread> hooksMap = (Map<Thread, Thread>) hooks;
			Iterator<Thread> it = hooksMap.values().iterator();
			while (it.hasNext()) {
				Thread hook = it.next();
				it.remove();
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
	}

}
