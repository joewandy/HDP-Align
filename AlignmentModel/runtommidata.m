%% Run clusterer for Tommi
clear all;close all;

names = {'184MC_ACACA_1_Ah_vtt22run1_20011001.CDF.csv','184MC_ACACA_2_Ah_vtt13run1_20011001.CDF.csv','184MC_ACACA_3_Ah_vtt26run1_20011001.CDF.csv'};

for n = 1:1:length(names)

X = importdata(names{n});

% Removing missings
r = find(X(1,:)==-99);
X(:,r) = [];
X(r,:) = [];

Qs = X;
Qs(isnan(X)) = 0;

% Just take one sample

% for i = 1:30
% Temp = Q(i,:,:);
% Temp = reshape(Temp,105,105);
% 
% Temp(isnan(Temp)) = 0;
% Qs(:,:,i) = Temp;
% end

out = corr_cluster(Qs,'nsamps',5);

I = [];
sZ = sum(out.Z,1);
[sZ,I] = sort(sZ,'descend');
out.Z = out.Z(:,I);
I = [];
for k = 1:size(out.Z,2)
    I = [I;find(out.Z(:,k))];
end

close all;
figure(1);
imagesc(Qs(I,I));
colorbar
print('-djpeg',[names{n} '.jpg']);
end