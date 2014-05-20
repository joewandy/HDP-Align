clear all; clc; close all;

%%% METABOLITE INFORMATION %

% the set of all metabolites and their formulae
name = {
    '4-Coumarate' '(R)-3-Hydroxybutanoate' 'Phthalate' 'Fumarate' 'Methylmalonate' '3_4-Dihydroxyphenylacetate' 'Thymidine' 'Phenolsulfonphthalein' 'Pyruvate' 'Malonate' 
};
F = {
    'C9H8O3' 'C4H8O3' 'C8H6O4' 'C4H4O4' 'C4H6O4' 'C8H8O4' 'C10H14N2O5' 'C19H14O5S' 'C3H4O3' 'C3H4O4'
};

%%% MODEL PARAMETERS %%%

S = 2;              % number of replicates to produce
prob_m = 1;         % probability of each metabolite to appear in replicate
prob_p = 0.7;       % probaiblity of each individiual observed peak
threshold_q = 500;  % threshold for filtering low intensity peaks

g = [1 1];          % warping scaling coeff in each replicate
h = [0 0];          % warping translation coeff in each replicate

a = 0.5;            % success probability of a metabolite to be selected (Bernoulli)
b = 10;             % mean of metabolite's concentration (log-Normal)
c = 1;              % standard deviation of metabolite concentration (log-Normal)

alpha = 1;          % DP concentration parameter for clustering
d = 1000;           % mean of predicted retention time of metabolite (Normal)
e = 1;              % standard deviation of predicted retention time of metabolite (Normal)

sigma_c = 1;        % standard deviation of cluster's RT (Normal)
sigma_q = 100;      % standard deviation of observed peak's intensity (Normal)
sigma_t = 1;        % standard deviation of observed peak's RT (Normal)
sigma_m = 0.03;     % standard deviation of observed peak's mass (Normal)

%%% PICK METABOLITES %%%

% pick the set of metabolites present in each replicate s
% M_s_all = [
%     1 1 1 1 1 1 1 1 1 1; 
%     1 1 1 1 1 1 1 1 1 1; 
% ];

% each metabolite has a uniform probability to appear in the replicate
num_meta = length(name);
for s = 1:S
    for m = 1:num_meta
        if rand > prob_m
            % not present in replicate
            M_s_all(s, m) = 0;
        else
            % present in replicate
            M_s_all(s, m) = 1;
        end
    end
end
M_s_all

% generate predicted retention time for each metabolite
R = normrnd(d, e, 1, length(F));

%%% GENERATE REPLICATES %%%

for s = 1:S

    % which metabolite in this replicate
    M_s = M_s_all(s, :);
    num_meta = sum(M_s);
    M_name = name(find(M_s));
    M_F = F(find(M_s));

    % set M_R, the predicted RT of m in this replicate
    M_R = R(find(M_s));
    
    % set C, the abundance / absolute intensity values of each metabolite in this replicate
    C = lognrnd(b, c, 1, sum(M_s));
    
    % generate isotopic distribution for each metabolite formula and adduct combination
    dist = importdata('isotopic_dist.csv');
    masses = importdata('isotopic_mass.csv');
    dist = dist';
    masses = masses';
    dist = dist(:, find(M_s));
    masses = masses(:, find(M_s));    

    % generate the theoretical isotopic peaks     
    theo_peaks = [];
    for m = 1:num_meta

        N = length(masses(:, m)); % number of isotopic peaks

        % create the assignment matrix of isotopic peaks to clusters
        Z = CRP(alpha, N); % assign peaks to clusters via CRP

        % get cluster's predicted RT after warping
        K = max(Z); % number of clustersZ(
        phi = zeros(1, K);
        lk = zeros(1, K);
        for k = 1:K
            phi(k) = m;
            % apply the RT mapping function        
            mapped_rt = (g(s)*M_R(m)) + h(s);
            % assign the cluster RT after mapping
            lk(k) = normrnd(mapped_rt, sigma_c);        
        end    
                
        % store isotopic peaks
        for n = 1:N
            k = Z(n);
            peak.mass = masses(n, m);
            peak.intense_ratio = dist(n, m);
            peak.cluster_rt = lk(k);
            peak.cluster = k;
            theo_peaks = [peak; theo_peaks];
        end
    
    end
                        
    x_n = [];
    q_n = [];
    t_n = [];
    % assign theoretical peaks to observed peaks
    for n = 1:length(theo_peaks)
        
        theo_peak = theo_peaks(n);
        qp = C(m)*theo_peak.intense_ratio;

        if qp > threshold_q & rand < prob_p

            % assign the intensity
            q_n = [q_n; normrnd(qp, sigma_q)];

            % assign the mass
            mp = theo_peak.mass;
            x_n = [x_n; normrnd(mp, sigma_m)];

            % assign the RT
            cluster_rt = theo_peak.cluster_rt;
            t_n = [t_n; normrnd(cluster_rt, sigma_t)];            
        
        end
                
    end
        
    disp([x_n, q_n, t_n])
    figure;
    scatter(t_n, x_n, log(q_n));
    xlabel('RT');
    ylabel('m/z');
    title('Features');
    
end


