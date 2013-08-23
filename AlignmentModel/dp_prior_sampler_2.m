%% More efficient implementation for Dirichlet Process Prior
clear all;
K = 10;
maxK = N;
Z = rand(N,K);
Z = (Z == repmat(max(Z,[],2),1,K));
Z = [Z zeros(N,maxK-K)];
% Z = sparse(Z);
[r,c] = find(Z);
clusind = sort(unique(c));
K = length(clusind);
alpha = 100;
cSums = sum(Z);
occupied = zeros(1,maxK);
occupied(clusind) = 1;
K = sum(occupied);
AllNk = zeros(NSAMPS,1);
t2 = zeros(NSAMPS,1);
for s = 1:NSAMPS
    tic
    order = randperm(N);
    for n = 1:N
        this = order(n);
        thisk = find(Z(this,:));
        Z(this,thisk) = 0;
        cSums(thisk) = cSums(thisk) - 1;
        if cSums(thisk) == 0
            % Add this cluster to the empty set
            occupied(thisk) = 0;
            K = K - 1;
        end
        prior = [cSums alpha];
        prior = prior./sum(prior);
        newk = find(rand<=cumsum(prior),1);
        if newk>maxK
            % This is a new cluster
            % find the first empty one
            pos = find(occupied==0,1);
            if isempty(pos) % This should only happen if maxK<N
                Z = [Z zeros(N,10)];
                occupied = [occupied zeros(1,10)];
                pos = find(occupied==0,1);
            end
            Z(this,pos) = 1;
            cSums(pos) = 1;
            occupied(pos) = 1;
            K = K + 1;
            
        else
            Z(this,newk) = 1;
            cSums(newk) = cSums(newk) + 1;
        end
        
    end
    t2(s) = toc;
    AllNk(s,1) = K;
    hist(AllNk);drawnow
end
