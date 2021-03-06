HDP-Align
==================

This repository primarily contains the implementation for **HDP-Align**, a hierarchical Bayesian non-parametric model that performs peak matching for liquid-chromatography mass-spectrometry data using a Hierarchical Dirichlet Process mixture model. This is still a work-in-progress (kind of abandoned, in fact), but its description can be found here: http://biorxiv.org/content/early/2016/09/13/074831.

Model prototypes in Matlab can be found in the **AlignmentModel** folder. The working Java implementation of HDP-Align is inside **HDP-Align** (click [here](https://github.com/joewandy/HDP-Align/blob/master/HDP-Align/src/main/java/com/joewandy/alignmentResearch/alignmentMethod/custom/hdp/HDPMassRTClustering.java) for the primary class that performs the clustering), alongside a few other alignment methods used in the evaluation experiments. **MultiAlignPipeline** contains the pipeline used for loading feature data in FeatureXML or SIMA format, performs alignment on them and computes the performance measures (Precision/Recall as defined in the paper). Local copies of MzMine2 and SIMA used for the purpose of evaluation in the paper is also provided in this repository.

Release & Experiments
======================================

Executable binary and instructions on running experiments in the paper can be found at [the repository's release page](https://github.com/joewandy/HDP-Align/releases/tag/1.0).

Development Set-up
======================================

Only if you're a developer!!

Importing projects
------------------

1. Install the Maven and eGit plug-ins in Eclipse
2. Clone the repository https://github.com/joewandy/HDP-Align.git using either the `git clone` command or do it from within Eclipse. If you're doing it from the command line via `git clone`, be sure to add the repository to Eclipse as well (via Git Repositories view -> Add an existing local Git repository).
3. Go to the Git Repositories view in Eclipse and expand the Working Directories in the added repository. We want to add each project in the repository into Eclipse. Start with PeakML. Right click **PeakML**, select Import Project > Use the New Project wizard > Java Project > enter `PeakML` for the project name, change the default location to the git working directory (e.g. /home/joewandy/git/HDP-Align/PeakML), click Finish.
4. Repeat for the remaining projects too: **mzMatch** and **cmdline** are imported as existing Java projects. **MZmine2**, **MUltiAlignPipeline** and **HDP-Align** should be imported as existing Maven projects (use File > Import > Existing Maven Projects from the menu).
5. There will be compilation errors in most of the projects due to unresolved dependencies. See next section.

Setting up dependencies
-----------------------

To keep it simple, we will manage dependencies between projects using Eclipse.

1. **PeakML** and **MZmine2** do not depend on anything.
2. **mzMatch** depends on **PeakML** and **cmdline**. Right-click on the **mzMatch** project from Package Explorer, select Properties, then go to Java Build Path, go to Projects tab, add **PeakML** and **cmdline** there.
3. **MultiAlignPipeline** depends on **cmdline**, **HDP-Align** and **PeakML**, so follow step (2) to add these dependencies.
4. **HDP-Align** depends on **mzMatch**, **MZmine2** and **PeakML**, so add these project dependencies as well. Additionally, go to the Libraries tab and also add all the jars inside HDP-Align/lib.

Extra stuff
-----------

git config http.postBuffer 524288000
