## Performance evaluation

To make evaluation easy, the various alignment methods tested are called within a Java pipeline that reads input files in either in featureXML or following SIMA format. The pipeline also computes performance measures (Precision, Recall, F1) produced on the output produced by the script against user-defined alignment ground truth ([computed here](https://github.com/joewandy/HDP-Align/blob/master/HDP-Align/src/main/java/com/joewandy/alignmentResearch/model/GroundTruth.java)). This pipeline can be found at https://github.com/joewandy/HDP-Align and bundled as alignment.jar (and its accompanying alignment_lib). All these can be obtained by checking out the whole repository, as explained in the main README file.

To reproduce key results from the paper, follow the steps:

1. Download all the necessary files (executables, input_data) in this repository. **alignment.jar** is the main executable for the evaluation pipeline that loads the input files in either featureXML or SIMA format. Be sure not to change the relative paths of the input files and executables. We also include SIMA executable for evaluation purposes, while mzMine is already bundled inside alignment.jar.
2. Go into this **experiments** folder from the shell.
3. For Proteomic results, execute the script **run_P1.sh**.
4. For Glycomic results, execute the script **run_glyco.sh**.
5. For Metabolomic results, execute the script **run_meta.sh**.
6. Results can be found in the **Results** folder created by each shell script in step (3) - (5).

### Notes

MzMine throws some null pointer exception as MzMine core is loaded and it can't find the properties file. That can be safely ignored for now as it doesn't affect the alignment results. Will fix this later on.

## Extending the pipeline

The pipeline here can be easily extended to load other data types (currently supporting featureXML and SIMA format files), run other aligment methods and compute various performance measures. Examples for the methods evaluated in the paper can be found here: [MzMine2 Join](https://github.com/joewandy/HDP-Align/blob/master/HDP-Align/src/main/java/com/joewandy/alignmentResearch/alignmentMethod/external/MzMineJoinAlignment.java), [SIMA](https://github.com/joewandy/HDP-Align/blob/master/HDP-Align/src/main/java/com/joewandy/alignmentResearch/alignmentMethod/external/SimaAlignment.java) and the [MW scripts](https://github.com/joewandy/HDP-Align/blob/master/HDP-Align/src/main/java/com/joewandy/alignmentResearch/alignmentMethod/external/PythonMW.java). Also, [OpenMS](https://github.com/joewandy/HDP-Align/blob/master/HDP-Align/src/main/java/com/joewandy/alignmentResearch/alignmentMethod/external/OpenMSAlignment.java) and [MzMine2 RANSAC](https://github.com/joewandy/HDP-Align/blob/master/HDP-Align/src/main/java/com/joewandy/alignmentResearch/alignmentMethod/external/MzMineRansacAlignment.java). The last two are old codes for methods which were not included for comparative evaluation -- the codes need to be cleaned up.
