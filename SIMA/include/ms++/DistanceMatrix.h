/*$Id$*/

/*
 * DistanceMatrix.h
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

#ifndef __DISTANCEMATRIX_H__
#define __DISTANCEMATRIX_H__

#include <vector>
#include <iostream>
#include <iomanip>
#include <float.h>

#include <ms++/config.h>
#include <ms++/Error.h>
#include <ms++/Log.h>


namespace ms
{

/** Simple matrix object holding distance values.
 *
 *  This simple matrix object is designed to hold distance values,
 *  which are double and > 0. Also, this matrix is symmetric sice
 *  d(i,j) = d(j,i) for any given distance.
 */
class MSPP_EXPORT DistanceMatrix
{
public:
    DistanceMatrix();
    
    /** Set element (i,j) of the matrix to value.
     */
    void setValue(unsigned int i, unsigned int j, double value);

    /** Get/Modify element (i,j) of the matrix.
     */
    double& operator()(unsigned int i, unsigned int j);

    /** Returns the size of the Distance Matrix
     */
    unsigned int size();

    /** Find minimum entry and return its value
     */
    double getMin_Value();

    /** Find minimum entry and return its lower position index
     */
    unsigned int getMin_LowerIndex();

    /** Find minimum entry and return its upper position index
     */
    unsigned int getMin_HigherIndex();

    /** Add one element to the distance matrix. Distances are set to default value val. Size increases by 1.
     */
    void addElement();
    void addElement(double val);

    /** Delete the element element_n from the distance matrix. Size decreases by 1.
     */
    void deleteElement(unsigned int element_n);


private:
    void searchMinimum_();

    std::vector< std::vector<double> > D_ ;
    unsigned int size_;

    double min_;
    unsigned int min_i_;
    unsigned int min_j_;

    double ZeroReturn_;
};



/** Output Distance matrix
 */
MSPP_EXPORT std::ostream& operator<<(std::ostream& o, DistanceMatrix& dm);
} /*namespace ms*/





#endif

