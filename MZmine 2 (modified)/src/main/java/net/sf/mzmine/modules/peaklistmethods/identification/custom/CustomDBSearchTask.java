/*
 * Copyright 2006-2012 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin
 * St, Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.peaklistmethods.identification.custom;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.mzmine.data.PeakIdentity;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.impl.SimplePeakIdentity;
import net.sf.mzmine.data.impl.SimplePeakListAppliedMethod;
import net.sf.mzmine.main.MZmineCore;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.parametertypes.MZTolerance;
import net.sf.mzmine.parameters.parametertypes.RTTolerance;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;
import net.sf.mzmine.util.Range;

import com.Ostermiller.util.CSVParser;

class CustomDBSearchTask extends AbstractTask {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private PeakList peakList;

    private String[][] databaseValues;
    private String[][] extraDatabaseValues;
    private int extraLimit;
    private int finishedLines = 0;

    private File dataBaseFile;
    private File extraDataBaseFile;
    private String fieldSeparator;
    private FieldItem[] fieldOrder;
    private boolean ignoreFirstLine;
    private MZTolerance mzTolerance;
    private RTTolerance rtTolerance;
    private ParameterSet parameters;

    CustomDBSearchTask(PeakList peakList, ParameterSet parameters) {

	this.peakList = peakList;
	this.parameters = parameters;

	dataBaseFile = parameters.getParameter(
		CustomDBSearchParameters.dataBaseFile).getValue();
	extraDataBaseFile = parameters.getParameter(
			CustomDBSearchParameters.extraDataBaseFile).getValue();
	extraLimit = parameters.getParameter(
			CustomDBSearchParameters.extraLimit).getValue();
	
	fieldSeparator = parameters.getParameter(
		CustomDBSearchParameters.fieldSeparator).getValue();

	fieldOrder = parameters.getParameter(
		CustomDBSearchParameters.fieldOrder).getValue();

	ignoreFirstLine = parameters.getParameter(
		CustomDBSearchParameters.ignoreFirstLine).getValue();
	mzTolerance = parameters.getParameter(
		CustomDBSearchParameters.mzTolerance).getValue();
	rtTolerance = parameters.getParameter(
		CustomDBSearchParameters.rtTolerance).getValue();

    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getFinishedPercentage()
     */
    public double getFinishedPercentage() {
		if (databaseValues == null) {
		    return 0;		
		} else {
			if (extraDatabaseValues == null) {
				return ((double) finishedLines) / databaseValues.length;			
			} else {
				return ((double) finishedLines) / (databaseValues.length + extraDatabaseValues.length);
			}
		}
    }

    /**
     * @see net.sf.mzmine.taskcontrol.Task#getTaskDescription()
     */
    public String getTaskDescription() {
	return "Peak identification of " + peakList + " using database "
		+ dataBaseFile;
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {

	setStatus(TaskStatus.PROCESSING);

	try {
		
	    // read database contents in memory
	    FileReader dbFileReader = new FileReader(dataBaseFile);
	    databaseValues = CSVParser.parse(dbFileReader,
		    fieldSeparator.charAt(0));
	    
	    // read additional database contents in memory, if provided
	    if ( extraDataBaseFile.getPath() != null && !("".equals(extraDataBaseFile.getPath())) ) {

	    	// load and shuffle extra db records
	    	FileReader extraDbFileReader = new FileReader(extraDataBaseFile);
		    extraDatabaseValues = CSVParser.parse(extraDbFileReader,
				    fieldSeparator.charAt(0));
		    Collections.shuffle(Arrays.asList(extraDatabaseValues));		    

		    int dbCount = 0;
		    int extraCount = 0;
		    if (ignoreFirstLine) {
		    	dbCount++;
		    	extraCount++;
		    	extraLimit++;
		    }
		    
		    Map<String, String> databaseKeys = new HashMap<String, String>();
		    for (; dbCount < databaseValues.length; dbCount++) {
				try {
					String id = processOneLine(databaseValues[dbCount], CountVotes.DB_SOURCE_STANDARD);
					databaseKeys.put(id, id);
				} catch (Exception e) {
				    // ingore incorrect lines
				}
		    	finishedLines++;
		    }		    		    
		    for (; extraCount < extraLimit; extraCount++) {
				try {
				    processOneLine(extraDatabaseValues[extraCount], CountVotes.DB_SOURCE_EXTRA);
				} catch (Exception e) {
				    // ingore incorrect lines
				}
		    	finishedLines++;
		    }    
		    
		    dbFileReader.close();
		    extraDbFileReader.close();
		    
		    CountVotes cv = new CountVotes(peakList);
		    cv.countVotes(CountVotes.DB_SOURCE_STANDARD, databaseKeys);
		    
		    /* 
		     * TODO: here we remove the annotated id. once we're done 
		     * this is a hack for experiments to evaluate performance 
		     */
			for (PeakListRow peakRow : peakList.getRows()) {
                for (final PeakIdentity id : peakRow.getPeakIdentities()) {
                    peakRow.removePeakIdentity(id);
                }
			}
	    
	    } else {

	    	// the rest of mzmine as per normal 
		    if (ignoreFirstLine) {
		    	finishedLines++;
		    }

		    for (; finishedLines < databaseValues.length; finishedLines++) {
				try {
				    processOneLine(databaseValues[finishedLines], dataBaseFile.getName());
				} catch (Exception e) {
				    // ingore incorrect lines
				}
		    }
		    
		    dbFileReader.close();	    	
	    }
	    
	} catch (Exception e) {
	    logger.log(Level.WARNING, "Could not read file " + dataBaseFile, e);
	    setStatus(TaskStatus.ERROR);
	    errorMessage = e.toString();
	    return;
	}

	// Add task description to peakList
	peakList.addDescriptionOfAppliedTask(new SimplePeakListAppliedMethod(
		"Peak identification using database " + dataBaseFile,
		parameters));

	// Repaint the window to reflect the changes in the peak list
	MZmineCore.getDesktop().getMainFrame().repaint();

	setStatus(TaskStatus.FINISHED);

    }

    private String processOneLine(String values[], String customSource) {

	int numOfColumns = Math.min(fieldOrder.length, values.length);

	String lineID = null, lineName = null, lineFormula = null;
	double lineMZ = 0, lineRT = 0;

	for (int i = 0; i < numOfColumns; i++) {
	    if (fieldOrder[i] == FieldItem.FIELD_ID)
		lineID = values[i];
	    if (fieldOrder[i] == FieldItem.FIELD_NAME)
		lineName = values[i];
	    if (fieldOrder[i] == FieldItem.FIELD_FORMULA)
		lineFormula = values[i];
	    if (fieldOrder[i] == FieldItem.FIELD_MZ)
		lineMZ = Double.parseDouble(values[i]);
	    if (fieldOrder[i] == FieldItem.FIELD_RT)
		lineRT = Double.parseDouble(values[i]);
	}

	SimplePeakIdentity newIdentity = new SimplePeakIdentity(lineName,
			lineFormula, customSource, lineID, null);
	int i = 0;
	for (PeakListRow peakRow : peakList.getRows()) {

		i++;
		if (i == 2840) {
			System.out.println("Found");
		}
		
	    Range mzRange = mzTolerance.getToleranceRange(peakRow
		    .getAverageMZ());
	    Range rtRange = rtTolerance.getToleranceRange(peakRow
		    .getAverageRT());

	    boolean mzMatches = (lineMZ == 0d) || mzRange.contains(lineMZ);
	    boolean rtMatches = (lineRT == 0d) || rtRange.contains(lineRT);

	    if (mzMatches && rtMatches) {

		logger.finest("Found compound " + lineID + " (m/z " + lineMZ
			+ ", RT " + lineRT + ")");

		// add new identity to the row
		peakRow.addPeakIdentity(newIdentity, false);

		// Notify the GUI about the change in the project
		MZmineCore.getCurrentProject().notifyObjectChanged(peakRow,
			false);

	    }
	}

	return lineID;
    }

    public Object[] getCreatedObjects() {
	return null;
    }

}
