%%% Example Dirichlet Process prior
clear all;
 
%%% setup some fake data %%%
data = randn(50,1);
data = [data;randn(50,1) + 10];
data = [data;randn(50,1) + 20];
data = [data;randn(50,1) + 30];
data = [data;randn(50,1) + 40];
data = [data;randn(50,1) + 50];
data = [data;randn(50,1) + 60];
data = [data;randn(50,1) + 70];
data = [data;randn(50,1) + 80];
data = [data;randn(50,1) + 90];
data = [data;randn(50,1) + 100];
data = [data;randn(50,1) + 110];

N = length(data);

%%% sampling variables %%%
K = 3; % Initial number of clusters
Z = rand(N, K); % indicator for cluster membership
Z = (Z==repmat(max(Z,[],2), 1, K)); % turn to binary values
NSAMPS = 1000; % no. of samples
cSums = sum(Z,1); % no. of members in each cluster
AllNk = zeros(NSAMPS, 1); % K for each sample
t1 = zeros(NSAMPS, 1); % time taken for each sample
scores = zeros(NSAMPS, 1);

%%% model parameters %%%
mu_zero = 5; % mean hyper-parameter for mu
tau_zero = 0.05; % precision hyper-parameter for mu
rt_tol = 0.1; % fixed component precision
% mus = normrnd(mu_zero, sqrt(1/tau_zero),1, K); % randomly initialise the component means
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
        if cSums(this_k) == 0
            K = K - 1;
            Z(:, this_k) = [];
            cSums(this_k) = [];
%             mus(this_k) = [];
        end
        
        % compute prior probability for K existing table and new table
        % n_k/(N+alpha) or alpha/(N+alpha)
        prior = [cSums alpha]; % --> infinite case
%       prior = [cSums + alpha/K]; % --> finite case 
        prior = prior./sum(prior);

        % for current k
        param_beta = tau_zero + (rt_tol*cSums);
        param_alpha = (1./param_beta).*((tau_zero*mu_zero) +  (rt_tol*sum(Z.*repmat(data,1,K),1)));

        % for new k
        last_param_beta = tau_zero;
        last_param_alpha = (1/last_param_beta) * (tau_zero*mu_zero);
        param_beta = [param_beta, last_param_beta];
        param_alpha = [param_alpha, last_param_alpha];
        
        % pick new k
%       prec = 1/(1/rt_tol);
%       log_likelihood = -0.5*log(2*pi) + 0.5*log(rt_tol) - 0.5*rt_tol*(this_data - mus).^2;

        sigma = sqrt(1./(param_beta+rt_tol));
        log_likelihood = -0.5*log(2*pi) + 0.5*log(sigma) - 0.5*sigma.*(this_data - param_alpha).^2;
       
        post = log_likelihood + log(prior);
        post = exp(post - max(post));
        post = post./sum(post);
        newk = find(rand<=cumsum(post),1);
%       [max_val, newk] = max(post); % -- sample from posterior, not just pick the max
                
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
    % param_beta = tau_zero + (rt_tol*sum(Z,1));
    % param_alpha = (1./param_beta).*((tau_zero*mu_zero) +  (rt_tol*sum(Z.*repmat(data,1,K),1)));
    % mus = normrnd(param_alpha, sqrt(1./param_beta));
    % All_mus(s,:) = mus;
    
    t1(s) = toc;
    AllNk(s,1) = K;
    % hist(AllNk, 100); drawnow;
    imagesc(double(Z)); 
    xlabel('K'); 
    ylabel('N');
    drawnow;
    
end % end sample
% plot(scores);
