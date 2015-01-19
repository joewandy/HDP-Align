/*$Id$*/

/*
 * PeakListCollection-test.cpp
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

#include <ms++/PeakListCollection.h>
#include <tests/unittest.hxx>


using namespace ms;

const double TOL = 1e-12;
#define myRandom double(rand())/double(RAND_MAX)

//setup a PeakListCollection with PeakLists of different length
PeakListCollection SamplePeakListCollection()
{
    PeakListCollection PLC;

    PeakList PL0;
    for(unsigned int i = 100; i<200; i++){
        Peak P (double(i),1,1.,double(i+100));
        PL0.push_back(P);
    }
    PLC.push_back(PL0);

    PeakList PL1;
    for(unsigned int i = 50; i<150; i++){
        Peak P (double(i)+0.01,1,1.,double(i+70));
        PL1.push_back(P);
    }
    PLC.push_back(PL1);
    
    PeakList PL2;
    for(unsigned int i = 70; i<110; i++){
        Peak P (double(i)+0.1,1,1.,double(i+130));
        PL2.push_back(P);
    }
    PLC.push_back(PL2);

    PeakList PL3;
    for(unsigned int i = 100; i<210; i++){
        Peak P (double(i)-0.2,1,1.,double(i+150));
        PL3.push_back(P);
    }
    PLC.push_back(PL3);
    
    PeakList PL4;
    for(unsigned int i = 250; i<280; i++){
        Peak P (double(i),1,1.,double(i+100));
        PL4.push_back(P);
    }
    PLC.push_back(PL4);

    return PLC;
}

struct PeakListCollectionTest {
    void testBuildDistanceMatrix() 
	{

        PeakListCollection PLC = SamplePeakListCollection();

        DistanceMatrix DM = PLC.buildDistanceMatrix_(200., 0.5, 0, 0.);

        //PeakList 4 should have no Peaks in common with the other lists --> Cost == DBL_MAX
        for(unsigned int i = 0; i < DM.size()-1; i++){
            shouldEqual(DM(i,4),DBL_MAX);
        }

        //distance PL0 - PL2 should be sqrt(0.1^2/0.5^2 + 30^2/200^2) = 0.25
        shouldEqualTolerance(DM(0,2),0.25,TOL);
        shouldEqualTolerance(DM(2,0),0.25,TOL);

        return;
    }

    void testUpdateDistanceMatrix() 
	{

        PeakListCollection PLC = SamplePeakListCollection();

        DistanceMatrix DM = PLC.buildDistanceMatrix_(200., 0.5, 0, 0.);
        DistanceMatrix Ref = PLC.buildDistanceMatrix_(200., 0.5, 0, 0.);

        DM.addElement(99.); //Add a dummy element, we will delete it again

        unsigned int merged_lower = 4; //last two elements
        unsigned int merged_upper = 5;

        PLC.plContent_.clear();
        PLC.plContent_.resize(6,1);

        PLC.updateDistanceMatrix_(DM, merged_lower, merged_upper, 200., 0.5, 0, 0.);

        //Size should still be 5
        shouldEqual((int)DM.size(),5);

        //Matrix should be the same as the original matrix Ref
        for(unsigned int i = 0; i < 5; i++){
            for(unsigned int j = 0; j < i; j++){
                shouldEqualTolerance(DM(i,j),Ref(i,j),TOL);
            }
        }

        return;
    }

    void testMergePeakLists()
    {
     

        //prepare a setup

        PeakListCollection PLC = SamplePeakListCollection();

        //fill correspondenceMap with rt-values
        PLC.correspondenceMap_.clear();
        PLC.correspondenceMap_.resize(5);
        for(unsigned int i = 0; i < 5; i++){
            PLC.correspondenceMap_[i].resize(PLC.c_[i].size());
            for(unsigned int j = 0; j < PLC.c_[i].size(); j++){
                //create map_item to write to correspondenceMap
                PeakListCollection::map_item tempItem;
                //origin information contains the PeakList index and rt value
                PeakListCollection::originInformation o = {PLC.c_[i][j].getRt() , i};
                tempItem.push_back( o );
                PLC.correspondenceMap_[i][j] = tempItem;
            }
        }

        //PeakLists are not merged in the beginning --> fill with 1
        PLC.plContent_.clear();
        PLC.plContent_.resize(5,1);

        //merge peaklists 0 and 1

        PLC.mergePeakLists_(0,1,PLC.c_,200.,0.5,0,0.);

        //PLC should contain 4 PeakLists
        shouldEqual((int)PLC.size(), 4);

        //merged PeakList PLC[3] should contain 150 Peaks
        shouldEqual((int)PLC[3].size(), 150);

        //first merged Peak should have mz = (100+100.01)/2 = 100.005 and rt = (200+170)/2 = 185
        shouldEqualTolerance(PLC[3][0].getMz(),100.005,TOL);
        shouldEqualTolerance(PLC[3][0].getRt(),185,TOL);

        //Peak [50] should be untouched from PL0, mz = 150, rt = 250
        shouldEqualTolerance(PLC[3][50].getMz(),150,TOL);
        shouldEqualTolerance(PLC[3][50].getRt(),250,TOL);
        
        //Peak [100] should be untouched from PL1, mz = 50.01, rt = 120
        shouldEqualTolerance(PLC[3][50].getMz(),150,TOL);
        shouldEqualTolerance(PLC[3][50].getRt(),250,TOL);

        //correspondenceMap_ should have information for 4 Peaklists
        shouldEqual((int)PLC.correspondenceMap_.size(),4);

        //correspondenceMap_[3] should contain information for 150 peaks
        shouldEqual((int)PLC.correspondenceMap_[3].size(),150);

        //Peaks 0..49 should have origin information from 2 Peaks, 50..149 from 1
        for(unsigned int i = 0; i < 50; i++){
            shouldEqual((int)PLC.correspondenceMap_[3][i].size(),2);
            shouldEqual(PLC.correspondenceMap_[3][i][0].originPeakList,0);
            shouldEqual(PLC.correspondenceMap_[3][i][1].originPeakList,1);
            shouldEqualTolerance(PLC.correspondenceMap_[3][i][0].rt,double(i)+200.,TOL);
        }
        for(unsigned int i = 50; i < 150; i++){
            shouldEqual((int)PLC.correspondenceMap_[3][i].size(),1);
        }

        //test plContent_
        shouldEqual((int)PLC.plContent_[0],1);
        shouldEqual((int)PLC.plContent_[1],1);
        shouldEqual((int)PLC.plContent_[2],1);
        shouldEqual((int)PLC.plContent_[3],2);

        return;
    }

    void testCalculateRtCorrespondencesFromCorrespondenceMap() 
	{


        //test setup
        PeakListCollection PLC = SamplePeakListCollection();

        unsigned int oldSize = 3;
        PLC.correspondenceMap_.clear();
        PLC.correspondenceMap_.resize(1);

        PeakListCollection::map_item temp;
        PeakListCollection::originInformation entry;

        entry.rt = 100.;
        entry.originPeakList = 0;
        temp.push_back(entry);

        entry.rt = 101.;
        entry.originPeakList = 1;
        temp.push_back(entry);
                
        entry.rt = 102.;
        entry.originPeakList = 2;
        temp.push_back(entry);

        PLC.correspondenceMap_[0].push_back(temp);
        temp.clear();

        entry.rt = 150.;
        entry.originPeakList = 1;
        temp.push_back(entry);

        entry.rt = 151.;
        entry.originPeakList = 2;
        temp.push_back(entry);

        PLC.correspondenceMap_[0].push_back(temp);
        temp.clear();

        entry.rt = 30.;
        entry.originPeakList = 0;
        temp.push_back(entry);

        entry.rt = 31.;
        entry.originPeakList = 1;
        temp.push_back(entry);
                
        entry.rt = 32.;
        entry.originPeakList = 2;
        temp.push_back(entry);

        PLC.correspondenceMap_[0].push_back(temp);
        temp.clear();

        entry.rt = 1010.;
        entry.originPeakList = 2;
        temp.push_back(entry);

        entry.rt = 1011.;
        entry.originPeakList = 0;
        temp.push_back(entry);
                
        entry.rt = 1010.;
        entry.originPeakList = 1;
        temp.push_back(entry);

        PLC.correspondenceMap_[0].push_back(temp);
        temp.clear();

        entry.rt = 22.;
        entry.originPeakList = 0;
        temp.push_back(entry);

        PLC.correspondenceMap_[0].push_back(temp);
        temp.clear();

        entry.rt = 23.;
        entry.originPeakList = 1;
        temp.push_back(entry);

        PLC.correspondenceMap_[0].push_back(temp);
        temp.clear();

        entry.rt = 66.;
        entry.originPeakList = 2;
        temp.push_back(entry);

        entry.rt = 67.;
        entry.originPeakList = 0;
        temp.push_back(entry);

        PLC.correspondenceMap_[0].push_back(temp);
        temp.clear();

        entry.rt = 5.;
        entry.originPeakList = 1;
        temp.push_back(entry);

        entry.rt = 8.;
        entry.originPeakList = 0;
        temp.push_back(entry);

        PLC.correspondenceMap_[0].push_back(temp);

        //calculate rtCorrespondences
        DBL_MATRIX RTC = PLC.calculateRtCorrespondencesFromCorrespondenceMap_(oldSize);


        //test if rtCorrespondences are correct
        
        //size check
        shouldEqual(RTC.rowCount(),6);
        for(unsigned int i = 0; i < 6; i++){
            shouldEqual(RTC.columnCount(), 3);
        }

        //check entries
        shouldEqualTolerance(RTC(0,0),100.,TOL);
        shouldEqualTolerance(RTC(1,0),-1.,TOL);
        shouldEqualTolerance(RTC(3,0),1011.,TOL);
        shouldEqualTolerance(RTC(4,0),67.,TOL);
        shouldEqualTolerance(RTC(5,0),8.,TOL);

        shouldEqualTolerance(RTC(0,1),101.,TOL);
        shouldEqualTolerance(RTC(1,1),150.,TOL);
        shouldEqualTolerance(RTC(2,1),31.,TOL);
        shouldEqualTolerance(RTC(3,1),1010.,TOL);
        shouldEqualTolerance(RTC(4,1),-1.,TOL);

        shouldEqualTolerance(RTC(1,2),151.,TOL);
        shouldEqualTolerance(RTC(2,2),32.,TOL);
        shouldEqualTolerance(RTC(4,2),66.,TOL);
        shouldEqualTolerance(RTC(5,2),-1,TOL);




        return;
    }


    void testMergeAll() 
	{
 
        //test setup
        PeakListCollection PLC = SamplePeakListCollection();

        PLC.mergeAll(200., 0.5, 0, 0.);

        //there should only be one Peaklist left
        shouldEqual((int)PLC.c_.size(),1);

        //this Peaklist should contain 190 Peaks (with setup SamplePeakListCollection())
        shouldEqual((int)PLC.c_[0].size(),190);

        //test single peaks
        shouldEqual(PLC.c_[0][0].getMz(),250.); //first Peak from PeakList 4
        shouldEqual((int)PLC.correspondenceMap_[0][0].size(),1);            
        shouldEqual(PLC.correspondenceMap_[0][0][0].originPeakList,4);
        
        shouldEqual(PLC.c_[0][30].getMz(),99.9775); // Peak with Mz 100 averaged over 4 matched Peaks
        shouldEqual((int)PLC.correspondenceMap_[0][30].size(),4);  
        shouldEqual(PLC.correspondenceMap_[0][30][2].originPeakList,0);
        shouldEqual(PLC.correspondenceMap_[0][30][2].rt,200.);
        shouldEqual(PLC.correspondenceMap_[0][30][3].originPeakList,1);
        shouldEqual(PLC.correspondenceMap_[0][30][3].rt,170.);

        return;
    }

    void testFastMergeAll() 
	{
        //test setup
        PeakListCollection PLC = SamplePeakListCollection();

        PLC.fastMergeAll(200., 0.5, 0, 0.);

        //there should only be one Peaklist left
        shouldEqual((int)PLC.c_.size(),1);

        //this Peaklist should contain 190 Peaks (in setup SamplePeakListCollection())
        shouldEqual((int)PLC.c_[0].size(),190);

        return;
    }

    void testApplyMTS() 
	{
        PeakListCollection PLC;

        //setup peaklistcollection
        for(int j=0; j<5; j++){
            PeakList PL;
            for(int i=0; i<5; i++){
                Peak P(100.+double(i)+myRandom*0.1, 1, 1., 100.+double(i)+myRandom*0.1);
                PL.push_back(P);
            }
            PLC.push_back(PL);
        }

        //calculate correspondences
        DBL_MATRIX al = PLC.getAlignment(0.3,0.3,0,0.,PeakListCollection::NORMAL,0);
        //each peak has a partner in the other lists (by construction)
       
        //set up fictional Master Time Scale
        DBL_MATRIX MTS (2,5);
        for(int i=0; i<5; i++){
            MTS(0,i) = 50.;
            MTS(1,i) = 150.;
        }

        MTS(1,4) = 106.;

        //correct by Master Time Scale
        PLC.applyMasterTimeScale(MTS);

        DBL_MATRIX new_al = PLC.getAlignment(0.3,0.3,0,0.,PeakListCollection::NORMAL,0);

        //Since MTS is the bisectional line for the first 4 components and has
        //much smaller slope for the last component, the Peaks of the last
        //PeakList should not be aligned again:

        shouldEqual(new_al(0,4),-1);
        shouldEqual(new_al(1,4),-1);
        shouldEqual(new_al(2,4),-1);
        shouldEqual(new_al(3,4),-1);
        shouldEqual(new_al(4,4),-1);

        //the other Peaks should still be at nearly equal values
        shouldEqualTolerance(new_al(0,0),new_al(0,1),0.1);
        shouldEqualTolerance(new_al(3,0),new_al(3,1),0.1);
        shouldEqualTolerance(new_al(2,2),new_al(2,3),0.1);
        shouldEqualTolerance(new_al(4,0),new_al(4,3),0.1);
        shouldEqualTolerance(new_al(1,0),new_al(1,2),0.1);

        return;
    }

};

struct PeakListCollectionTestSuite : public vigra::test_suite {
    PeakListCollectionTestSuite() : vigra::test_suite("PeakListCollection Tests") {
        add( testCase(&PeakListCollectionTest::testBuildDistanceMatrix));
        add( testCase(&PeakListCollectionTest::testUpdateDistanceMatrix));
        add( testCase(&PeakListCollectionTest::testMergePeakLists));
        add( testCase(&PeakListCollectionTest::testCalculateRtCorrespondencesFromCorrespondenceMap));
        add( testCase(&PeakListCollectionTest::testMergeAll));
        add( testCase(&PeakListCollectionTest::testFastMergeAll));
        add( testCase(&PeakListCollectionTest::testApplyMTS));
    }
};

int main()
{
    PeakListCollectionTestSuite test;
    int failed = test.run();
    std::cout << test.report() << std::endl;
    return failed;
}
