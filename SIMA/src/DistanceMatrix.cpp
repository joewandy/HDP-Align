/*$Id$*/

/*
 * DistanceMatrix.cpp
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

#include "ms++/DistanceMatrix.h"

using namespace ms;


DistanceMatrix::DistanceMatrix()
{
    D_.clear();
    size_ = 0;
}

void DistanceMatrix::setValue(unsigned int i, unsigned int j, double value)
{
    if(i == j){
        MSPP_LOG(logERROR) << "DistanceMatrix::setValue(): Diagonal elements cannot be set." << std::endl;
        return;
    } else if(i<size_ && j<size_){
        if(i>j){
            D_[i-1][j] = value;
            return;
        } else {
            D_[j-1][i] = value;
            return;
        }
    } else {
        MSPP_LOG(logERROR) << "DistanceMatrix::setValue(): Index out of bounds." << std::endl;
        mspp_precondition(false , "DistanceMatrix::setValue(): Index out of bounds.");
        return;
    }    
}


double& DistanceMatrix::operator()(unsigned int i, unsigned int j)
{
    ZeroReturn_ = 0.;
    if(i == j){
        return ZeroReturn_;
    } else if(i<size_ && j<size_){
        if(i>j){
            return D_[i-1][j];
        } else {
            return D_[j-1][i];
        }
    } else {
        MSPP_LOG(logERROR) << "DistanceMatrix::operator(): Index out of bounds." << std::endl;
        mspp_precondition(false , "DistanceMatrix::operator(): Index out of bounds.");
        return ZeroReturn_;
    }
}

unsigned int DistanceMatrix::size()
{
    return size_;
}

void DistanceMatrix::searchMinimum_()
{
    min_ = DBL_MAX;
    min_i_ = 0;
    min_j_ = 0;

    for(unsigned int i = 0; i < D_.size(); i++){
        for(unsigned int j = 0; j < D_[i].size(); j++){
           if( D_[i][j] < min_){
                min_ = D_[i][j];
                min_i_ = i;
                min_j_ = j;
            }
        }
    }
    //increase min_i_ to fit indices
    min_i_++;
    return;
}

double DistanceMatrix::getMin_Value()
{
    searchMinimum_();

    return min_;
}

unsigned int DistanceMatrix::getMin_LowerIndex()
{
    searchMinimum_();

    if(min_i_ < min_j_){
        return min_i_;
    } else {
        return min_j_;
    }
}

unsigned int DistanceMatrix::getMin_HigherIndex()
{
    searchMinimum_();

    if(min_i_ > min_j_){
        return min_i_;
    } else {
        return min_j_;
    }
}

void DistanceMatrix::addElement(double val)
{
    std::vector<double> new_element (size_ , val);
    size_++;

    if(size_ > 1){
        D_.push_back(new_element);
    }

    return;
}

void DistanceMatrix::addElement()
{
    addElement(DBL_MAX);

    return;
}


void DistanceMatrix::deleteElement(unsigned int element_n)
{
    
    if(element_n < size_){
        if(size_ == 1){
            //do nothing, D_ is already empty
            size_--;
        } else {

            //delete distances in column i (i>element_n)
            for(unsigned int i = element_n; i<D_.size(); i++){
                D_[i].erase(D_[i].begin()+element_n);
            }
            //delete column element_n
            if(0 == element_n){
                D_.erase(D_.begin());
            }else{
                D_.erase(D_.begin()+element_n-1);
            }
            size_--;
        }        
    }
    
    return;
}


MSPP_EXPORT std::ostream& operator<<(std::ostream& o, DistanceMatrix& dm)
{
    o << std::endl << "+-";
    for(unsigned int i = 0; i < dm.size(); i++){
        o << "--------------";
    }
    o << "-+" << std::endl ;
    for(unsigned int i = 0; i < dm.size(); i++){
        o << "| ";
        for(unsigned int j = 0; j < dm.size(); j++){
            o << std::setw(14) << dm(i,j);
        }
        o << " |" << std::endl;
    }
    o << "+-";
    for(unsigned int i = 0; i < dm.size(); i++){
        o << "--------------";
    }
    o << "-+" << std::endl ;
    return o;
}