/*$Id:$*/

/*
 * all-test.cpp
 *
 * Copyright (c) 2010 Martin Lindner <martin.lindner@iwr.uni-heidelberg.de>
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


struct Evaluations {

    void Sample() 
	{
        //This code is only meant to illustrate, how the SIMA code can be used.
        return;

        //sample usage of alignment code

        //start with an empty PeakListCollection
        PeakListCollection plc;

        //add as many PeakLists (stored in files) as you like
        plc.addPeakList("../Data/M1/PL1.txt");
        plc.addPeakList("../Data/M1/PL2.txt");
        plc.addPeakList("../Data/M1/PL2.txt");
        plc.addPeakList("../Data/M1/PL2.txt");

        //you may also add already loaded peaklists
        PeakList PL ("../Data/M1/PL5.txt");
        
        //it works like in std::vector
        plc.push_back(PL);
        
        //calculate the correspondence estimation, called alignment
        double drt = 40.;
        double dmz = 0.03;
        double dint = 0.; //do not consider intensity

        DBL_MATRIX plc_al = plc.getAlignment(drt,dmz,0,dint,PeakListCollection::FAST,0);

        //from the alignment information, you can create an Alignment object
        Alignment Al (plc_al);

        //Here you are able to
        Al.writeRTC("filename.txt"); //export the rt-correspondences
        DBL_MATRIX MTS = Al.getMasterTimeScale(100); //calculate a Master Time Scale
        Al.writeMTS("filename.txt",MTS); //export the linepoints of the MTS

        //once you calculated the MTS, you can apply it to your original PeakLists
        plc.applyMasterTimeScale(MTS);

        //finally, you can run the correspondence estimation again (plc.getAlignment())
        //or export the modified spectra using saveAllPeakLists(char *folder, std::vector<std::string>& filenames)
    }
};

struct EvaluationsSuite : public vigra::test_suite {
    EvaluationsSuite() : vigra::test_suite("Alignment Tests") {
        add( testCase(&Evaluations::Sample));        
    }
};

int main()
{
    EvaluationsSuite test;
    int failed = test.run();
    std::cout << test.report() << std::endl;
    return failed;
}
