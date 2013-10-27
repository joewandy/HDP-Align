clear all; clc;

% load A - hard clustering
% load 6-06-03_000.csv.Z.mat
% ZA = Z;
% A = ZA * ZA';

% load A - soft clustering
load 6-06-03_000.csv.ZZprob.mat
A = ZZprob;

% plot A
file1 = importdata('../6-06-03_000.csv');
rt1 = file1.data(:, 3);
[sorted_rt1, posA] = sort(rt1);
A_ordered = A(posA, posA);
% figure;
% imagesc(A_ordered); colorbar;
% title('A');

% load B - hard clustering
% load 6-17-03_000.csv.Z.mat
% ZB = Z;
% B = ZB * ZB';

% load B - soft clustering
load 6-17-03_000.csv.ZZprob.mat
B = ZZprob;

% plot B
file2 = importdata('../6-17-03_000.csv');
rt2 = file2.data(:, 3);
[sorted_rt2, posB] = sort(rt2);
B_ordered = B(posB, posB);
% figure;
% imagesc(B_ordered); colorbar;
% title('B');

% load score matrix W & Q
load WQ.mat
W_ordered = W(posA, posB);
Q_ordered = Q(posA, posB);
% figure;
% imagesc(W_ordered); colorbar;
% title('W');

% combine here
tic;
W = W ./ max(max(W)); % should be done in java already ..
A = A - diag(diag(A));
B = B - diag(diag(B));
% A = sparse(A);
% B = sparse(B);
% W = sparse(W);
% D = (A*W)*B;
D = A*W;
D = D*B;
D = Q .* D;
D = D ./ max(max(D));
toc;

alpha = 0.3;
Wp = (alpha .* W) + ((1-alpha) .* D);

% figure;
% imagesc(Wp); colorbar;
% title('Wp');

save('Wp.mat', 'Wp');
