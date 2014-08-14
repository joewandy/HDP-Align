clear all; close all; clc;

NSAMPS = 20;
BURN_IN = 10;

fprintf('\n=================================\n');
fprintf('REPLICATE 0\n');
fprintf('=================================\n');
[sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/documents/experiment_results/synthdata_experiments/GT_no_noise/rep_0/input/csv', true, true, false, 'synthdata_rt_mass', NSAMPS, BURN_IN);


