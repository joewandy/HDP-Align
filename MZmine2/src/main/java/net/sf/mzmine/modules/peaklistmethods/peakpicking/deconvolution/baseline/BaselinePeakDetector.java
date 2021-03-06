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

package net.sf.mzmine.modules.peaklistmethods.peakpicking.deconvolution.baseline;

import static net.sf.mzmine.modules.peaklistmethods.peakpicking.deconvolution.baseline.BaselinePeakDetectorParameters.BASELINE_LEVEL;
import static net.sf.mzmine.modules.peaklistmethods.peakpicking.deconvolution.baseline.BaselinePeakDetectorParameters.MIN_PEAK_HEIGHT;
import static net.sf.mzmine.modules.peaklistmethods.peakpicking.deconvolution.baseline.BaselinePeakDetectorParameters.PEAK_DURATION;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.sf.mzmine.data.ChromatographicPeak;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.modules.peaklistmethods.peakpicking.deconvolution.PeakResolver;
import net.sf.mzmine.modules.peaklistmethods.peakpicking.deconvolution.ResolvedPeak;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.util.Range;

/**
 * This class implements a simple peak deconvolution algorithm. Continuous peaks
 * above a given baseline threshold level are detected.
 */
public class BaselinePeakDetector implements PeakResolver {

    public @Nonnull String getName() {
	return "Baseline cut-off";
    }


    @Override
    public ChromatographicPeak[] resolvePeaks(
	    final ChromatographicPeak chromatogram, final int[] scanNumbers,
	    final double[] retentionTimes, final double[] intensities,
	    ParameterSet parameters) {

	// Get parameters.
	final double minimumPeakHeight = parameters.getParameter(
		MIN_PEAK_HEIGHT).getValue();
	final double baselineLevel = parameters.getParameter(BASELINE_LEVEL)
		.getValue();
	final Range durationRange = parameters.getParameter(PEAK_DURATION)
		.getValue();

	final List<ResolvedPeak> resolvedPeaks = new ArrayList<ResolvedPeak>(2);

	// Current region is a region of consecutive scans which all have
	// intensity above baseline level.
	final int scanCount = scanNumbers.length;
	for (int currentRegionStart = 0; currentRegionStart < scanCount; currentRegionStart++) {

	    // Find a start of the region.
	    final DataPoint startPeak = chromatogram
		    .getDataPoint(scanNumbers[currentRegionStart]);
	    if (startPeak != null && startPeak.getIntensity() >= baselineLevel) {

		double currentRegionHeight = startPeak.getIntensity();

		// Search for end of the region
		int currentRegionEnd;
		for (currentRegionEnd = currentRegionStart + 1; currentRegionEnd < scanCount; currentRegionEnd++) {

		    final DataPoint endPeak = chromatogram
			    .getDataPoint(scanNumbers[currentRegionEnd]);
		    if (endPeak == null
			    || endPeak.getIntensity() < baselineLevel) {

			break;
		    }

		    currentRegionHeight = Math.max(currentRegionHeight,
			    endPeak.getIntensity());
		}

		// Subtract one index, so the end index points at the last data
		// point of current region.
		currentRegionEnd--;

		// Check current region, if it makes a good peak.
		if (durationRange.contains(retentionTimes[currentRegionEnd]
			- retentionTimes[currentRegionStart])
			&& currentRegionHeight >= minimumPeakHeight) {

		    // Create a new ResolvedPeak and add it.
		    resolvedPeaks.add(new ResolvedPeak(chromatogram,
			    currentRegionStart, currentRegionEnd));
		}

		// Find next peak region, starting from next data point.
		currentRegionStart = currentRegionEnd;

	    }
	}

	return resolvedPeaks.toArray(new ResolvedPeak[resolvedPeaks.size()]);
    }

    @Override
    public @Nonnull Class<? extends ParameterSet> getParameterSetClass() {
	return BaselinePeakDetectorParameters.class;
    }
}
