clear all; close all; clc;

NSAMPS = 2000;
BURN_IN = 1000;

fprintf('\n=================================\n');
fprintf('REPLICATE 0\n');
fprintf('=================================\n');
[sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/documents/new_measure_experiment/input_data/synth_test_met_10_2/GT/rep_0/input/csv', true, false, false, 'synthdata_rt_mass', NSAMPS, BURN_IN);


