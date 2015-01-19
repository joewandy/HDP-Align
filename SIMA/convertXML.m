% XML Converter tool for the LC/MS Data sets published by (Lange et al.
% 2008)
% This script was written to convert the datasets benchmark provided by
% (Lange et al., 2008) into txt files that can be processed by SIMA.
% http://www.ncbi.nlm.nih.gov/pmc/articles/PMC2570366/
% http://msbi.ipb-halle.de/msbi/caap
% 
% This tool uses a XML-Toolbox which can be found under:
% http://www.mathworks.com/matlabcentral/fileexchange/4278
%
%
% How to use the Converter:
% (1) download and decompress the datasets M1, M2, P1, and P2
%
% (2) download the XML-Toolbox from
% http://www.mathworks.com/matlabcentral/fileexchange/4278
%
% (3) set the correct path to the toolbox in line 29
%
% (4) execute the script and select the peak-lists you want to
% convert
%
% (5) the converter will generate a folder "/SIMA-converted/" in the folder
% storing the XML peak-lists.
%
% (6) In "/SIMA-converted/" for each peak XML peak-list a txt-file is generated.
% In this new file each line corresponds to the peak information
% [mz, charge, intensity, rt] of a single peak from the XML peak list
%
%
% Note that the data sets P1 and P2 contain different fractions that have to
% stored in different folders before running SIMA.

clear all;
close all;

% Path of the XML-Toolbox
addpath('~/src/xml_toolbox/');

% Get the path to the XML Files
[filenames, Path] = uigetfile('*.*', 'Select XML peak-lists.','MultiSelect','on');

% New Folder
mkdir(Path,'SIMA-converted');

% Extract txt files from each selected XML file
for fileNr = 1:size(filenames,2)
        ['Processing file Nr. ' num2str(fileNr) ' of ' num2str(size(filenames,2))]
		peakList = xml_load([Path filenames{fileNr}]);
		peakListMat=zeros(size(peakList.featureList,2),2);
		for i = 1:size(peakList.featureList,2);
			[rt, mz] = peakList.featureList(i).feature.position;
			int = peakList.featureList(i).feature.intensity;
			peakListMat(i,1) = str2double(mz);
			peakListMat(i,2) = 1;
			peakListMat(i,3) = str2double(int);
			peakListMat(i,4) = str2double(rt);
		end
		dlmwrite([Path 'SIMA-converted/' filenames{fileNr} '.txt'], peakListMat,...
		'delimiter', '\t', 'precision', 10);
end
