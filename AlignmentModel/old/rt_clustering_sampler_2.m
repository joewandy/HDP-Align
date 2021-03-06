%%% Example Dirichlet Process prior
clear all;
 
%%% setup some fake data %%%
col = 20;
data = normrnd(0, 5, col, 1);
data = [data; normrnd(50, 5, col, 1)];
data = [data; normrnd(100, 5, col, 1)];
data = [data; normrnd(150, 5, col, 1)];
data = [data; normrnd(200, 5, col, 1)];

data = randn(50,1);
data = [data;randn(50,1) + 5];

N = length(data);

%%% sampling variables %%%
K = 2; % Initial number of clusters
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
rt_tol = 1; % fixed component precision
mus = normrnd(mu_zero, sqrt(1/tau_zero),1, K); % randomly initialise the component means
alpha = 10; % DP concentration parameter

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
%         if cSums(this_k) == 0
%             K = K - 1;
%             Z(:, this_k) = [];
%             cSums(this_k) = [];
%             mus(this_k) = [];
%         end
        
        % compute prior probability for K existing table and new table
        % n_k/(N+alpha) or alpha/(N+alpha)
%         prior = [cSums alpha];
        prior = [cSums + alpha/K]; % --> finite case only
        prior = prior./sum(prior);
        
        % pick new k
        % newk = find(rand<=cumsum(prior), 1);
        %log_likelihood = log(normpdf(this_data, mu_zero, 1/rt_tol + 1/tau_zero));
        
%         log_likelihood = log(normpdf(this_data, mus, sqrt(1/rt_tol)))
        prec = 1/(1/rt_tol);
        log_likelihood = -0.5*log(2*pi) + 0.5*log(rt_tol) - 0.5*rt_tol*(this_data - mus).^2;
        
%         scores(s) = scores(s) + log_likelihood;
        post = log_likelihood + log(prior);
        post = exp(post - max(post));
        post = post./sum(post);
        newk = find(rand<=cumsum(post),1);
%         [max_val, newk] = max(post); % -- sample, not just pick the max
                
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

    % param_beta = tau_zero + (rt_tol*sum(sum(Z)));
    param_beta = tau_zero + (rt_tol*sum(Z,1));

    % param_alpha = 1/param_beta * ( (tau_zero*mu_zero) +  (rt_tol*sum(sum(Z, 2).*data)) );
    param_alpha = (1./param_beta).*((tau_zero*mu_zero) +  (rt_tol*sum(Z.*repmat(data,1,K),1)));

    % mus = normrnd(param_alpha, 1/param_beta, K, 1);    
    mus = normrnd(param_alpha, sqrt(1./param_beta));
    
    All_mus(s,:) = mus;
    
    t1(s) = toc;
    AllNk(s,1) = K;
    % hist(AllNk, 100); drawnow;
    imagesc(double(Z)); 
    xlabel('K'); 
    ylabel('N');
    drawnow;
    
end % end sample
% plot(scores);
