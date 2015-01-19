/*$Id$*/

/*
 * stablemarriage-test.cpp
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


#include <ms++/StableMarriage.h>

#include <ms++/PeakList.h>


#include <tests/unittest.hxx>

using namespace ms;


#define myRandom double(rand())/double(RAND_MAX)

struct StableMarriageTest {
    void testStableMarriage() 
	{
        // correct assignment test

        double Range = 1000.;
        double variance_Rt = 1.;
        double variance_Mz = 0.1;
        int length = 100;

        PeakList A;
        PeakList B;

 
        
        //fill peak lists
        for(int i=0; i<length; i++){
            Peak temp;
            temp.setRt(myRandom*Range);
            temp.setMz(myRandom*Range);
            temp.setCharge(1);
            temp.setAbundance(1.);

            A.push_back(temp);
            

            temp.setRt(temp.getRt() + myRandom*variance_Rt);
            temp.setMz(temp.getMz() + myRandom*variance_Mz);
            //Every Peak in PL1 is only slightly different from corresponding peak in PL0 --> every Peak should have a partner
            B.push_back(temp);
        }
        
        StableMarriage SM(A,B,10.*variance_Rt,10.*variance_Mz,0,0);
        StableMarriage::Assignment assign = SM.getAssignment();

        //are assignments made correctly
        for(int i = 0; i < length; i++){
            shouldEqual(assign.ass12[i],i);
            shouldEqual(assign.ass21[i],i);
        }
        

        // symmetry test (the distance-weighted marriage is symmetric)
        length = 100;
        A.clear();
        B.clear();
        A.resize(length);
        B.resize(length);
        //fill peak lists
        for(int i=0; i<length; i++){
            Peak temp;
            temp.setRt(myRandom*Range);
            temp.setMz(myRandom*Range);
            temp.setCharge(1);
            temp.setAbundance(1.);

            A[i]=temp;

            temp.setRt(temp.getRt() + myRandom*variance_Rt);
            temp.setMz(temp.getMz() + myRandom*variance_Mz);
            //Every Peak in PL1 is only slightly different from corresponding peak in PL0 --> every Peak should have a partner
            B[i]=temp;  
        }
        //"shuffle" Peaklist 1 by sorting it by its Mz values
        sort(B.begin(),B.end(),PeakList::LessThanMz<Peak,Peak>());

        StableMarriage SM1(A,B,10.*variance_Rt,10.*variance_Mz,0,0);
        StableMarriage SM2(B,A,10.*variance_Rt,10.*variance_Mz,0,0);

        StableMarriage::Assignment assignment1 = SM1.getAssignment();
        StableMarriage::Assignment assignment2 = SM2.getAssignment();

        //test for symmetry
        for(int i = 0; i < length; i++){
            shouldEqual(assignment1.ass12[i],assignment2.ass21[i]);
            shouldEqual(assignment1.ass21[i],assignment2.ass12[i]);
        }



        // some special cases

        A.clear();
        B.clear();

        Peak temp;
        temp.setCharge(1);
        temp.setRt(5.);
        temp.setMz(5.);

        //A contains two equal peaks
        A.push_back(temp);
        A.push_back(temp);

        temp.setRt(2.);
        temp.setMz(5.);
        B.push_back(temp);
        temp.setRt(3.);
        temp.setMz(5.);
        B.push_back(temp);
        temp.setRt(4.);
        temp.setMz(5.);
        B.push_back(temp);
        temp.setRt(5.);
        temp.setMz(5.);
        B.push_back(temp);
        temp.setRt(7.);
        temp.setMz(5.);
        B.push_back(temp);

        SM.setPeakList1(A);
        SM.setPeakList2(B);
        SM.setDrt(5.);
        SM.setDmz(5.);
        
        StableMarriage::Assignment assignment = SM.getAssignment();
        
        //Peak 0 should get the perfect match, Peak 1 the second best
        shouldEqual(assignment.ass12[0],3);
        shouldEqual(assignment.ass12[1],2);

        //Empty lists should cause an exception
        A.clear();
        B.clear();

        SM.setPeakList1(A);
        SM.setPeakList2(B);

        bool thrown = false;
		try
		{
			SM.getAssignment();
		}
		catch(ms::PreconditionViolation pv)
		{
			thrown = true;
		}
        shouldEqual(thrown, true);




        
        //test RtAssignment
        A.clear();
        B.clear();
        length = 100;

        for(int i = 0; i<length; i++){
            temp.setRt(4.*i);
            temp.setMz(4.*i);
            A.push_back(temp);
            temp.setRt(4.*i +1.);
            temp.setMz(4.*i +1.);
            B.push_back(temp);
        }

        SM.setPeakList1(A);
        SM.setPeakList2(B);
        SM.setDrt(sqrt(2.));
        SM.setDmz(sqrt(2.));

        StableMarriage::RtAssignment rtAssign = SM.getRtAssignment();
     
        for(unsigned int i = 0; i<rtAssign.size(); i++){
            //test if the pairs contain the right values
            shouldEqual(rtAssign[i].Rt1+1,rtAssign[i].Rt2);
            if(i>0){
                //test if the pairs are ordered
                shouldEqual(rtAssign[i-1].Rt1<=rtAssign[i].Rt1,true);
            }
        }

        //test if cost is calculated properly
        SM.setDint(0.); //make some change to force recalculation --> higher code coverage
        shouldEqualTolerance(SM.getCost(),1., 1e-12);


        //must not fail test
        PeakList PL0;
        for(unsigned int i = 100; i<200; i++){
            Peak P (double(i),1,1.,double(i+100));
            PL0.push_back(P);
        }

        PeakList PL1;
        for(unsigned int i = 99; i<199; i++){
            Peak P (double(i)+0.01,1,1.,double(i+70));
            PL1.push_back(P);
        }

        StableMarriage SMMNF1(PL0,PL1,200.,0.2,0,0);
        StableMarriage SMMNF2(PL1,PL0,200.,0.2,0,0);

        StableMarriage::Assignment ass1 = SMMNF1.getAssignment();
        StableMarriage::Assignment ass2 = SMMNF2.getAssignment();

        //test for symmetry
        for(int i = 0; i < length; i++){
            shouldEqual(ass1.ass12[i],ass2.ass21[i]);
            shouldEqual(ass1.ass21[i],ass2.ass12[i]);
        }




    }

    void testSortPeakList() 
	{
        //set up a peak list with random Mz and Rt values
        PeakList PL;
        for(int i = 0; i<100; i++){
            Peak t (double(rand()),1,1,0);
            t.setRt(double(rand()));
            PL.push_back(t);
        }

        //Initialize StableMarriage
        StableMarriage SM(PL,1.,1.,0.,0);

        //sort PL and get mapping
        PeakList sortedPL;
        std::vector<int> order = SM.sortPeakList_(PL,sortedPL);        

        for(int i = 0; i<100; i++){
            //did the mapping work?
            shouldEqual(PL[order[i]].getMz() ,sortedPL[i].getMz());
            //are all entries sorted?
            if(i>0){
                shouldEqual(sortedPL[i-1].getMz() <= sortedPL[i].getMz(), true);
            }
        }

        //test if all RegionId's are preserved
        for(int i = 0; i<100; i++){
            //did the mapping work?
            shouldEqual(PL[order[i]].getRegionId() ,sortedPL[i].getRegionId());

        }
    }

    void testCalculatePreferenceLists()
    {
        PeakList A;
        PeakList B;

        //Initialize StableMarriage
        StableMarriage SM(A,B,1.,1.,0.,0);

        // fail tests: empty vectors, illegal input
        //empty input lists
        A.clear(); B.clear();
        SM.setPeakList1(A);
        SM.setPeakList2(B);
        bool thrown = false;
		try
		{
			SM.calculatePreferenceLists_();
		}
		catch(ms::PreconditionViolation pv)
		{
			thrown = true;
		}
        shouldEqual(thrown, true);
        
        //drt/dmz <= 0
        Peak temp (1.,1,1,1.);
        A.push_back(temp);
        B.push_back(temp);
        SM.setPeakList1(A);
        SM.setPeakList2(B);
        SM.setDrt(0.);
        thrown = false;
		try
		{
			SM.calculatePreferenceLists_();
		}
		catch(ms::PreconditionViolation pv)
		{
			thrown = true;
		}
        shouldEqual(thrown, true);

        SM.setDrt(1.);
        SM.setDmz(-1.);
        thrown = false;
		try
		{
            SM.calculatePreferenceLists_();
		}
		catch(ms::PreconditionViolation pv)
		{
			thrown = true;
		}
        shouldEqual(thrown, true);

      
        // special cases: short/long peak lists, outliers, equal peaks
        A.clear();  //large peak list, narrow peaks
        B.clear();  //small peak list, contains one peak
        for(int i = 0; i<100; i++){
           temp.setMz(10.+0.01*double(i));
           temp.setRt(10.+0.01*double(i));
           A.push_back(temp);
        }
        temp.setMz(10.);
        temp.setRt(10.);
        B.push_back(temp);

        SM.setPeakList1(A);
        SM.setPeakList2(B);
        SM.setDrt(10.);
        SM.setDmz(10.);

        SM.calculatePreferenceLists_();
        shouldEqual((int)SM.pref12_.size(),100);  //100 preference lists
        shouldEqual((int)SM.pref21_.size(),1);    //Only one preference list
        shouldEqual((int)SM.pref21_[0].size(),100);//element in B has 100 preferred elements in A
        for(int i = 0; i<100; i++){
            shouldEqual((int)SM.pref12_[i].size(),1);//each element in A has only one element in it's pref. list
        }

        //one peak in A will have no preferred peak in B
        A[0].setMz(10000.);
        SM.setPeakList1(A);
        SM.calculatePreferenceLists_();
        shouldEqual((int)SM.pref12_[0].size(),0);//element 0 in A has no preferred elements in B (outlier)


        // symmetry test: pref(A,B) == pref(B,A)
        A.clear();
        B.clear();
        for(int i = 0; i<100; i++){
           temp.setMz(10.+myRandom);
           temp.setRt(10.+myRandom);
           A.push_back(temp);
           temp.setMz(10.+myRandom);
           temp.setRt(10.+myRandom);
           B.push_back(temp);
        }

        StableMarriage SM1(A,B,1.0,1.0,0.,0);
        StableMarriage SM2(B,A,1.0,1.0,0.,0);

        SM1.calculatePreferenceLists_();
        SM2.calculatePreferenceLists_();

        //test symmetry
        for(int i = 0; i < 100; i++){
            //length of each list should be equal
            shouldEqual(SM1.pref12_[i].size(),SM2.pref21_[i].size());
            shouldEqual(SM1.pref21_[i].size(),SM2.pref12_[i].size());
            //entries should be equal
            for(unsigned int j = 0; j<SM1.pref12_[i].size(); j++){
                shouldEqual(SM1.pref12_[i][j].index,SM2.pref21_[i][j].index);
            }
            for(unsigned int j = 0; j<SM1.pref21_[i].size(); j++){
                shouldEqual(SM1.pref21_[i][j].index,SM2.pref12_[i][j].index);
            }
        }

        //each preference list should be sorted
        for(int i = 0; i < 100; i++){
            for(unsigned int j = 0; j<SM1.pref12_[i].size(); j++){
                if(j>0){
                    shouldEqual(SM1.pref12_[i][j-1].distance<=SM1.pref12_[i][j].distance, true);
                }
            }
        }

        //must not fail test
        PeakList PL0;
        for(unsigned int i = 100; i<200; i++){
            Peak P (double(i),1,1.,double(i+100));
            PL0.push_back(P);
        }
        
        PeakList PL1;
        for(unsigned int i = 50; i<150; i++){
            Peak P (double(i)+0.01,1,1.,double(i+70));
            PL1.push_back(P);
        }
        StableMarriage SMMNF1(PL0,PL1,200.,0.2,0.,0);
        StableMarriage SMMNF2(PL1,PL0,200.,0.2,0.,0);
        SMMNF1.calculatePreferenceLists_();
        SMMNF2.calculatePreferenceLists_();

        //test symmetry
        for(int i = 0; i < 100; i++){
            //length of each list should be equal
            shouldEqual(SMMNF1.pref12_[i].size(),SMMNF2.pref21_[i].size());
            shouldEqual(SMMNF1.pref21_[i].size(),SMMNF2.pref12_[i].size());
            //entries should be equal
            for(unsigned int j = 0; j<SMMNF1.pref12_[i].size(); j++){
                shouldEqual(SMMNF1.pref12_[i][j].index,SMMNF2.pref21_[i][j].index);
            }
            for(unsigned int j = 0; j<SMMNF1.pref21_[i].size(); j++){
                shouldEqual(SMMNF1.pref21_[i][j].index,SMMNF2.pref12_[i][j].index);
            }
        }

    }
};

struct StableMarriageTestSuite : public vigra::test_suite {
    StableMarriageTestSuite() : vigra::test_suite("StableMarriage Tests") {
        add( testCase(&StableMarriageTest::testStableMarriage));
        add( testCase(&StableMarriageTest::testSortPeakList));
        add( testCase(&StableMarriageTest::testCalculatePreferenceLists));
        
    }
};

int main()
{
    StableMarriageTestSuite test;
    int failed = test.run();
    std::cout << test.report() << std::endl;
    
    return failed;
}
