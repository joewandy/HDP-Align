function gmm_dp_sampler(data, param_rtwindow, param_alpha, param_nsamps)

% graphics_toolkit ("fltk");
N = length(data);

%%% model parameters %%%
mu_zero = mean(data);  % mean hyper-parameter for mu
tau_zero = 5e-3; % precision hyper-parameter for mu
rt_tol = 1/param_rtwindow;  % fixed component precision
alpha = param_alpha; % DP concentration parameter

%%% sampling variables %%%
K = 3; % Initial number of clusters
Z = rand(N, K); % indicator for cluster membership
Z = (Z==repmat(max(Z,[],2), 1, K)); % turn to binary values
NSAMPS = param_nsamps; % no. of samples
cSums = sum(Z,1); % no. of members in each cluster
AllNk = zeros(NSAMPS, 1); % K for each sample
t1 = zeros(NSAMPS, 1); % time taken for each sample
scores = zeros(NSAMPS, 1);
ZZall = zeros(N);
allZ = zeros(N,NSAMPS);

% sampling starts here ..
h = figure;
for s = 1:NSAMPS

    tic;
    
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
        prior = [cSums alpha]; % --> infinite case
        % prior = [cSums + alpha/K]; % --> finite case 
        prior = prior./sum(prior);

        % for current k
        param_beta = tau_zero + (rt_tol*cSums);
        param_alpha = (1./param_beta).*((tau_zero*mu_zero) +  (rt_tol*sum(Z.*repmat(data,1,K),1)));

        % for new k
        last_param_beta = tau_zero;
        last_param_alpha = mu_zero;
        param_beta = [param_beta, last_param_beta];
        param_alpha = [param_alpha, last_param_alpha];
        
        % pick new k
        prec = 1./((1./param_beta)+(1/rt_tol));
        log_likelihood = -0.5*log(2*pi) + 0.5*log(prec) - 0.5*prec.*(this_data - param_alpha).^2;
       
        % sample from posterior
        post = log_likelihood + log(prior);
        post = exp(post - max(post));
        post = post./sum(post);
        newk = find(rand<=cumsum(post),1);
                
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
    subplot(121)
    imagesc(double(Z)); 
    xlabel('K'); 
    ylabel('N');
    title('Z');
    subplot(122)
    ZZall = ZZall + double(Z)*double(Z');
    imagesc(ZZall);
    title('ZZall');
    colorbar;
    drawnow;
    
    % store all the samples
    [r,c] = find(Z);
    [r I] = sort(r,'ascend');
    allZ(:,s) = c(I);
    
end % end sample

% Find the least squares clustering
bestsse = inf;
bestZ = [];
for s = 1:NSAMPS
    % convert 1D vector allZ(:, s) into a 2D matrix again
    tempZ = full(sparse([1:N]', allZ(:,s), 1));
    tempZZ = tempZ*tempZ';
    % compute SSE
    sse(s) = sum(sum((tempZZ - ZZall).^2));
    if sse(s) < bestsse
        bestsse = sse(s);
        bestZ = tempZ;
    end
end

Z = bestZ;

% dim = size(ZZall, 2);
% ZZprob = ZZall ./ repmat(max(ZZall, [], 2), 1, dim);
ZZprob = ZZall ./ NSAMPS;

% save('-v6', 'temp.mat');
save('-v6', 'temp.Z.mat', 'Z');
% save('-v6', 'temp.ZZall.mat', 'ZZall');
save('-v6', 'temp.ZZprob.mat', 'ZZprob');
