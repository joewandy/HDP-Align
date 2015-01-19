/*$Id: MahalanobisDistance.h 2849 2010-02-01 15:40:53Z martin.lindner@iwr.uni-heidelberg.de $*/

/*
 * MahalanobisDistance.h
 *
 * Copyright (c) 2009 Martin Lindner <martin.lindner@iwr.uni-heidelberg.de>
 *
 * This file is part of ms++.
 *
 * ms++ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ms++ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ms++. If not, see <http://www.gnu.org/licenses/>.
 *
 */

#ifndef __MAHALANOBISDISTANCE_H__
#define __MAHALANOBISDISTANCE_H__

#include <vector>
#include <vigra/matrix.hxx>
#include <ms++/config.h>
#include <ms++/Error.h>
#include <ms++/Log.h>
#include <ms++/PeakList.h>



namespace ms
{

/**
 * Calculate the Mahalanobis distance between Peak objects given a set of
 * normalization parameters.
 *
 * @author Martin Lindner <martin.lindner@iwr.uni-heidelberg.de>
 * @date 2009-06-15
 *
 */
class MSPP_EXPORT MahalanobisDistance
{
public:
	/**
	 * Calculates the Mahalanobis distance of two Peaks considering intensity thresholds as described by Bjoern Voss.
	 *
	 * Returns the the Mahalanobis distance of Peak A and Peak B with
	 * respect to the normalizations drt and dmz along the dimensions Retention
	 * Time (Rt) and Mass/Charge (Mz), if both Peak's intensities are above threshold dint.
	 */
    double operator()(const Peak& A, const Peak& B, double drt, double dmz, double dz, double dint);

};

} /*namespace ms*/

#endif

