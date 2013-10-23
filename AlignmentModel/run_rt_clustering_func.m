function [Z, ZZall, ZZprob] = run_rt_clustering_func(filename)

X = importdata(filename);
data = X.data(:, 3);

% h = figure;
% scatter(X.data(:, 3), X.data(:, 2), log(X.data(:, 4)))
% xlabel('RT');
% ylabel('m/z');
% title([filename ' - data']);
% saveas(h, [filename '.data.png']);

% h = figure;
% plot(data)
% title([filename ' - retention times'])
% ylabel('RT (s)');
% xlabel('RT');
% xlabel('peaks');
% saveas(h, [filename '.rt.png']);

rt_tol = 0.05;
alpha = 10;
NSAMPS = 10; % no. of samples
[Z, ZZprob] = gmm_dp_sampler(data, rt_tol, alpha, NSAMPS);

save('-v6', [filename '.mat']);
% save('-v6', [filename '.Z.mat'], 'Z');
% save('-v6', [filename '.ZZprob.mat'], 'ZZprob');
