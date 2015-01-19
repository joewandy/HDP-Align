/*$Id:$*/

/*
 * Alignment-test.cpp
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


#include <ms++/Alignment.h>
#include <ms++/PeakListCollection.h>
#include <tests/unittest.hxx>



using namespace ms;

const double TOL = 1e-12;

#define myRandom double(rand())/double(RAND_MAX)

struct AlignmentTest {
    
    void TestSigmoid() 
	{
        DBL_MATRIX dummyrtc;
        Alignment A(dummyrtc);
        //test sigmoid_ for scalar input
        shouldEqualTolerance(A.sigmoid_(1.,1.),0.731058578630005,TOL);
        shouldEqualTolerance(A.sigmoid_(1.,-1.),0.268941421369995,TOL);

        DBL_MATRIX delta (5,1);
        delta(0,0) = -1.;
        delta(1,0) = 0.;
        delta(2,0) = 1.;
        delta(3,0) = 2.;
        delta(4,0) = 10.;

        //test sigmoid_ for vector input
        DBL_MATRIX sig = A.sigmoid_(delta,1.);
        shouldEqual(sig.rowCount(),5);
        shouldEqual(sig.columnCount(),1);
        shouldEqualTolerance(sig(0,0),0.268941421369995,TOL);
        shouldEqualTolerance(sig(1,0),0.5,TOL);
        shouldEqualTolerance(sig(2,0),0.731058578630005,TOL);
        shouldEqualTolerance(sig(3,0),0.880797077977882,TOL);
        shouldEqualTolerance(sig(4,0),0.999954602131298,TOL);

        // Test SumSigmoid_

        //set up some dummy rtCorrespondences
        int dim = 3;
        int size = 10;
        DBL_MATRIX rtc (size,dim);


        for(int i = 0; i<size; i++){
            for(int  j = 0; j<dim; j++){
                rtc(i,j) = 100. + 5.*double(i) + 0.01*double(j);
            }
        }

        //probe vector x0
        DBL_MATRIX x0 (dim,1);
        x0(0,0) = 120.;
        x0(1,0) = 130.;
        x0(2,0) = 140.;

        // do test
        double s = A.sumSigmoid_(x0, rtc, 10.);

        shouldEqualTolerance(s,-6.049833997312503,TOL);

    }


    void TestMatOp() 
	{
        DBL_MATRIX rtc;
        Alignment A(rtc);

        DBL_MATRIX testmat (4,4,0.);

        for(int i = 0; i < 4; i++){
            for(int j = 0; j < 4; j++){
                testmat(i,j) = double(i*j + i - j);
            }
        }
      
        //Test extractRow
        DBL_MATRIX row = extractRow(testmat,2);
        shouldEqual(row.rowCount(),1);
        shouldEqual(row.columnCount(),4);
        shouldEqual(row(0,0),2.);
        shouldEqual(row(0,1),3.);
        shouldEqual(row(0,2),4.);
        shouldEqual(row(0,3),5.);

        //Test extractColumn
        DBL_MATRIX col = extractColumn(testmat,0);
        shouldEqual(col.rowCount(),4);
        shouldEqual(col.columnCount(),1);
        shouldEqual(col(0,0),0.);
        shouldEqual(col(1,0),1.);
        shouldEqual(col(2,0),2.);
        shouldEqual(col(3,0),3.);

        //Test precondition violations
        bool thrown = false;
        try{
            DBL_MATRIX f = extractRow(testmat,-1);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

        thrown = false;
        try{
            DBL_MATRIX f = extractRow(testmat,4);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

        thrown = false;
        try{
            DBL_MATRIX f = extractColumn(testmat,-1);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

        thrown = false;
        try{
            DBL_MATRIX f = extractColumn(testmat,4);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);
            

    }

    void TestInitTimeScale() 
	{
        DBL_MATRIX rtc;
        Alignment A(rtc);

        unsigned int dim = 5;
        unsigned int size = 20;
        double min_rt = 23.;
        double max_rt = 42.;

        DBL_MATRIX its = A.initTimeScale_(dim, min_rt, max_rt, size);

        shouldEqual(its.columnCount(),(int)dim);
        shouldEqual(its.rowCount(),(int)size);
        for(unsigned int i = 0; i<size; i++){
            for(unsigned int j = 0; j<dim; j++){
                shouldEqualTolerance(its(i,j),double(i) + min_rt,TOL);
            }
        }
    }

    void TestValOnHPlane()
    {
        DBL_MATRIX dummyrtc;
        Alignment A(dummyrtc);

        //set up some dummy rtCorrespondences
        int dim = 3;
        int size = 10;
        DBL_MATRIX rtc (size,dim);

        for(int i = 0; i<size; i++){
            for(int  j = 0; j<dim; j++){
                rtc(i,j) = 100. + 5.*double(i) + 0.01*double(j);
            }
        }

        //support vector y
        DBL_MATRIX y (dim,1);
        y(0,0) = 130.;
        y(1,0) = 130.;
        y(2,0) = 130.;

        //probe vector x0
        DBL_MATRIX x0 (dim-1,1);
        x0(0,0) = 129.;
        x0(1,0) = 131.;

        double val = A.valOnHPlane_(x0,y,rtc,10.);

        shouldEqualTolerance(val, -9.000048020661236, TOL);

        //evoke some precondition violations

        //wrong length of x0
        x0.reshape(3,1);

        bool thrown = false;
        try{
            A.valOnHPlane_(x0,y,rtc,10.);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

        //wrong length of y
        x0.reshape(2,1);
        y.reshape(5,1);

        thrown = false;
        try{
            A.valOnHPlane_(x0,y,rtc,10.);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

        //use matrix instead of vector for x0
        x0.reshape(2,2);
        y.reshape(3,1);

        thrown = false;
        try{
            A.valOnHPlane_(x0,y,rtc,10.);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

        //use matrix instead of vector for y
        x0.reshape(2,1);
        y.reshape(3,4);

        thrown = false;
        try{
            A.valOnHPlane_(x0,y,rtc,10.);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

    }

    void TestGradOnHPlane()
    {
        DBL_MATRIX dummyrtc;
        Alignment A(dummyrtc);

        //set up some dummy rtCorrespondences
        int dim = 3;
        int size = 10;
        DBL_MATRIX rtc (size,dim);

        for(int i = 0; i<size; i++){
            for(int  j = 0; j<dim; j++){
                rtc(i,j) = 100. + 5.*double(i) + 0.01*double(j);
            }
        }

        
        //std::cout << rtc << std::endl;

        //support vector y
        DBL_MATRIX y (dim,1);
        y(0,0) = 130.;
        y(1,0) = 130.;
        y(2,0) = 130.;

        //probe vector x0
        DBL_MATRIX x0 (dim-1,1);
        x0(0,0) = 129.;
        x0(1,0) = 131.;

        //calculate gradient
        DBL_MATRIX grad = A.gradOnHPlane_(x0,y,rtc,1.);

        //std::cout << grad << std::endl;

        shouldEqual(grad.rowCount(),2);
        shouldEqual(grad.columnCount(),1);

        //compare to matlab results
        shouldEqualTolerance(grad(0,0),-0.059492491272081,TOL);
        shouldEqualTolerance(grad(1,0), 0.061402685220511,TOL);

        //Missing correspondences
        rtc(5,0) = 0;
        rtc(6,1) = 0;
        rtc(7,2) = 0;

        //std::cout << A.gradOnHPlane_(x0,y,rtc,1.) << std::endl;
        
    }

    void TestStepSizeND()
    {
        DBL_MATRIX dummyrtc;
        Alignment A(dummyrtc);

        //set up some dummy rtCorrespondences
        int dim = 3;
        int size = 10;
        DBL_MATRIX rtc (size,dim);

        for(int i = 0; i<size; i++){
            for(int  j = 0; j<dim; j++){
                rtc(i,j) = 100. + 5.*double(i) + 0.01*double(j);
            }
        }

        //support vector y
        DBL_MATRIX y (dim,1);
        y(0,0) = 130.;
        y(1,0) = 130.;
        y(2,0) = 130.;

        //probe vector x0
        DBL_MATRIX x0 (dim-1,1);
        x0(0,0) = 129.;
        x0(1,0) = 131.;

        //direction vector d
        DBL_MATRIX d (dim-1,1);
        d(0,0) = 1.;
        d(1,0) = 1.;

        //calculate stepsize
        double newstepsize = A.stepSizeND_(x0,y,d,1.,1.,1.,rtc,1.);

        //compare to matlab results
        shouldEqualTolerance(newstepsize,6.103515625000000e-005,TOL);

        //evoke some precondition violations

        //use row- instead of column vectors
        bool thrown = false;
        try{
            DBL_MATRIX t = x0.transpose();
            A.stepSizeND_(t,y,d,1.,1.,1.,rtc,1.);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

        //wrong length of input data
        thrown = false;
        try{
            A.stepSizeND_(x0,y,y,1.,1.,1.,rtc,1.);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);

        thrown = false;
        try{
            A.stepSizeND_(x0,d,d,1.,1.,1.,rtc,1.);
        } catch(ms::PreconditionViolation pv) {
            thrown = true;
        }
        should(thrown);
    }

    void TestEstimateAlpha()
    {
        DBL_MATRIX dummyrtc;
        Alignment A(dummyrtc);

        //set up some dummy rtCorrespondences
        int dim = 3;
        int size = 10;
        DBL_MATRIX rtc (size,dim);

        for(int i = 0; i<size; i++){
            for(int  j = 0; j<dim; j++){
                rtc(i,j) = 100. + 5.*double(i) + 0.01*double(j);
            }
        }

        //support vector y
        DBL_MATRIX y (2,dim);
        y(0,0) = 130.;
        y(0,1) = 130.;
        y(0,2) = 130.;
        y(1,0) = 131.;
        y(1,1) = 131.;
        y(1,2) = 131.;

        //calculate alpha estimation:
        //since the estimation of alpha is heuristic and still needs to
        //be implemented properly, only some general tests are performed here.

        DBL_MATRIX est = A.estimateAlpha_(y,rtc);

        bool b = false;
        try{
            DBL_MATRIX est = A.estimateAlpha_(y,rtc);
        } catch(ms::PreconditionViolation pv) {
            b = true;
        }
        //no precondition violations
        should(!b);
        
        //output dimensions:
        shouldEqual(est.rowCount(),y.rowCount());
        shouldEqual(est.columnCount(),1);

        
    }

    void TestLineDistort()
    {
        //set up some dummy rtCorrespondences
        int dim = 3;
        int size = 10;
        DBL_MATRIX rtc (size,dim);
        Alignment A(rtc);

        for(int i = 0; i<size; i++){
            for(int  j = 0; j<dim; j++){
                rtc(i,j) = 100. + 5.*double(i) + 0.01*double(j);
            }
        }

        int k = 20;
        //init line with k Points
        DBL_MATRIX initL = A.initTimeScale_(3,90.,160.,k);

        //line points (one dimension less than initL)
        DBL_MATRIX lp (k,2,0.);
        for(int i = 0; i<k; i++){
            for(int j = 0; j<2; j++){
                lp(i,j) = initL(i,j);
            }
        }
        

        //calculate alpha estimates
        DBL_MATRIX alpha = A.estimateAlpha_(initL,rtc);
                
        //set constants
        double c1 = 0.25;
        double c2 = 0.75;

        //do line distortion
        DBL_MATRIX nlp = A.lineDistort_(lp,initL,rtc,c1,c2,alpha);

        shouldEqual(nlp.columnCount(),initL.columnCount());
        shouldEqual(nlp.rowCount(),initL.rowCount());
                
    }


    void TestGetMasterTimeScale()
    {
        //set up some dummy Rt-Correspondences
        int dim = 3;
        int size = 200;
        DBL_MATRIX rtc (size,dim);
        double min = 10;
        double max = 1000;

        //set up some correspondent values
        for(int i = 0; i<size; i++){
            double pos = myRandom * (max-min);
            for(int  j = 0; j<dim; j++){
                rtc(i,j) = min + pos + (max-min)/60.*sin(4*2.*3.141*pos/(max-min)+.5*3.141*j) + (max-min)/90.*(myRandom - 0.5);
            }
        }

        Alignment A(rtc);

        //TIC
        DBL_MATRIX MTS = A.getMasterTimeScale(20);
        //A.writeMTS("C:/Users/mlindner/Desktop/mts.txt",MTS);
        //A.writeRTC("C:/Users/mlindner/Desktop/rtc.txt");
        //A.writeValley2D("C:/Users/mlindner/Desktop/map.txt",500,0,1);
        //TOC


        shouldEqual(MTS.rowCount(),20);
        shouldEqual(MTS.columnCount(),rtc.columnCount());


        ////testing with real data

        //PeakListCollection plc;

        //plc.addPeakList("C:/Users/mlindner/Desktop/AlData/M1/PL1-1.txt");
        //plc.addPeakList("C:/Users/mlindner/Desktop/AlData/M1/PL1-2.txt");
        //plc.addPeakList("C:/Users/mlindner/Desktop/AlData/M1/PL1-3.txt");
        ////plc.addPeakList("C:/Users/mlindner/Desktop/AlData/M1/PL1-4.txt");
        ////plc.addPeakList("C:/Users/mlindner/Desktop/AlData/M1/PL1-5.txt");
        ////plc.addPeakList("C:/Users/mlindner/Desktop/AlData/M1/PL1-6.txt");

        //TIC
        //DBL_MATRIX plc_al = plc.fastGetAlignment(sqrt(30.),sqrt(0.02),0.);
        //TOC
        //
        //Alignment rAl (plc_al);

        //RETIC
        //DBL_MATRIX rMTS = rAl.getMasterTimeScale(100);
        //TOC
        ////rAl.writeValley2D("C:/Users/mlindner/Desktop/map.txt",100,0,1);
        ////rAl.writeMTS("C:/Users/mlindner/Desktop/mts.txt",rMTS);
        ////rAl.writeRTC("C:/Users/mlindner/Desktop/rtc.txt");

    }
};

struct AlignmentTestSuite : public vigra::test_suite {
    AlignmentTestSuite() : vigra::test_suite("Alignment Tests") {
        add( testCase(&AlignmentTest::TestSigmoid));
        add( testCase(&AlignmentTest::TestMatOp));
        add( testCase(&AlignmentTest::TestInitTimeScale));
        add( testCase(&AlignmentTest::TestValOnHPlane));
        add( testCase(&AlignmentTest::TestGradOnHPlane));
        add( testCase(&AlignmentTest::TestStepSizeND));
        add( testCase(&AlignmentTest::TestEstimateAlpha));
        add( testCase(&AlignmentTest::TestLineDistort));
        add( testCase(&AlignmentTest::TestGetMasterTimeScale));
    }
};

int main()
{
    AlignmentTestSuite test;
    int failed = test.run();
    std::cout << test.report() << std::endl;
    return failed;
}
