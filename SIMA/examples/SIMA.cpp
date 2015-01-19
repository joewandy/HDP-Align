/*$Id:$*/

/*
 * SIMAwin.cpp
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


#include <stdio.h>
#include <stdlib.h>
#include <string>
#include <iostream>

#ifdef _WIN32
# include <direct.h>
#else
# include <sys/stat.h>
# include <sys/types.h>
# include <dirent.h>
#endif

#include <ms++/config.h>
#include <ms++/Error.h>

#include <ms++/Alignment.h>
#include <ms++/PeakListCollection.h>


using namespace ms;



bool getopt(char *argument,char *option)
{
    if( argument[0]=='-' && argument[1]==option[0] ){
        return 1;
    }
    return 0;
}


void show_help(void)
{
    printf("\nUsage: SIMA [OPTION] [PARAMETER] [OPTION] [PARAMETER] ... \n\n"\
        "SIMA allows the alingment of LC-MS spectra. Use the parameters below.\n\n"\
        "\t-i\tInput folder (mandatory). Load all files in this folder.\n\t\tMake sure that only spectra are stored in this folder! \n"\
        "\t-R\tRetention time normalization drt (mandatory).\n"\
        "\t-M\tMass over charge normalization dmz (mandatory).\n"\
        "\t-Z\tCharge normalization (default: 0).\n"\
        "\t-o\tChange output folder. Default is [input folder]/results \n"\
        "\t-C\tEnable MTS correction using PARAMETER linepoints. \n\t\tRecommendation: 50-100 linepoints.\n "\
        "\t-c\tSave rt-correspondences separately in file.\n\t\tDefault is [input folder]/results/rtc.txt\n "\
        "\t-l\tSave MTS linepoints separately in file.\n\t\tDefault is [input folder]/results/mts.txt\n "\
        "\t-a\tAcceleration mode for correspondence calculation. \n\t\tPossible parameters: normal, fast, reference\n "\
        "\t-s\tSet the reference spectrum for the reference mode by its \n\t\tfilename (not path).\n "\
        "\t-x\tShow program information and exit.\n "\
        "\t-h\tShow this help text.\n");
}

void show_about(void)
{
    printf("\n"\
    "+----------------------------------------------------------+\n"\
    "|------ SIMA ----------------------------------------------|\n"\
    "+----------------------------------------------------------+\n\n"\
    "Copyright (c) 2010 Martin Lindner \n<martin.lindner@iwr.uni-heidelberg.de>\n\n"\
    "This file is part of ms++.\n\n"\
    "ms++ is free software: you can redistribute it and/or modify\n"\
    "it under the terms of the GNU Lesser General Public License as published by\n"\
    "the Free Software Foundation, either version 3 of the License, or\n"\
    "(at your option) any later version.\n\n"\
    "ms++ is distributed in the hope that it will be useful,\n"\
    "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"\
    "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"\
    "GNU Lesser General Public License for more details.\n\n"\
    "You should have received a copy of the GNU Lesser General Public License\n"\
    "along with ms++. If not, see <http://www.gnu.org/licenses/>.\n\n"\
    "Type SIMA -h to get get started.\n");
}

#ifdef _WIN32
std::vector<std::string> getFilenamesFromFolder(std::string folder)
{
    std::vector<std::string> files;

    //go through folder and collect filenames
    HANDLE fHandle;
    WIN32_FIND_DATA wfd;

    fHandle=FindFirstFile(folder.c_str(),&wfd);
    int counter = 0;

    do{
        // only look at objects that are not '.' or '..'
        if (!( (wfd.cFileName[0]=='.') && ( (wfd.cFileName[1]=='.' && wfd.cFileName[2]==0) || wfd.cFileName[1]==0 ) ))
        {
            if (wfd.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)
            {
                //don't do anything with directories
            }
            else
            {
                std::string s (wfd.cFileName); //get filename as string
                //add filename to list
                files.push_back(s);
            }
        }
    }
    while (FindNextFile(fHandle,&wfd));
    FindClose(fHandle);

    return files;
}

#else

std::vector<std::string> getFilenamesFromFolder(std::string folder)
{
    std::vector<std::string> files;

    DIR *dp;
    struct dirent *dirp;
    if((dp  = opendir(folder.c_str())) == NULL) {
        MSPP_LOG(logERROR) << "Error opening " << folder << std::endl;
        return files;
    }

    while ((dirp = readdir(dp)) != NULL) {
#ifdef _DIRENT_HAVE_D_TYPE
        if(dirp->d_type == DT_REG) {
            files.push_back(std::string(dirp->d_name));
        }
#else
        files.push_back(std::string(dirp->d_name));
        MSPP_LOG(logWARNING) << "Warning - Loader was not able to distinguish between folder and file. Make sure there are only files in the provided folder." << std::endl;
#endif

        
    }
    closedir(dp);


    return files;
}
#endif



int main(int argc, char *argv[])
{
    if(argc == 1){
        show_about();
        exit(0);
    }
    if(getopt(argv[1],(char*)"h") == 1 ){
        show_help();
        exit(0);
    } 

    // variables controlling program flow    
    std::string input_folder = "";
    std::string output_folder = "";

    double drt1 = -1;
    double dmz1 = -1;
    double dz = 0;

    bool run_mts = 0;
    int mts_linepoints = -1;

    bool save_rtc = 0;
    std::string rtc_filename = "";

    bool save_mts = 0;
    std::string mts_filename = "";

    std::string ref_filename = "";
    PeakListCollection::AccelerationFlag flag = PeakListCollection::NORMAL;
    int ref_index = -1;

    //evaluating input
    if(argc > 1){
        //go through all possible options
        for(int i=1; i < argc; i++){
            if(getopt(argv[i],(char*)"x") == 1){
                show_about();
                return 0;
            }

            //set input folder
            if(getopt(argv[i],(char*)"i") == 1){
                if(i<argc-1){
                    if(argv[i+1][0] != '-'){
                        input_folder = argv[i+1] ;
                    }else{
                        MSPP_LOG(logERROR) << argv[i+1] << " is not a valid input folder! Please provide a correct folder name." << std::endl;
                        exit(0);
                    }
                }
            }
            
            //set output folder
            if(getopt(argv[i],(char*)"o") == 1){
                if(i<argc-1){
                    if(argv[i+1][0] != '-'){
                        output_folder = argv[i+1];
                    }else{
                        MSPP_LOG(logERROR) << argv[i+1] << " is not a valid output folder! Please provide a correct folder name." << std::endl;
                    }
                }
            }
                        
            //get drt
            if(getopt(argv[i],(char*)"R") == 1){
                if(i<argc-1){
                    if(argv[i+1][0] != '-'){
                        drt1 = std::atof(argv[i+1]);
                    }else{
                        MSPP_LOG(logERROR) << argv[i+1] << " is not a valid number! Please provide a correct rt threshold." << std::endl;
                        exit(0);
                    }
                }
            }
                        
            //get dmz
            if(getopt(argv[i],(char*)"M") == 1){
                if(i<argc-1){
                    if(argv[i+1][0] != '-'){
                        dmz1 = std::atof(argv[i+1]);
                    }else{
                        MSPP_LOG(logERROR) << argv[i+1] << " is not a valid number! Please provide a correct m/z threshold." << std::endl;
                        exit(0);
                    }
                }
            }

            //get dz
            if(getopt(argv[i],(char*)"Z") == 1){
                if(i<argc-1){
                    if(argv[i+1][0] != '-'){
                        dmz1 = std::atof(argv[i+1]);
                    }else{
                        MSPP_LOG(logERROR) << argv[i+1] << " is not a valid number! Please provide a correct charge threshold." << std::endl;
                        exit(0);
                    }
                }
            }
                        
            //apply MTS
            if(getopt(argv[i],(char*)"C") == 1){
                if(i<argc-1){
                    if(argv[i+1][0] != '-'){
                        run_mts = 1;
                        mts_linepoints = std::atoi(argv[i+1]);
                    }else{
                        run_mts = 1;
                        mts_linepoints = 50;
                    }
                }else{
                    run_mts = 1;
                    mts_linepoints = 50;
                }
            }

            //save RTC
            if(getopt(argv[i],(char*)"c") == 1){
                if(i<argc-1){            
                    if(argv[i+1][0] != '-'){
                        save_rtc = 1;
                        rtc_filename = argv[i+1];
                    }else{
                        save_rtc = 1;
                    }
                }else{
                    save_rtc = 1;
                }
            }

            //save MTS
            if(getopt(argv[i],(char*)"l") == 1){
                if(i<argc-1){ 
                    if(argv[i+1][0] != '-'){
                        save_mts = 1;
                        mts_filename = argv[i+1];
                    }else{
                        save_mts = 1;
                    }
                }else{
                    save_mts = 1;
                }
            }

            //get acceleration mode
            if(getopt(argv[i],(char*)"a") == 1){
                if(i<argc-1){
                    if(std::string(argv[i+1]) == std::string("fast")){
                        flag = PeakListCollection::FAST;
                    }
                    if(std::string(argv[i+1]) == std::string("reference")){
                        flag = PeakListCollection::REFERENCE;
                    }
                }
            }

            //get reference spectrum
            if(getopt(argv[i],(char*)"s") == 1){
                if(i<argc-1){
                    if(argv[i+1][0] != '-'){
                        ref_filename = argv[i+1];
                    }else{
                        MSPP_LOG(logERROR) << argv[i+1] << " is not a valid spectrum! Please provide a valid filename or omit the -s option." << std::endl;
                    }
                }
            }
        }
    }

    //fill undefined variables with default values
    if(input_folder == ""){
        MSPP_LOG(logERROR) << "Please provide an input folder [-i]." << std::endl;
        exit(0);
    }

    if(drt1 <= 0 || dmz1 <= 0){
        MSPP_LOG(logERROR) << "Please provide correct normalization parameters drt [-R] and dmz [-M]. Both must be greater than 0." << std::endl;
        exit(0);
    }
        
    if(output_folder == ""){
        std::stringstream tempstr;
        tempstr << input_folder << DELIM << "results";
        output_folder = tempstr.str();
    }

    if(rtc_filename == ""){
        std::stringstream tempstr;
        tempstr << output_folder << DELIM << "rtc.txt";
        rtc_filename = tempstr.str();
    }

    if(mts_filename == ""){
        std::stringstream tempstr;
        tempstr << output_folder << DELIM << "mts.txt"; 
        mts_filename = tempstr.str();
    }

    if(mts_linepoints < 2){
        mts_linepoints = 50;
    }
    //Load PeakLists
    PeakListCollection PLC;

#ifdef _WIN32
    //create output folder
    _mkdir(output_folder.c_str());
    
    char sfolder [500];
    sprintf(sfolder,"%s\\*",input_folder.c_str());
    std::vector<std::string> filenames = getFilenamesFromFolder(sfolder);
#else
    //create output folder
    mkdir(output_folder.c_str(),0777);   
    std::vector<std::string> filenames = getFilenamesFromFolder(input_folder);
#endif

    //load files into PeakListCollection
    std::vector<std::string>::iterator it = filenames.begin();
    while(it != filenames.end()){
        if(*it == "." || *it == ".."){
            it = filenames.erase(it);
            continue;
        }else{
            std::stringstream path;
            path << input_folder << DELIM << (*it).c_str();
            PLC.addPeakList(path.str());
            MSPP_LOG(logINFO) << "Added file "<< (*it) << std::endl;
            it++;
        }
    }

    
    if(PLC.size() < 2){
        MSPP_LOG(logERROR) << "At least two spectra are needed to run SIMA. Please provide more spectra." << std::endl;
        exit(0);
    }


    //find reference Peaklist, if needed
    if(flag == PeakListCollection::REFERENCE){
        if(ref_filename != ""){
            ref_index = 0;
            for(unsigned int i = 0; i<filenames.size(); i++){
                if(filenames[i] == ref_filename){
                    ref_index = i;
                    MSPP_LOG(logINFO) << "Setting reference spectrum to "<< filenames[i] << std::endl;
                    break;
                }
            }
        } else {
            ref_index = 0;
            MSPP_LOG(logINFO) << "Setting reference spectrum to "<< filenames[0] << std::endl;
        }
    }


    MSPP_LOG(logINFO) << "Starting alignment of spectra. This may take a while." << std::endl;
    DBL_MATRIX RTC = PLC.getAlignment(drt1,dmz1,dz,0.,flag,ref_index);
    MSPP_LOG(logINFO) << "Alignment of spectra finished." << std::endl;
  
    std::stringstream ofilename;
    ofilename << output_folder << DELIM << "result.txt";

    //save result of first correspondence estimation
    PLC.createCorrespondenceFile(ofilename.str(),filenames);

    Alignment AL (RTC);

    //save rt-correspondences, if needed
    if(save_rtc){
        AL.writeRTC(rtc_filename.c_str());
        MSPP_LOG(logINFO) << "Rt correspondences written to file " <<rtc_filename<< std::endl;
    }

    //calculate MTS if nescessary
    if(save_mts || run_mts){
        MSPP_LOG(logINFO) << "Calculating Master Time Scale. This may take a while." << std::endl;
        DBL_MATRIX MTS = AL.getMasterTimeScale(mts_linepoints);
        MSPP_LOG(logINFO) << "Calculation finished." << std::endl;

        if(save_mts){
            AL.writeMTS(mts_filename,MTS);
            MSPP_LOG(logINFO) << "Master Time Scale written to file " <<mts_filename<< std::endl;
        }
        if(run_mts){
            MSPP_LOG(logINFO) << "Correcting spectra with Master Time Scale." << std::endl;
            PLC.applyMasterTimeScale(MTS);
            
            MSPP_LOG(logINFO) << "Second alignment of spectra." << std::endl;
            PLC.getAlignment(drt1,dmz1,dz,0.,flag,ref_index);
            ofilename.str(std::string());
            ofilename << output_folder << DELIM << "result_MTS.txt";
            PLC.createCorrespondenceFile(ofilename.str(),filenames);

            PLC.saveAllPeakLists(output_folder,filenames);
        }
    }

    MSPP_LOG(logINFO) << "SIMA finished processing folder '" << input_folder << "'." << std::endl;
   
    return 0;
}
