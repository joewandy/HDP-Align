HDP-Align
==================

This repository primarily contains the implementation for **HDP-Align**, a hierarchical Bayesian non-parametric model, that performs peak matching for liquid-chromatography mass-spectrometry data using a Hierarchical Dirichlet Process mixture model. Detailed description of the paper can be found in *HDP-Align: Hierarchical Dirichlet Process Clustering for Multiple Peak Alignment of LC-MS Data* by Wandy, et al. (2015).

Model prototypes in Matlab can be found in the **AlignmentModel** folder. The working Java implementation of HDP-Align is inside **AlignmentResearch**, alongside the evaluation pipeline used in the paper to evaluate performance against the selected benchmark methods (Join, SIMA). Copies of MzMine2 and SIMA used for the purpose of evaluation in the paper is also provided in this repository.

Experiments
======================================

The folder **experiments** contains all the experiments done to produce the results in the paper. Further details can be found inside.

Development Set-up
======================================

Only if you're a developer!! 

Importing projects

1. Install eGit and maven plugins in eclipse
2. Clone the repository. git clone https://github.com/joewandy/phd-research.git, either from command line or from within eclipse. If doing from command line, be sure to add it to Eclipse as well via Git Repositories view -> Add an existing local Git repository
3. Expand the Working Directories in the added repository, right click 'mzmatch_clone', select Import Project > Use the New Project wizard > Java Project > enter 'mzmatch_clone' for the project name, change the location to the git working directory (e.g. /home/joewandy/git/phd-research/mzmatch_clone)
4. Repeat for MZmine_2_clone and AlignmentResearch by importing as existing projects.

Setting up dependencies

1. Right click on mzmatch_clone project, select Properties, go to Projects tab, add AlignmentResearch
2. Add mzmatch_clone and MZmine_2_clone too into the dependencies of AlignmentResearch
3. Mark Circular dependencies as "Warning" in Eclipse tool to avoid "A CYCLE WAS DETECTED IN THE BUILD PATH" error. In Eclipse got to :-> Windows -> Prefereneces -> Java-> Compiler -> Buliding -> Circular Depencies. This is because of poor modularity in the code ! To be fixed soon.
4. Right click AlignmentResearch project > Properties > Libraries > Add JARs > select the AlignmentResearch project/lib, add all the jars inside

