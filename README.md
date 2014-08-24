alignment-research
==================

Research Codes for Peak Alignment Methods

- AlignmentModel: throwaway Matlab/Octave codes as proof-of-concepts
- AlignmentResearch: Java implementations of various peak alignment algorithms that's being developed.
- MW_alignment: Python implementation of feature matching + DP mixture on retention time clustering
- MZmine_2_clone: fork of MZMine2 for evaluation purpose
- mzmatch_clone: fork of mzMatch for evaluation and integration purpose

Setting Up
======================================

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
