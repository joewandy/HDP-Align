function [Z, ZZall, ZZprob] = run_rt_clustering_func(filename)

X = importdata(filename);
h = figure;
scatter(X.data(:, 3), X.data(:, 2), log(X.data(:, 4)))
xlabel('RT');
ylabel('m/z');
title([filename ' - data']);
saveas(h, [filename '.data.png']);

data = X.data(:, 3);
h = figure;
plot(data)
title([filename ' - retention times'])
ylabel('RT (s)');
xlabel('RT');
xlabel('peaks');
saveas(h, [filename '.rt.png']);

[Z, ZZall, ZZprob] = rt_clustering_sampler_4(data, filename);
save('-v6', [filename '.mat']);
save('-v6', [filename '.Z.mat'], 'Z');
save('-v6', [filename '.ZZall.mat'], 'ZZall');
save('-v6', [filename '.ZZprob.mat'], 'ZZprob');
