clear all; close all;

mass_tol = 1.5;         % instrument mass tolerance
rt_tol = 100;           % instrument retention time tolerance
noise_mult = 1;         % mass & RT noise added are this multiply of the mass & rt tolerances
num_metabolites = 10;   % how many metabolites per sample ?
num_clusters = 3;       % how many clusters per metabolite ?
num_peaks = 30;         % how many peaks per metabolite ?
mass_min = 0;           % min range for sampled mass
mass_max = 300;         % max range for sampled mass
cl_mean_min = 100;      % min range for sampled cluster mean
cl_mean_max = 500;      % max range for sampled cluster mean
cl_std = 5;             % fixed standard deviation for all cluster

% generate some metabolites, their RT clusters and their peaks
metabolites = generate_metabolites(num_metabolites, num_clusters, cl_mean_min, cl_mean_max, cl_std, num_peaks, mass_max, mass_min);

% pick some metabolites
% technical replicates all contain the same metabolites
% each peak have some probabilities of appearing in the replicate
selected_peaks = generate_peaks(metabolites);

% evaluate alignment
iter = 100;

use_clustering = false;
precs = [];
% fprintf('Without clustering info\n');
% fprintf('\tIter\tPrec\n');
figure;
for i = 1:iter

    % generate the first data
    data1 = selected_peaks;
    for j = 1:length(data1)
        data1(j) = add_noise(data1(j), mass_tol*noise_mult, rt_tol*noise_mult);
    end

    % generate the second data
    data2 = selected_peaks;
    for k = 1:length(data2)
        data2(k) = add_noise(data2(k), mass_tol*noise_mult, rt_tol*noise_mult);
    end

    prec = eval_combine(data1, data2, mass_tol, rt_tol, use_clustering);
    % fprintf('\t#%d\t%f\n', i, prec);
    precs = [precs; prec];

    hist(precs);
    title(['Without clustering info iter=' num2str(i)]); 
    xlabel('Precision');
    drawnow;

end

use_clustering = true;
clustering_precs = [];
% fprintf('With clustering info\n');
% fprintf('\tIter\tPrec\n');
figure;
for i = 1:iter

    % generate the first data
    data1 = selected_peaks;
    for j = 1:length(data1)
        data1(j) = add_noise(data1(j), mass_tol*noise_mult, rt_tol*noise_mult);
    end

    % generate the second data
    data2 = selected_peaks;
    for k = 1:length(data2)
        data2(k) = add_noise(data2(k), mass_tol*noise_mult, rt_tol*noise_mult);
    end

    prec = eval_combine(data1, data2, mass_tol, rt_tol, use_clustering);
    % fprintf('\t#%d\t%f\n', i, prec);
    clustering_precs = [clustering_precs; prec];

    hist(clustering_precs);
    title(['With clustering info iter=' num2str(i)]); 
    xlabel('Precision');
    drawnow;

end
