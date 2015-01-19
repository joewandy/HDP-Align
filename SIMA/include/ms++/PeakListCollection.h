/*$Id$*/

/*
 * PeakListCollection.h
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

#ifndef __PEAKLISTCOLLECTION_H__
#define __PEAKLISTCOLLECTION_H__

#include <vector>
#include <iostream>
#include <fstream>
#include <string>
#include <sstream>

#include <ms++/config.h>
#include <ms++/Error.h>
#include <vigra/matrix.hxx>

#include <ms++/Collection.h>
#include <ms++/PeakList.h>
#include <ms++/StableMarriage.h>
#include <ms++/DistanceMatrix.h>



// for friend declaration further down
struct PeakListCollectionTest;

namespace ms
{

typedef vigra::linalg::Matrix<double> DBL_MATRIX;


/** A collection of PeakList objects
 *
 * This class stores and manages a collection of
 * PeakList objects. One major task is the merging of Peaklists,
 * this is done by the StableMarriage algorithm.
 */
class MSPP_EXPORT PeakListCollection : public Collection<PeakList>
{
public:
    friend struct ::PeakListCollectionTest;

    //acceleration flags used for the calculation of the Alignment
    enum AccelerationFlag {
        NORMAL,
        FAST,
        REFERENCE
    };

    /** Default constructor. PeakListCollection contains no PeakLists
     */
    PeakListCollection() : Collection<PeakList>() {}


    /** Constructor using a sequence of PeakLists.
     *  @param begin Iterator pointing at the beginning of a sequence of PeakList objects.
     *  @param end Iterator pointing at the end of the sequence.
     *
     *  This constructs a PeakListCollection from PeakList
     *  objects in the range [begin, end).
     */
    PeakListCollection(
        Collection<PeakList>::const_iterator begin,
        Collection<PeakList>::const_iterator end)
            : Collection<PeakList>(begin, end) {}


    /** Assignment operator. Assign one PeakListCollection object to another.
     *  @param rhs Right-hand-side PeakListCollection object that will be assigned
     *             to (*this).
     */
    PeakListCollection& operator=(const PeakListCollection rhs) {
        c_ = rhs.c_;
        return *this;
    }

    
    /** Clear PeakListCollection: remove all PeakLists
     */
    void clear();


    /** Add a PeakList stored in "filename"
     */
    void addPeakList(const std::string& filename);


    /** Merge all Peak Lists with Stable Marriage algorithm
     *  @return A PeakListCollection::DBL_MATRIX object.
     *  @param drt Normalization parameter for Mahalanobis distance in Rt space.
     *  @param dmz Normalization parameter for Mahalanobis distance in M/z space.
     *  @param dz Normalization parameter for Mahalanobis distance for charge states.
     *  @param dint Intensity threshold for for small Peaks.
     *
     * This function merges all contained PeakList objects by finding
     * correspondent Peaks via the Stable Marriage algorithm. As result,
     * a list of the corresponding retention times is produced and stored
     * instead of the original PeakLists.
     */
    DBL_MATRIX mergeAll(double drt, double dmz, double dz, double dint);


    /** Merge all Peak Lists with Stable Marriage algorithm (fast)
     *  @return A PeakListCollection::DBL_MATRIX object.
     *  @param drt Normalization parameter for Mahalanobis distance in Rt space.
     *  @param dmz Normalization parameter for Mahalanobis distance in M/z space.
     *  @param dz Normalization parameter for Mahalanobis distance for charge states.
     *  @param dint Intensity threshold for for small Peaks.
     *
     * fastMergeAll() merges all contained PeakList objects as mergeAll(), but with
     * the difference that not the best merge order is determined. This saves a lot
     * computation time, but may deliver worse results.
     */
    DBL_MATRIX fastMergeAll(double drt, double dmz, double dz, double dint);


    /** Align all PeakLists via Stable Marriage algorithm (faster version)
     *  @return A DBL_MATRIX object.
     *  @param drt Normalization parameter for Mahalanobis distance in Rt space.
     *  @param dmz Normalization parameter for Mahalanobis distance in M/z space.
     *  @param dz Normalization parameter for Mahalanobis distance for charge states.
     *  @param dint Intensity threshold for for small Peaks.
     *  @param flag Acceleration type.
     *  @param param If flag is set to REFERENCE, param specifies the reference PeakList.
     *
     * getAlignment() aligns all contained PeakList objects using the Stable Marriage
     * algorithm. Contrary to mergeAll(), the original order and content of the PeakLists
     * is not changed. So, the contained PeakLists can still be corrected by a Master
     * Time Scale. The flag specifies the acceleration type, there are three possibilities:
     * NORMAL: The optimum merging order is determined before every step. This might be slow
     * for large PeakLists and high dimensionality!
     * FAST: Always the last two spectra are merged. The computational effort is proportional to the
     * number of PeakLists (dimension).
     * REFERENCE: Always the spectrum specified by param and the last one are merged. This is as fast
     * as FAST.
     */
    DBL_MATRIX getAlignment(double drt, double dmz, double dz, double dint, AccelerationFlag flag, int param);


    /** Correct all PeakList's Rt-values by a Master Time Scale 
     *  @param mts DBL_MATRIX holding the Master Time Scale
     *
     * This function applies a Master Time Scale mts to the contained
     * PeakList objects. Usually, the Master Time Scale is computed by
     * the Alignment object, that belongs to this PeakListCollection.
     */
    void applyMasterTimeScale(DBL_MATRIX mts);


    /** save all PeakLists in CSV format to one folder
     *  @param folder Folder to store the files
     *  @param filenames Filenames of the PeakLists
     *
     * The PeakLists are saved separately in CSV files using TAB as delimiter.
     * Format: m/z TAB charge TAB intensity TAB retention time RETURN
     */
    void saveAllPeakLists(std::string folder, std::vector<std::string>& filenames);


    /** Create human readable file containing assignment lists with origin information
     *  @param filename Path to store the file
     *  @param plNames Filenames of the PeakLists
     *
     * The file can only be created AFTER the alignment is calculated! Call
     * getAlignment() or similar method first.
     */
    void createCorrespondenceFile(std::string filename, std::vector<std::string>& plNames);


private:
    //the correspondence map contains information about the origin of each peak:
    // - original rt values
    // - original PeakList number
    // - index in original PeakList
    // - original mz value
    // - original intensity
    struct originInformation {
        double rt;
        int originPeakList;
        int originPeak;
        double mz;
        double intensity;
    };
    typedef std::vector<originInformation> map_item;
    std::vector< std::vector<map_item> > correspondenceMap_;


    //counts how many peaklists are merged into one
    std::vector<unsigned int> plContent_;


    //build initial distance matrix
    DistanceMatrix buildDistanceMatrix_(double drt, double dmz, double dz, double dint);


    //updates the distance matrix and the plContent vector after merging
    void updateDistanceMatrix_( DistanceMatrix& D, unsigned int merged_lower, unsigned int merged_upper, 
                                double drt, double dmz, double dz, double dint);

    
    //merges specified Peaklists
    void mergePeakLists_(unsigned int merge_lower, unsigned int merge_upper, std::vector<PeakList>& c,
                           double drt, double dmz, double dz, double dint);


    //convert correspondenceMap into meaningful output
    DBL_MATRIX calculateRtCorrespondencesFromCorrespondenceMap_(unsigned int size);

};





} /*namespace ms*/

#endif

