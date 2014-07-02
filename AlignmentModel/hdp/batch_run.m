clear all; close all; clc;

% synthetic data
[sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/documents/experiment_results/synthdata_experiments/GT/rep_0/input/csv', true, false, false, 'synthdata_rt', 200, 100);
[sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/documents/experiment_results/synthdata_experiments/GT/rep_0/input/csv', true, true, false, 'synthdata_rt_mass', 200, 100);

% P1 data - fraction 080
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/real_datasets/P1/source_features_080/csv', true, false, false, 'hdp_result_rt');
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/real_datasets/P1/source_features_080/csv', true, true, false, 'hdp_result_rt_mass');

% P1 data - fraction 100
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/real_datasets/P1/source_features_100/csv', true, false, false, 'hdp_result_rt');
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/real_datasets/P1/source_features_100/csv', true, true, false, 'hdp_result_rt_mass');

% glycomics data
% [sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/tsai_datasets/glycomics/glyco_sima_2/csv', true, true, false, 'hdp_result_rt_mass');
