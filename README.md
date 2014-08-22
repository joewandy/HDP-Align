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

0. Install eGit and maven plugins in eclipse
1. Clone the repository. git clone https://github.com/joewandy/phd-research.git
2. From Eclipse, open Git perspective. Go to Git Repositories view, choose 'Add an existing local Git repository'
3. Expand the Working Directories in the added repository, right click 'mzmatch_clone', select Import Project > Use the New Project wizard > Java Project > enter 'mzmatch_clone' for the project name, change the location to the git working directory (e.g. /home/joewandy/git/phd-research/mzmatch_clone)
4. Repeat for MZmine_2_clone and AlignmentResearch, but with Import Maven Projects
5. Right click on mzmatch_clone project, select Properties, go to Projects tab, add AlignmentResearch
6. Add mzmatch_clone and MZmine_2_clone too into the dependencies of AlignmentResearch
7. Mark Circular dependencies as "Warning" in Eclipse tool to avoid "A CYCLE WAS DETECTED IN THE BUILD PATH" error. In Eclipse got to :-> Windows -> Prefereneces -> Java-> Compiler -> Buliding -> Circular Depencies. This is because of poor modularity in the code ! To be fixed soon.
8. Right click AlignmentResearch project > Properties > Libraries > Add JARs > select the AlignmentResearch project/lib, add all the jars inside
