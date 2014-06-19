clear all; close all; clc;

[sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/synth_datasets/rep_0/input/csv', true, false, false, 'synthdata_rt');
[sorted_res, samples] = run_hdp('/home/joewandy/Dropbox/Project/synth_datasets/rep_0/input/csv', true, true, false, 'synthdata_rt_mass');
