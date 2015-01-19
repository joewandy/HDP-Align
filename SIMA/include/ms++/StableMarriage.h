/*$Id:$*/

/*
 * StableMarriage.h
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


#ifndef __STABLEMARRIAGE_H__
#define __STABLEMARRIAGE_H__

#include <vector>
#include <algorithm>
#include <functional>

#include <ms++/config.h>
#include <ms++/Error.h>
#include <ms++/Log.h>


#include <ms++/PeakList.h>
#include <ms++/MahalanobisDistance.h>

// for friend declaration further down
struct StableMarriageTest;

namespace ms
{

/**
 * Assigns elements of two PeakLists pairwise.
 *
 * @author Martin Lindner <martin.lindner@iwr.uni-heidelberg.de>
 * @date 2009-07-23
 * This class matches the elements of two peak lists to pairs according to their Mz value.
 * The matching algorithm is a modified Stable Marriage algorithm as implemented by Bjoern Voss
 * in his diploma works.
 */
class MSPP_EXPORT StableMarriage
{
public:
    //declare as friend to allow testing
    friend struct ::StableMarriageTest;

    /**
     * The result of the matching is called an Assignment. An Assignment consists of two vectors,
     * one for each PeakList, containing the index of the assigned partner in the other list.
     * Unassigned Peaks get the assignment -1 .
     * The value cost is a measure for the Quality of the assignment.
     */
    struct Assignment { 
        std::vector<int> ass12;
        std::vector<int> ass21; 
        double cost;
    };

    struct RtPair{
        double Rt1;
        double Rt2;
    };

    /**
     * RtAssignment contians the Rt values of assigned peaks in ascending order 
     */
    typedef std::vector<RtPair> RtAssignment;

    //constructors/destructor
    StableMarriage(PeakList& PL1, PeakList& PL2, double drt, double dmz, double dz, double dint ):
      upToDate_(false), cost_(0.), pl1_(PL1), pl2_(PL2), drt_(drt), dmz_(dmz), dz_(dz), dint_(dint), limit_(0.) {}
    StableMarriage(PeakList& PL, double drt, double dmz, double dz, double dint ):
      upToDate_(false), cost_(0.), pl1_(PL), pl2_(PL), drt_(drt), dmz_(dmz), dz_(dz), dint_(dint), limit_(0.) {}

    /**
     * Set a limit: do not match peaks with distance larger than limit.
     */
    void setLimit(double limit) {limit_ = limit; upToDate_ = false; }

    /**
     * Set drt value for Mahalanobis distance calculation.
     */
    void setDrt(double drt) {drt_ = drt; upToDate_ = false; }

    /**
     * Set dmz value for Mahalanobis distance calculation.
     */
    void setDmz(double dmz) {dmz_ = dmz; upToDate_ = false; }

    /**
     * Set dint value (intensity threshold) for Mahalanobis distance calculation.
     */
    void setDint(double dint) {dint_ = dint; upToDate_ = false; }

    /**
     * Set the Peaklist used for the assignemnt.
     */
    void setPeakList1(PeakList& PL){ pl1_ = PL; upToDate_ = false; }

    /**
     * Set the Peaklist used for the assignment.
     */
    void setPeakList2(PeakList& PL){ pl2_ = PL; upToDate_ = false; }

    /**
     * The assignment vectors can be recieved by calling getAssignment().
     */
    Assignment getAssignment();

    /**
     * The list with Rt values of the matched peaks can be recieved by calling getRtAssignment().
     */
    RtAssignment getRtAssignment();

    /**
     * Get the cost of the assignment. The cost is a measure for the quality of an assignment.
     */
    double getCost();

   

private:
    //Entry of a preference list;
    struct PrefListEntry {
        int index;          //Index of partner peak
        double distance;    //Distance to partner peak
    };
    //Preference list type
    typedef std::vector<std::vector<StableMarriage::PrefListEntry> > PrefList;

    //compare two PrefListEntries by their distance
    struct PrefList_LessThanDistance:
        std::binary_function<bool, StableMarriage::PrefListEntry, StableMarriage::PrefListEntry> {
        bool operator()(const StableMarriage::PrefListEntry& lhs, const StableMarriage::PrefListEntry& rhs) {
            return lhs.distance < rhs.distance;
        }
    };
    
    //compare two RtPair structs by their Rt1 value 
    struct RtPair_LessThanRt1:
        std::binary_function<bool, StableMarriage::RtPair, StableMarriage::RtPair> {
        bool operator()(const StableMarriage::RtPair& lhs, const StableMarriage::RtPair& rhs) {
            return lhs.Rt1 < rhs.Rt1;
        }
    };

    //implementation of Stable Marriage algorithm by Bjoern Voss
    void stableMarriage_();

    //calculation of preference lists for given peak lists
    void calculatePreferenceLists_();

    //ugly modification of STL sort algorithm, which returns a vector containing the sorting order
    std::vector<int> sortPeakList_(PeakList& inputPL, PeakList& sortedPL);

    //recalculate the assignment only if it is nescessary
    void recalculateIfNescessary();

    bool upToDate_;     //is the stable marriage already calculated

    //Preference lists are stored here
    PrefList pref12_;   
    PrefList pref21_;

    //vectors containing assignments (result of stable marriage)
    std::vector<int> assignment12_;
    std::vector<int> assignment21_;
    double cost_;

    //reference to peak lists to be matched
    PeakList& pl1_;
    PeakList& pl2_;

    //Mahalanobis distance parameters
    double drt_;    // rt normalization parameter
    double dmz_;    // mz normalization parameter
    double dz_;     // charge parameter (dz_ = 0 means: compare only peaks with equal charge)
    double dint_;   // intensity cutoff
    double limit_;

};

} /*namespace ms*/

#endif /*__STABLEMARRIAGE_H__*/
