/*$Id$*/

/*
 * Alignment.h
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

#ifndef __ALIGNMENT_H__
#define __ALIGNMENT_H__

#include <vector>
#include <algorithm>
#include <iostream>
#include <fstream>
#include <cmath>

#include <ms++/config.h>
#include <ms++/Error.h>
#include <vigra/matrix.hxx>

#include <ms++/PeakList.h>
#include <ms++/StableMarriage.h>
#include <ms++/DistanceMatrix.h>

#include <time.h>


// for friend declaration further down
struct AlignmentTest;

namespace ms
{
    typedef vigra::linalg::Matrix<double> DBL_MATRIX;

    /** Alignment class
     *
     * The Alignment class allows the computation of the Master Time Scale from given 
     * Rt-correspondences.
     */
    class MSPP_EXPORT Alignment
    {
    public:
        friend struct ::AlignmentTest;

        /** Alignment Constructor
         *  @param Rt-Correspondences from a PeakListCollection
         *
         * Since an Alignment is only valid for a specific dataset, it can only
         * be constructed from the Rt-Correspondences of a specific dataset.
         */
        Alignment(DBL_MATRIX& rtc) {setRTC(rtc);};

        ~Alignment() {};

        /** Calculate Master Time Scale
         *  @return Matrix holding Master Time Scale. Each row corresponds to one point on the MTS.
         *  @param n_points Specifies on how many points the MTS will be sampled.
         *
         * getMasterTimeScale returns the Master Time Scale (MTS) of the given Alignment.
         * The MTS is sampled on n_points points in the range of the given rt_values in the
         * Rt-Correspondences rtc. Use standard values for all parameters.
         */
        DBL_MATRIX getMasterTimeScale(int n_points);


        /**
         * Change the Rt-Correspondences from which the Aligmnent is calculated
         */
        void setRTC(DBL_MATRIX& rtc);


        /**
         * Write Rt-Corresponences to a file (eg. for plotting).
         * Correspondent Rt-values from different lists are stored in one line.
         */
        void writeRTC(std::string filename);


        /**
         * Write Master Time Scale to a file (eg. for plotting).
         * Correspondent Rt-values from different lists are stored in one line.
         */
        void writeMTS(std::string filename);
        void writeMTS(std::string filename, DBL_MATRIX& MTS);

    private:
        //Sigmoid function for 1-dimensional input values
        double sigmoid_(double delta, double alpha);


        //Sigmoid function for n-dimionsional delta
        DBL_MATRIX sigmoid_(DBL_MATRIX& delta, double alpha);


        //Sum over sigmoid kernels in point x0
        double sumSigmoid_(DBL_MATRIX& x0, DBL_MATRIX& rt_vec, double alpha);


        //Estimate the alpha parameter of the sigmoid function
        DBL_MATRIX estimateAlpha_(DBL_MATRIX& points, DBL_MATRIX& rt_vec);


        //Create bisection line in dimensions-dimensional space going from min_rt to max_rt using size sampling points
        DBL_MATRIX initTimeScale_(unsigned int dimensions, double min_rt, double max_rt, unsigned int size);


        //Calculate value in x0 on the hyperplane defined by support vector y
        double valOnHPlane_(DBL_MATRIX& x0, DBL_MATRIX& y, DBL_MATRIX& rt_vec, double alpha);


        //Calculate gradient in x0 on the hyperplane defined by support vector y and return gradient as column vector
        DBL_MATRIX gradOnHPlane_(DBL_MATRIX& x0, DBL_MATRIX& y, DBL_MATRIX& rt_vec, double alpha);


        //Calculate new step size for gradient descent
        double stepSizeND_(DBL_MATRIX& xk, DBL_MATRIX& y, DBL_MATRIX& d, double lambda_old, double c1, double c2, DBL_MATRIX& rt_vec, double alpha);


        //Calculate master time scale via a gradient descent method
        DBL_MATRIX lineDistort_(DBL_MATRIX& linePoints, DBL_MATRIX& y, DBL_MATRIX& rt_vec, double c1, double c2, DBL_MATRIX& alpha);


        DBL_MATRIX rtc_;
    };


    //Extracts row i from given matrix and returns a 1*n row vector
    DBL_MATRIX MSPP_EXPORT extractRow(DBL_MATRIX& mat, int i);


    //Extracts column i from given matrix and returns a n*1 column vector
    DBL_MATRIX MSPP_EXPORT extractColumn(DBL_MATRIX& mat, int i);


} /*namespace ms*/

#endif

