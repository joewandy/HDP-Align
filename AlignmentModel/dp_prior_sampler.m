%% Example Dirichlet Process prior
clear all;
 
% Number of 'data' objects - note that they don't have any data in them...
N = 1000;

% Initial number of clusters
K = 10; 

% matrix of size 1000 x 10
Z = rand(N,K); 

% binary indicator matrix of size 1000 x 10 for cluster membership
Z = (Z==repmat(max(Z,[],2),1,K)); 

% no. of samples
NSAMPS = 200;

% no. of members in each cluster
cSums = sum(Z,1); 

% DP concentra  tion parameter
alpha = 100;

% K for each sample
AllNk = zeros(NSAMPS,1);

% time for each sample
t1 = zeros(NSAMPS,1);

for s = 1:NSAMPS

    tic
    
    % loop through the objects in random order
    order = randperm(N); 
    for n = 1:N
    
        % this is the currently selected object from the random permutation
        this = order(n);
        thisk = find(Z(this, :)); % which cluster this object currently belong to ?

        % remove from model, detecting empty table if necessary
        cSums(thisk) = cSums(thisk) - 1;
        Z(this, thisk) = 0;
        
        % if empty table, delete this cluster
        if cSums(thisk) == 0
            K = K - 1;
            Z(:, thisk) = [];
            cSums(thisk) = [];
        end
        
        % compute probability for K existing table and new table
        % n_k/(N+alpha) or alpha/(N+alpha)
        prior = [cSums alpha];
        prior = prior./sum(prior);
        
        % pick new k randomly
        newk = find(rand<=cumsum(prior), 1);
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
    
    t1(s) = toc;
    AllNk(s,1) = K;
    hist(AllNk, 100);drawnow
    % spy(Z); drawnow
    
end
