/*$Id:$*/

/*
 * StableMarriage.cpp
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

#include "ms++/StableMarriage.h"

using namespace ms;



std::vector<int> StableMarriage::sortPeakList_(PeakList& inputPL, PeakList& sortedPL)
{
    //MSPP_LOG(logDEBUG4) << "StableMarriage::sortPeakList(): Sorting PeakList by Mz." << std::endl;
    //set sortedPL as a copy of input
    sortedPL.clear();
    sortedPL.resize(inputPL.size());
    for(unsigned int i = 0; i<inputPL.size(); i++){
        sortedPL[i] = inputPL[i];
    }
    
	//abuse RegionID to label the order of the peaks
	for(unsigned int i = 0; i<sortedPL.size(); i++){
		sortedPL[i].setRegionId(i);
	}

	//sort labeled peak list
	std::stable_sort(sortedPL.begin(),sortedPL.end(),PeakList::LessThanMz<Peak,Peak>());

	//analyze RegionId data to get the order map
	std::vector<int> order (inputPL.size(),0);
	for(unsigned int i = 0; i<sortedPL.size(); i++){
		order[i] = sortedPL[i].getRegionId(); 
	}

	//rename RegionId to original values
    for(unsigned int i = 0; i< inputPL.size(); i++){
        sortedPL[i].setRegionId(inputPL[order[i]].getRegionId());
    }

	return order;
}



void StableMarriage::calculatePreferenceLists_()
{
    //MSPP_LOG(logDEBUG3) << "StableMarriage::calculatePreferenceLists(): Calculating Preference Lists for stableMarriage." << std::endl;
    mspp_precondition(drt_ > 0 && dmz_ > 0 , "StableMarriage::calculatePreferenceLists(): drt and dmz have to be positive");

	int size1 = pl1_.size();
	int size2 = pl2_.size();
    
    mspp_precondition(size1 > 0 && size2 > 0 , "StableMarriage::calculatePreferenceLists(): Peak list must contain at least one element");

	//sort peak lists by Mz
    PeakList sorted1;
	PeakList sorted2;
	std::vector<int> order1 = sortPeakList_(pl1_, sorted1);
	std::vector<int> order2 = sortPeakList_(pl2_, sorted2);

    //preallocate preference lists
	pref12_.clear();
	pref21_.clear();
	pref12_.resize(size1);
	pref21_.resize(size2);

    //initialize mahalanobis distance
	MahalanobisDistance MD;

    
    //calculate preference list as implemented by Björn Voss
	int lower = 0;
	int upper = 0;

	for(int i = 0; i < size1; i++){  
        
		//shift lower bound through sorted peak list until we find an element in dmz range of i
		while( lower < size2 && ( sorted1[i].getMz() - sorted2[lower].getMz() >= dmz_ ) ){
			lower++;
		}
		upper = lower;
        
		//shift upper bound until we find an element which is out of dmz range. record elements in preference list
		while( upper < size2 && (  sorted1[i].getMz() - sorted2[upper].getMz() >= -dmz_  ) ){
            
            PrefListEntry entry1 = {order2[upper], MD(sorted1[i],sorted2[upper],drt_,dmz_,dz_,dint_) };
			pref12_[order1[i]].push_back(entry1);

            PrefListEntry entry2 = {order1[i], MD(sorted1[i],sorted2[upper],drt_,dmz_,dz_,dint_) };
			pref21_[order2[upper]].push_back(entry2);

			upper++;  
		}

	}



	//sort each preference list for every peak by distance
	for(int i = 0; i<size1; i++){
        sort( pref12_[i].begin(), pref12_[i].end(), PrefList_LessThanDistance() );
	}
	for(int i = 0; i<size2; i++){
		sort( pref21_[i].begin(), pref21_[i].end(), PrefList_LessThanDistance() );
	}


}



void StableMarriage::stableMarriage_()
{
    //MSPP_LOG(logDEBUG2) << "StableMarriage::stableMarriage(): Calculating a Stable Marriage of two PeakLists." << std::endl;
    
    int size1 = pref12_.size();
    int size2 = pref21_.size();

    if(size1 <= 0 || size2 <= 0){
        MSPP_LOG(logERROR) << "StableMarriage::stableMarriage(): Preference Lists must not be empty." << std::endl;
    }
    mspp_precondition(size1 > 0 && size2 > 0 , "StableMarriage::stableMarriage(): Preference Lists must not be empty.");


    //prepare assignment vectors
    assignment12_.clear();
    assignment21_.clear();
    assignment12_.resize(size1,-1); //  (-1,-1,-1,-1,-1,-1,-1,-1,-1,-1)
    assignment21_.resize(size2,-1); //  (-1,-1,-1,-1,-1,-1,-1,-1)

    //currrent preference index for pref12
    std::vector<unsigned int> partnerIndex (size1,0); //try to get nearest peak in the beginning

    //these lines should make the code more readable
    const bool NOT_PROCESSED = true;
    const bool DONE = false;

    //binary list: processing status of element pref12/pref21[i]
    std::vector<bool> ProcessStatus1 (size1, NOT_PROCESSED);
    std::vector<bool> ProcessStatus2 (size2, NOT_PROCESSED);

     while(true){
        //search for peak in 1 to be matched
        unsigned int currentPeak = 0;
        while(ProcessStatus1[currentPeak]== DONE && currentPeak<ProcessStatus1.size()){
            currentPeak++;
        }
        
        //discard current peak if all preferred peaks are already matched to other peaks
        if(pref12_[currentPeak].size() < partnerIndex[currentPeak]+1){

            ProcessStatus1[currentPeak] = DONE; //set status to "done", but do not assign a partner

        } else {

            //get index of nearest possible peak in B
            int partnerPeak = pref12_[currentPeak][partnerIndex[currentPeak]].index;
            
            //check if most preferred peak is too far away (distance > limit, if limit is  > 0)
            if(limit_ > 0 && pref12_[currentPeak][partnerIndex[currentPeak]].distance > limit_){

                ProcessStatus1[currentPeak] = DONE; //set status to "done", but do not assign a partner

            } else { //if preferred peak is near enough:

                if(ProcessStatus2[partnerPeak] == NOT_PROCESSED){ //if partnerPeak is still free

                    //assign pair
                    assignment12_[currentPeak] = partnerPeak;
                    assignment21_[partnerPeak] = currentPeak;
                    ProcessStatus1[currentPeak] = DONE;
                    ProcessStatus2[partnerPeak] = DONE;

                } else { //if partnerPeak is already assigned to oldPeak

                    int oldPeak = assignment21_[partnerPeak];
                    double oldDistance = pref12_[oldPeak][partnerIndex[oldPeak]].distance;

                    //check if currentPeak is nearer than oldPeak
                    if(pref12_[currentPeak][partnerIndex[currentPeak]].distance < oldDistance){

                        ProcessStatus1[oldPeak] = NOT_PROCESSED;       //old peak has to be processed again
                        partnerIndex[oldPeak]++;    //take next partner in list next time
                        assignment12_[oldPeak] = -1;  //delete assignment

                        //assign pair
                        ProcessStatus1[currentPeak] = DONE;
                        ProcessStatus2[partnerPeak] = DONE;
                        assignment12_[currentPeak] = partnerPeak;
                        assignment21_[partnerPeak] = currentPeak;

                    } else {
                        //take next possible partner in list for the next time
                        partnerIndex[currentPeak]++;                        
                    }                    
                }
            }
        }

        //check if we are done
        bool all_done = true;
        for(int k = 0; k<size1; k++){
            if(ProcessStatus1[k] == NOT_PROCESSED){ //not finished if there is at least one peak not processed
                all_done = false;
            }
        }

        if(all_done){ //leave loop, if all are processed
            MSPP_LOG(logDEBUG2) << "StableMarriage::stableMarriage(): stableMarriage finished successfully." << std::endl;
            break;
        }

     }

     
     //calculate the average "cost" of the matching (= quality of matching)
     double cost = 0.;
     int count = 0;
     for(unsigned int i = 0; i<assignment12_.size(); i++){
         if(assignment12_[i] >= 0){
             cost += pref12_[i][partnerIndex[i]].distance;
             count++;
         }
     }
     if(count != 0){
         cost_ = cost / double(count);
     } else {
        cost_ = DBL_MAX;
     }
}



StableMarriage::Assignment StableMarriage::getAssignment()
{
    Assignment Result;

    recalculateIfNescessary();

    Result.ass12 = assignment12_;
    Result.ass21 = assignment21_;
    Result.cost = cost_;

    return Result;
}



StableMarriage::RtAssignment StableMarriage::getRtAssignment()
{
    RtAssignment Result;
    Result.clear();

    recalculateIfNescessary();

    for(unsigned int i = 0; i < assignment12_.size(); i++){  //go through Peaklist 1
        if(assignment12_[i] >= 0){                  //if peak i is assigned...
            RtPair temp;
            temp.Rt1 = pl1_[i].getRt();
            temp.Rt2 = pl2_[assignment12_[i]].getRt();
            Result.push_back(temp);                 //Add both Rt-values to the list.
        }
    }

    //finally sort RtPair list by first value
    std::sort(Result.begin(), Result.end(),RtPair_LessThanRt1());

    return Result;
}



double StableMarriage::getCost()
{
    recalculateIfNescessary();

    return cost_;
}



void StableMarriage::recalculateIfNescessary()
{
    if(!upToDate_){
        //Recalculate if calculations are not up to date
        
        calculatePreferenceLists_();
        stableMarriage_();
        
        
        upToDate_ = true;
    }   

    return;
}

