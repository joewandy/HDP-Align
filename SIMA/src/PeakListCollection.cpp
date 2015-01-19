/*$Id$*/

/*
 * PeakListCollection.cpp
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

#include "ms++/PeakListCollection.h"
#include "ms++/Alignment.h"

namespace ms
{
    void PeakListCollection::addPeakList( const std::string& filename )
    {
        //Load PeakList from file
        PeakList P (filename);

        //push_back PeakList, if not empty
        if(P.size()>0){
            c_.push_back(P);
        }else{
            MSPP_LOG(logERROR) << "PeakListCollection::addPeakList(): Could not load PeakList from file '" << filename <<"'" << std::endl;
        }

        return;
    }



    void PeakListCollection::clear() {
        ms::Collection<PeakList>::clear();
        correspondenceMap_.clear();
        plContent_.clear();
    }



    void PeakListCollection::applyMasterTimeScale(DBL_MATRIX mts)
    {
        int dim = c_.size();
        int n_points = mts.rowCount();

        if(dim != mts.columnCount()){
            MSPP_LOG(logERROR) << "PeakListCollection::applyMasterTimeScale(): The Master Time Scale's dimension differs from the number of Peak Lists!" << std::endl;
        }
        mspp_precondition(dim == mts.columnCount() , "PeakListCollection::applyMasterTimeScale(): The Master Time Scale's dimension differs from the number of Peak Lists!");

        if(n_points < 2){
            MSPP_LOG(logERROR) << "PeakListCollection::applyMasterTimeScale(): Master Time Scale does not provide enough sampling points!" << std::endl;
        }
        mspp_precondition(dim == mts.columnCount() , "PeakListCollection::applyMasterTimeScale(): Master Time Scale does not provide enough sampling points!");


        //Correct each dimension by mts
        for(int i = 0; i < dim; i++){

            int length = c_[i].size();

            //process each Peak in PeakList i
            for(int j = 0; j < length; j++){
                double oldRt = c_[i][j].getRt();

                //search for interval of MTS to do interpolation
                if(mts(0,i)>oldRt){
                    MSPP_LOG(logWARNING) << "PeakListCollection::applyMasterTimeScale(): Could not correct Peak " << j << " in PeakList " << i << "! Rt-Value not covered by Master Time Scale." << std::endl;
                    continue;
                }
                int mts_index = 0;
                while(true){
                    mts_index++;
                    if(mts_index>=n_points){
                        MSPP_LOG(logWARNING) << "PeakListCollection::applyMasterTimeScale(): Could not correct Peak " << j << " in PeakList " << i << "! Rt-Value not covered by Master Time Scale." << std::endl;
                        break;
                    }
                    if(mts(mts_index,i)>oldRt){
                        break;
                    }
                }
                if(mts_index>=n_points){
                    continue;
                }

                //interval is now [mts(mts_index-1) , mts(mts_index)] =: [x0 , x1]
                //now do linear interpolation: x = x0 + (x1-x0)/(x1[i]-x0[i])*(oldRt-x0[i])

                //calculate interpolated point x on MTS with x[i] = oldRt
                DBL_MATRIX x0 = extractRow(mts,mts_index-1).transpose();
                DBL_MATRIX x1 = extractRow(mts,mts_index).transpose();
                DBL_MATRIX x = x0 + (x1 - x0)/(x1(i,0) - x0(i,0))*(oldRt - x0(i,0));


                //create unit vector in bisectional line direction
                DBL_MATRIX uBisec (dim,1,1./sqrt(double(dim)));

                //calculate distance vector to bisectional line
                DBL_MATRIX tmpProd = uBisec.transpose()*x;
                double dotProd = tmpProd(0,0);
                DBL_MATRIX dist = dotProd*uBisec - x;

                //update Rt value of current Peak: rt = oldRt + dist[i]
                c_[i][j].setRt(oldRt + dist(i,0));
            }
        }
        return;
    }



    DBL_MATRIX PeakListCollection::getAlignment(double drt, double dmz, double dz, double dint, AccelerationFlag flag = NORMAL, int param = 0)
    {

        //Create a copy of c_ to work on
        std::vector<PeakList> pls = c_;

        unsigned int oldSize = pls.size();

        // initialization

        //fill correspondenceMap with rt-values
        correspondenceMap_.clear();
        correspondenceMap_.resize(oldSize);

        for(unsigned int i = 0; i < oldSize; i++){
            correspondenceMap_[i].resize(pls[i].size());
            for(unsigned int j = 0; j < pls[i].size(); j++){
                //create map_item to write to correspondenceMap
                map_item tempItem;
                //origin information contains the PeakList index and rt value
                originInformation o;
                o.rt = c_[i][j].getRt();
                o.mz = c_[i][j].getMz();
                o.intensity = c_[i][j].getAbundance();
                o.originPeakList = i;
                o.originPeak = j;
                tempItem.push_back( o );
                correspondenceMap_[i][j] = tempItem;
            }
        }

        //PeakLists are not merged in the beginning --> fill with 1
        plContent_.clear();
        plContent_.resize(oldSize,1);

        DistanceMatrix D;
        if(flag == NORMAL){
            //build distance matrix
            D = buildDistanceMatrix_(drt,dmz,dz,dint);
        }

        if(flag == REFERENCE){
            //first, merge PeakList n with one other --> last PeakList contains PeakList n
            if(param==0){
                //select first two peaklists
                unsigned int merge_lower = param;
                unsigned int merge_upper = 1;

                //merge peaklists into a new one
                mergePeakLists_(merge_lower, merge_upper, pls, drt, dmz, dz, dint);
            }else{
                //select first and n'th PeakList
                unsigned int merge_lower = 0;
                unsigned int merge_upper = param;

                //merge peaklists into a new one
                mergePeakLists_(merge_lower, merge_upper, pls, drt, dmz, dz, dint);
            }
        }

        //merge, until there is only one PeakList left
        bool exit = false;
        while(!exit){
            //find cheapest assignment
            unsigned int merge_lower = 0;
            unsigned int merge_upper = 1;
            switch(flag){
            case NORMAL:
                merge_lower = D.getMin_LowerIndex();
                merge_upper = D.getMin_HigherIndex();
                break;

            case FAST:
                //select last two PeakList
                merge_lower = pls.size()-2;
                merge_upper = pls.size()-1;
                break;

            case REFERENCE:
                //select last two PeakList
                merge_lower = pls.size()-2;
                merge_upper = pls.size()-1;
                break;
            }

            //merge peaklists into a new one
            mergePeakLists_(merge_lower, merge_upper, pls, drt, dmz, dz, dint);


            switch(flag){
            case NORMAL:
                //update distance matrix
                updateDistanceMatrix_(D, merge_lower, merge_upper, drt, dmz, dz, dint);
                if(D.size()==1){
                    exit = true;
                }
                break;

            case FAST:
                if(pls.size()==1){
                    exit = true;
                }
                break;

            case REFERENCE:
                if(pls.size()==1){
                    exit = true;
                }
                break;
            }
        }

        DBL_MATRIX rtc = calculateRtCorrespondencesFromCorrespondenceMap_(oldSize);

        return rtc;


    }



    DBL_MATRIX PeakListCollection::mergeAll(double drt, double dmz, double dz, double dint)
    {
        //preprocessing: delete empty PeakLists from c_
        for(unsigned int i = 0; i < c_.size(); i++){
            if(c_[i].size() == 0){
                c_.erase(c_.begin() + i);
            }
        }

        unsigned int oldSize = c_.size();

        // initialization

        //fill correspondenceMap with rt-values
        correspondenceMap_.clear();
        correspondenceMap_.resize(oldSize);

        for(unsigned int i = 0; i < oldSize; i++){
            correspondenceMap_[i].resize(c_[i].size());
            for(unsigned int j = 0; j < c_[i].size(); j++){
                //create map_item to write to correspondenceMap
                map_item tempItem;
                //origin information contains the PeakList index and rt value
                originInformation o;
                o.rt = c_[i][j].getRt();
                o.mz = c_[i][j].getMz();
                o.intensity = c_[i][j].getAbundance();
                o.originPeakList = i;
                o.originPeak = j;
                tempItem.push_back( o );
                correspondenceMap_[i][j] = tempItem;
            }
        }

        //PeakLists are not merged in the beginning --> fill with 1
        plContent_.clear();
        plContent_.resize(oldSize,1);

        //build distance matrix
        DistanceMatrix D = buildDistanceMatrix_(drt,dmz,dz,dint);

        //merge, until there is only one PeakList left

        while(D.size() > 1){
            //find cheapest assignment
            unsigned int merge_lower = D.getMin_LowerIndex();
            unsigned int merge_upper = D.getMin_HigherIndex();

            //merge peaklists into a new one
            mergePeakLists_(merge_lower, merge_upper, c_, drt, dmz, dz, dint);

            //update distance matrix
            updateDistanceMatrix_(D, merge_lower, merge_upper, drt, dmz, dz, dint);
        }

        return calculateRtCorrespondencesFromCorrespondenceMap_(oldSize);

    }



    DBL_MATRIX PeakListCollection::fastMergeAll(double drt, double dmz, double dz, double dint = 0.)
    {
        //preprocessing

        //delete empty PeakLists from c_
        for(unsigned int i = 0; i < c_.size(); i++){
            if(c_[i].size() == 0){
                c_.erase(c_.begin() + i);
            }
        }

        unsigned int oldSize = c_.size();

        // initialization

        //fill correspondenceMap
        correspondenceMap_.clear();
        correspondenceMap_.resize(oldSize);

        for(unsigned int i = 0; i < oldSize; i++){ //go through PeakLists
            correspondenceMap_[i].resize(c_[i].size());
            for(unsigned int j = 0; j < c_[i].size(); j++){ //go through Peaks in PeakList i
                //create map_item to write to correspondenceMap
                map_item tempItem;
                //origin information contains the PeakList index and rt value
                originInformation o;
                o.rt = c_[i][j].getRt();
                o.mz = c_[i][j].getMz();
                o.intensity = c_[i][j].getAbundance();
                o.originPeakList = i;
                o.originPeak = j;
                tempItem.push_back( o );
                correspondenceMap_[i][j] = tempItem;
            }
        }

        //PeakLists are not merged in the beginning --> fill with 1
        plContent_.clear();
        plContent_.resize(oldSize,1);


        //merge until there is only one PeakList left

        while(c_.size() > 1){
            //merge first two peaklists
            unsigned int merge_lower = 0;
            unsigned int merge_upper = 1;

            //merge peaklists into a new one
            mergePeakLists_(merge_lower, merge_upper, c_, drt, dmz, dz, dint);
        }

        return calculateRtCorrespondencesFromCorrespondenceMap_(oldSize);

    }



    void PeakListCollection::saveAllPeakLists(std::string folder, std::vector<std::string>& filenames)
    {
        int size = c_.size();
        if(size != (int)filenames.size()){
            MSPP_LOG(logERROR) << "PeakListCollection::saveAllPeakLists(): Please supply as many filenames as PeakLists." << std::endl;
        }
        mspp_precondition(size == (int)filenames.size(), "PeakListCollection::saveAllPeakLists(): Please supply as many filenames as PeakLists.");

        for(int i = 0; i<size; i++){
            //Create file
            std::stringstream filename;
            filename << folder << DELIM << filenames[i];

            std::fstream f (filename.str().c_str(), std::fstream::out | std::fstream::trunc);
            for(unsigned int j=0; j<c_[i].size(); j++){
                //Write Peak
                f << c_[i][j].getMz() << "\t" << c_[i][j].getCharge() << "\t";
                f << c_[i][j].getAbundance() << "\t" << c_[i][j].getRt() << std::endl;
            }
            f.close();
        }
        return;
    }



    void PeakListCollection::createCorrespondenceFile(std::string filename, std::vector<std::string>& plNames)
    {
        unsigned int cmSize = correspondenceMap_.size();
        //correspondenceMap should have only one column left
        if(cmSize != 1){
            MSPP_LOG(logERROR) << "PeakListCollection::createCorrespondenceFile(): correspondenceMap_ has invalid size (size != 1). Call getAlignment() first." << std::endl;
        }
        mspp_precondition(cmSize == 1 , "PeakListCollection::createCorrespondenceFile(): correspondenceMap_ has invalid size (size != 1). Call getAlignment() first.");

        int group = 0;
        std::fstream f (filename.c_str(), std::fstream::out | std::fstream::trunc);


        //write correspondences to file
        for(unsigned int i = 0; i < correspondenceMap_[0].size(); i++){ //go through whole map
            if(correspondenceMap_[0][i].size() >= 1){ //add correspondence if available
                for(unsigned int j = 0; j < correspondenceMap_[0][i].size(); j++){
                    f << group << "\t"; //group index
                    if(plNames.size()!=0){
                        f << plNames[correspondenceMap_[0][i][j].originPeakList] << "\t";
                    }else{
                        f << correspondenceMap_[0][i][j].originPeakList << "\t"; //origin information
                    }
                    f << correspondenceMap_[0][i][j].originPeak << "\t"; //origin information
                    f.precision(10);
                    f << correspondenceMap_[0][i][j].mz << "\t"; //mass over charge
                    f.precision(5);
                    f << correspondenceMap_[0][i][j].rt << std::endl; //retention time
                }
                group++;
            }
        }
        f.close();

        return;
    }



    //convert correspondenceMap into meaningful output
    DBL_MATRIX PeakListCollection::calculateRtCorrespondencesFromCorrespondenceMap_(unsigned int oldSize)
    {
        unsigned int cmSize = correspondenceMap_.size();

        //correspondenceMap should have only one column left
        if(cmSize != 1){
            MSPP_LOG(logERROR) << "PeakListCollection::calculateRtCorrespondencesFromCorrespondenceMap_(): correspondenceMap_ has invalid size (size != 1)." << std::endl;
        }
        mspp_precondition(cmSize == 1 , "PeakListCollection::calculateRtCorrespondencesFromCorrespondenceMap_(): correspondenceMap_ has invalid size (size != 1).");

        //find out how many correspondences we have
        unsigned int num_correspond = 0;
        for(unsigned int i = 0; i < correspondenceMap_[0].size(); i++){
            if(correspondenceMap_[0][i].size() > 1){
                num_correspond++;
            }
        }

        DBL_MATRIX RTC (num_correspond,oldSize,-1.);  //empty rt-correspondences

        //fill correspondences table, all positions without match remain -1.
        unsigned int current_correspond = 0;
        for(unsigned int i = 0; i < correspondenceMap_[0].size(); i++){ //go through whole map
            if(correspondenceMap_[0][i].size() > 1){ //add correspondence if available
                for(unsigned int j = 0; j < correspondenceMap_[0][i].size(); j++){
                    RTC(current_correspond,correspondenceMap_[0][i][j].originPeakList) = correspondenceMap_[0][i][j].rt;
                }
                current_correspond++; //keep track of the number of processed correspondences
            }
        }

        return RTC;
    }



    //build a distance matrix out of all PeakLists
    DistanceMatrix PeakListCollection::buildDistanceMatrix_(double drt, double dmz, double dz, double dint = 0.)
    {
        //number of Peaklists
        unsigned int numPl = c_.size();

        DistanceMatrix DM;
        //Add new elements to the Distance Matrix
        for(unsigned int i = 0; i<numPl; i++){
            DM.addElement();
            //Calculate distance to the other elements / the cost of merging them
            for(unsigned int j = 0; j<i; j++){
                StableMarriage sm(c_[i],c_[j],drt,dmz,dz,dint);
                sm.setLimit(1.);
                DM(i,j) = sm.getCost();
            }
        }

        return DM;
    }



    //Update the Distance Matrix
    void PeakListCollection::updateDistanceMatrix_(DistanceMatrix& D, unsigned int merged_lower, unsigned int merged_upper,
                                                   double drt, double dmz, double dz, double dint = 0.)
    {

        //remove distances of merged PeakLists from distance matrix
        D.deleteElement(merged_upper);
        D.deleteElement(merged_lower);

        //add a new column for the merged PeakList
        D.addElement();

        //fill distances
        unsigned int sz = D.size();
        for(unsigned int i = 0; i<sz-1; i++){
            StableMarriage sm(c_[i],c_[sz-1],drt,dmz,dz,dint);
            sm.setLimit(1.);
            D(i,sz-1) = sm.getCost();
        }

        return;
    }



    //Merge Peaklists merge_lower and merge_upper
    void PeakListCollection::mergePeakLists_(unsigned int merge_lower, unsigned int merge_upper, std::vector<PeakList>& c,
                                             double drt, double dmz, double dz, double dint = 0.)
    {

        PeakList newPeakList; //Peaklists will be merged into newPeakList
        std::vector<map_item> newCorrespondenceMapColumn; //correspondenceMap information will be merged in here

        //get assignments via StableMarriage
        StableMarriage sm(c[merge_lower], c[merge_upper], drt, dmz, dz, dint);

        //set limit: do not assign Peaks with distance larger than 1. Note: normalization constants
        //drt and dmz must be adjusted properly.
        sm.setLimit(1.);

        StableMarriage::Assignment assign = sm.getAssignment();

        //process peaks with a partner
        for(unsigned int i = 0; i < assign.ass12.size(); i++){
            if(assign.ass12[i] > -1){
                //write matched Peaks into newPeaklist

                //calculate average Rt, Mz and Intensity values
                //Peak1: PL: merge_lower ; Item: i ; plContent: correspondenceMap_[merge_lower][i].size()
                //Peak2: PL: merge_upper ; Item: assign.ass12[i] ; plContent: correspondenceMap_[merge_upper][assign.ass12[i]].size()

                //Number of peaks merged into single peak = size of originInformation vector
                double plc_lower = correspondenceMap_[merge_lower][i].size();
                double plc_upper = correspondenceMap_[merge_upper][assign.ass12[i]].size();

                double avRt = (c[merge_lower][i].getRt()*plc_lower +
                               c[merge_upper][assign.ass12[i]].getRt()*plc_upper) /
                              (plc_lower + plc_upper);
                double avMz = (c[merge_lower][i].getMz()*plc_lower +
                               c[merge_upper][assign.ass12[i]].getMz()*plc_upper) /
                              (plc_lower + plc_upper);
                double avInt = (c[merge_lower][i].getAbundance()*plc_lower +
                                c[merge_upper][assign.ass12[i]].getAbundance()*plc_upper) /
                               (plc_lower + plc_upper);

                //create temporary peak and fill with averaged values
                Peak temp;
                temp.setCharge(c[merge_lower][i].getCharge()); //charge is assumed to be equal
                temp.setRt(avRt);
                temp.setMz(avMz);
                temp.setAbundance(avInt);

                //add temporary peak to peaklist
                newPeakList.push_back(temp);


                //write origin information of matched peaks

                map_item tempItem;

                //copy all origin information from merge_lower
                for(unsigned int j = 0; j < correspondenceMap_[merge_lower][i].size(); j++){
                    tempItem.push_back(correspondenceMap_[merge_lower][i][j]);
                }

                //copy all origin information from merge_upper
                for(unsigned int j = 0; j < correspondenceMap_[merge_upper][assign.ass12[i]].size(); j++){
                    tempItem.push_back(correspondenceMap_[merge_upper][assign.ass12[i]][j]);
                }

                //add item to column
                newCorrespondenceMapColumn.push_back(tempItem);

            }
        }

        //process PeakList merge_lower
        for(unsigned int i = 0; i < assign.ass12.size(); i++){
            if(assign.ass12[i] == -1){

                newPeakList.push_back(c[merge_lower][i]);

                map_item tempItem;
                //copy all origin information
                for(unsigned int j = 0; j < correspondenceMap_[merge_lower][i].size(); j++){
                    tempItem.push_back(correspondenceMap_[merge_lower][i][j]);
                }

                newCorrespondenceMapColumn.push_back(tempItem);

            }
        }

        //process PeakList merge_upper
        for(unsigned int i = 0; i < assign.ass21.size(); i++){
            if(assign.ass21[i] == -1){

                newPeakList.push_back(c[merge_upper][i]);

                map_item tempItem;
                //copy all origin information
                for(unsigned int j = 0; j < correspondenceMap_[merge_upper][i].size(); j++){
                    tempItem.push_back(correspondenceMap_[merge_upper][i][j]);
                }
                newCorrespondenceMapColumn.push_back(tempItem);

            }
        }

        //refresh correspondence map

        //erase merged columns
        correspondenceMap_.erase(correspondenceMap_.begin()+merge_upper);
        correspondenceMap_.erase(correspondenceMap_.begin()+merge_lower);

        //add new column
        correspondenceMap_.push_back(newCorrespondenceMapColumn);


        //refresh peaklist collection c

        //erase merged peaklists
        c.erase(c.begin()+merge_upper);
        c.erase(c.begin()+merge_lower);

        //add new peaklist
        c.push_back(newPeakList);

        //update plContent_
        plContent_.push_back(plContent_[merge_lower] + plContent_[merge_upper]);
        plContent_.erase(plContent_.begin()+merge_upper);
        plContent_.erase(plContent_.begin()+merge_lower);



        return;
    }



} /*namespace ms*/
