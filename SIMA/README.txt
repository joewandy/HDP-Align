S
MA - Simultaneous Multiple Alignment of LC/MS Data
=====================================================

Copyright (c) 2010 Martin Lindner <martin.lindner@iwr.uni-heidelberg.de>

This file is part of ms++.

ms++ is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

ms++ is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with ms++. If not, see <http://www.gnu.org/licenses/>.
  

SIMA Command line tool
----------------------

  Prerequisites:
    - A folder containing spectra (no other files), for example '/some/folder'
    - Each spectrum is a plain text file with the following structure:
      - One line contains information of exactly one peak
      - The delimiter is a TAB
      - Structure of each line:
        [m/z] [charge] [intensity] [rt]
      - The electric charge is ignored
    - The compiled SIMA command line tool

  Running the tool:
    Configurations are passed to the tool as parameters:
      > SIMA [OPTION] [PARAM]

    Set of MANDATORY parameters:
    -R retention time (rt) threshold (drt)
    -M mass over charge (m/z) threshold (dmz)
    -i input folder containing the spectra.

    Set of optional parameters:
    -Z charge state threshold. If it is set to 0 (default), only peaks with equal charge state will be merged.
    -C Enable correction of the spectra by the Master Time Scale. Pass the
       number of discrete points as PARAM. Recommendation: 50-100 points.
    -o Provide a different output folder as PARAM. The default output
       folder is '[input folder]/results'
    -l Save the Master Time Scale points in a separate file. If no filename
       is provided as PARAM, default is '[input folder]/results/mts.csv'
    -c Save the rt correspondence matrix to a separate file. If no filename is
       provided as PARAM, default is '[input folder]/results/rtc.csv'

    -a Accelerate the correspondence calculation. Default is 'normal'. Possible
       parameters are: 'normal', 'fast', 'reference'
       'normal' merges the best matching spectra first. This can be expensive!
       'fast' merges the spectra one by one.
       'reference' merges all spectra in one reference spectrum.
    -s Set the filename (not path) of the reference spectrum, if the 'reference'
       mode is selected.

  Examples:
    Assume a set of spectra in the folder '/some/folder'.

    1. Find correspondences with drt = 100, dmz = 10 (nothing fancy):

      > SIMA -R 100 -M 10 -i /some/folder

      Correspondences are placed in '/some/folder/results/result.csv'.

    2. Same as 1, but calculate MTS (70 points) and correct spectra:

      > SIMA -R 100 -M 10 -i /some/folder -C 70

      Correspondences are placed in '/some/folder/results/result_MTS.csv',
      corrected spectra are written to '/some/folder/results/[original name]'.

    3. Additionally write out the points of the Master Time Scale and the
       rt correspondence matrix.

      > SIMA -R 100 -M 10 -i /some/folder -C 70 -l -c -o /some/other/folder

      All results are written to '/some/other/folder'.

  Correspondence file format:
    The correspondence files list all peaks that have a match in other spectra.
    Each group of corresponding peaks has an index assigned. Each peak can be
    identified explicitly by its origin spectrum and its index in the spectrum.

    Each line in the file contains exactly one peak, delimiter is a TAB:
    [group] [origin spectrum] [origin index] [m/z] [rt]
 
	


   
   
   
