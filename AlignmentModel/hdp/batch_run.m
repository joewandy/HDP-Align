clear all; close all; clc;

NSAMPS = 20;
BURN_IN = 10;

% synthetic data
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/documents/experiment_results/synthdata_experiments/GT/rep_0/input/csv', true, false, false, 'synthdata_rt', NSAMPS, BURN_IN);
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/documents/experiment_results/synthdata_experiments/GT/rep_0/input/csv', true, true, false, 'synthdata_rt_mass', NSAMPS, BURN_IN);

% P1 data - fraction 080
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/real_datasets/P1/source_features_080/csv', true, false, false, 'hdp_result_rt', NSAMPS, BURN_IN);
[sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/real_datasets/P1/source_features_080/csv', true, true, false, 'hdp_result_rt_mass', NSAMPS, BURN_IN);

% P1 data - fraction 100
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/real_datasets/P1/source_features_100/csv', true, false, false, 'hdp_result_rt', NSAMPS, BURN_IN);
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/real_datasets/P1/source_features_100/csv', true, true, false, 'hdp_result_rt_mass', NSAMPS, BURN_IN);

% glycomics data
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/documents/new_measure_experiment/input_data/glycomics_2/csv', true, true, false, 'hdp_result_rt_mass', NSAMPS, BURN_IN);
