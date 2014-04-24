clear all; clc; close all;

% the set of all metabolites and their formulae
name = {
    '4-Coumarate' '(R)-3-Hydroxybutanoate' 'Phthalate' 'Fumarate' 'Methylmalonate' '3_4-Dihydroxyphenylacetate' 'Thymidine' 'Phenolsulfonphthalein' 'Pyruvate' 'Malonate' 
};
F = {
    'C9H8O3' 'C4H8O3' 'C8H6O4' 'C4H4O4' 'C4H6O4' 'C8H8O4' 'C10H14N2O5' 'C19H14O5S' 'C3H4O3' 'C3H4O4'
};

% generate predicted retention time for each metabolite
a = 1000;
b = 100;
R = normrnd(a, b, 1, length(F));

S = 2; % number of replicates to produce
alpha = 10; % DP concentration parameter
N_s = [100 300]; % number of peaks in each replicate

% the set of metabolites present in each replicate s
M_s_all = [
    1 0 1 1 1 1 1 1 0 1; 
    1 0 1 1 1 1 0 1 0 1
];
for s = 1:S

    % which metabolite in this replicate
    M_s = M_s_all(s, :);
    M_name = name(find(M_s));
    M_F = F(find(M_s));
    M_R = R(find(M_s));
    
    % set C_s, the abundance / absolute intensity of m
    d = 10;
    e = 1;
    C = lognrnd(d, e, 1, sum(M_s));

    % create the assignment matrix of peaks to clusters    
    N = N_s(s);
    Z = CRP(alpha, N); % assign peaks to clusters via CRP
    K = max(Z); % number of clusters
    pos = sparse(N, K);
    for n = 1:N
        k = Z(n);
        pos(n, k) = 1;
    end
    figure;
    spy(full(pos));
    title('Z');
    xlabel('k');
    ylabel('n');
    
    % assign cluster to its parent metabolite
    phi = zeros(1, K);
    lk = zeros(1, K);
    sigma_c = 100;
    for k = 1:K
        m = randsample(1:length(M_name), 1);
        phi(k) = m;
        % apply the RT mapping function        
        g = 2;
        h = 10;
        mapped_rt = (g*M_R(m)) + h;
        % assign the cluster RT after mapping
        lk(k) = normrnd(mapped_rt, sigma_c);        
    end    
    
    % assign each peak the retention time from cluster
    sigma_t = 100;
    t_n = zeros(1, N);
    for n = 1:N
        k = Z(n);
        cluster_rt = lk(k);
        t_n(n) = normrnd(cluster_rt, sigma_t);
    end
    
    % generate isotopic distribution for each metabolite formula and adduct combination
    dist = importdata('isotopic_dist.csv');
    masses = importdata('isotopic_mass.csv');
    dist = dist';
    masses = masses';
    dist = dist(:, find(M_s));
    masses = masses(:, find(M_s));    
    
    % assign theoretical peaks to observed peaks
    x_n = zeros(1, N);
    q_n = zeros(1, N);
    sigma_m = 0.03;
    sigma_q = 1;
    for n = 1:N
        % find parent cluster and metabolite
        k = Z(n);
        m = phi(k);
        % pick a theoretical peak randomly
        theo_peak = randsample(1:length(masses(:, m)), 1);
        mp_mai = masses(theo_peak, m);
        rho_mai = dist(theo_peak, m);
        % assign the mass
        x_n(n) = normrnd(mp_mai, sigma_m);
        % assign the intensity
        q_n(n) = normrnd(C(m)*rho_mai, sigma_q);
    end
    
    disp([x_n', q_n', t_n'])

end


