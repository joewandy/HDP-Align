X = importdata('plot.csv');
figure;
plot(X(:, 2), X(:, 1));
hold on
scatter([0.97], [0.97]);
ylim([0 1]);
xlim([0 1]);
