%%% Example Dirichlet Process prior
clear all;
 
%%% setup some fake data %%%
col = 20;
data = normrnd(0, 5, col, 1);
data = [data; normrnd(50, 5, col, 1)];
data = [data; normrnd(100, 5, col, 1)];
data = [data; normrnd(150, 5, col, 1)];
data = [data; normrnd(200, 5, col, 1)];
N = length(data);

%%% sampling variables %%%
K = 5; % Initial number of clusters
Z = rand(N, K); % indicator for cluster membership
Z = (Z==repmat(max(Z,[],2), 1, K)); % turn to binary values
NSAMPS = 100; % no. of samples
cSums = sum(Z,1); % no. of members in each cluster
AllNk = zeros(NSAMPS, 1); % K for each sample
t1 = zeros(NSAMPS, 1); % time taken for each sample
scores = zeros(NSAMPS, 1);

%%% model parameters %%%
mu_zero = 5; % mean hyper-parameter for mu
tau_zero = 0.05; % precision hyper-parameter for mu
rt_tol = 0.05; % fixed component precision
mus = normrnd(mu_zero, 1/tau_zero, K, 1); % randomly initialise the component means
alpha = 100; % DP concentration parameter

% sampling starts here ..
for s = 1:NSAMPS

    tic
    
    % loop through the objects in random order
    order = randperm(N); 
    for n = 1:N
    
        % this is the currently selected object from the random permutation
        this = order(n);
        this_k = find(Z(this, :)); % which cluster this object currently belong to ?
        this_data = data(this);

        % remove from model, detecting empty table if necessary
        cSums(this_k) = cSums(this_k) - 1;
        Z(this, this_k) = 0;
        
        % if empty table, delete this cluster
        if cSums(this_k) == 0
            K = K - 1;
            Z(:, this_k) = [];
            cSums(this_k) = [];
        end
        
        % compute prior probability for K existing table and new table
        % n_k/(N+alpha) or alpha/(N+alpha)
        prior = [cSums alpha];
        prior = prior./sum(prior);
        
        % pick new k
        % newk = find(rand<=cumsum(prior), 1);
        log_likelihood = log(normpdf(this_data, mu_zero, 1/rt_tol + 1/tau_zero));
        scores(s) = scores(s) + log_likelihood;
        post = log_likelihood + log(prior);
        [max_val, newk] = max(post);
                
        if newk > K
            % make new cluster and add to it
            Z = [Z zeros(N, 1)];
            Z(this, newk) = 1;
            K = K + 1;
            cSums = [cSums 1];
        else
            % add to existing cluster
            Z(this, newk) = 1;
            cSums(newk) = cSums(newk) + 1;
        end
        
    end
    
    % sample all the mus again
    param_beta = tau_zero + (rt_tol*sum(sum(Z)));
    param_alpha = 1/param_beta * ( (tau_zero*mu_zero) +  (rt_tol*sum(sum(Z, 2).*data)) );
    mus = normrnd(param_alpha, 1/param_beta, K, 1);
    
    t1(s) = toc;
    AllNk(s,1) = K;
    % hist(AllNk, 100); drawnow;
    imagesc(Z); 
    xlabel('K'); 
    ylabel('N');
    drawnow;
    
end % end sample
plot(scores);
