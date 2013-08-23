clear all; clc; close all;

fls = dir('*.csv');
for fi=1:numel(fls)
    run_rt_clustering_func(fls(fi).name);
    close all;
end
