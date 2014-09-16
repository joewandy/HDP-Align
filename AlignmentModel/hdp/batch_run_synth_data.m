clear all; close all; clc;

NSAMPS = 200;
BURN_IN = 100;

fprintf('\n=================================\n');
fprintf('REPLICATE 0\n');
fprintf('=================================\n');
[sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/documents/new_measure_experiment/input_data/synth_test/GT/rep_0/input/csv', true, true, false, 'synthdata_rt_mass', NSAMPS, BURN_IN);


