function a = hdp()
%% Hierarchical DP for Alignment
clear all;
close all;
R = 5; % The number of replicates
K = 0;
P = 0;

M = 10; % The number of metabolites
totP = 0;
qall = [];
mind = [];
for m = 1:M
    nP = poissrnd(10);
    q{m} = repmat(0.8,nP,1);
    totP = totP + nP;
    qall = [qall;q{m}];
    mind = [mind;repmat(m,nP,1)];
end
P = length(qall);
x = zeros(P,R);
for r = 1:R
    x(:,r) = rand(size(qall))<qall;
end


% Translate to other format
ain = 5;bin = 1;
aout = 1;bout = 5;
% Create the Q matrices
Q = zeros(totP,totP,R);
for r = 1:R
    Q(:,:,r) = Q(:,:,r) + nan*eye(totP);
    for i = 1:totP-1
        for j = i+1:totP
            
            if x(i,r)==1 & x(j,r)==1
                if mind(i)==mind(j)
                    Q(i,j,r) = betarnd(ain,bin);
                    Q(j,i,r) = Q(i,j,r);
                else
                    Q(i,j,r) = betarnd(aout,bout);
                    Q(j,i,r) = Q(i,j,r);
                end
            else
                Q(i,j,r) = nan;
                Q(j,i,r) = nan;
            end
        end
    end
end


qlikein = zeros(size(Q));
qlikeout = zeros(size(Q));
qlikein(~isnan(Q)) = log(betapdf(Q(~isnan(Q)),ain,bin));
qlikeout(~isnan(Q)) = log(betapdf(Q(~isnan(Q)),aout,bout));

%% Run the HDP - intialise with everything in one cluster
for r = 1:R
    Z{r} = x(:,r);
    topZ{r} = 1; % This will be T(r) x K
    N(r) = sum(x(:,r));
end
K = 1;
NSAMPS = 100;
alpha = 1;beta = 1;newtab = 1;gamma = 1;
for s = 1:NSAMPS
    reporder = randperm(R);
    for r = 1:R
        thisr = reporder(r);
        npos = find(x(:,thisr));
        for n = 1:length(npos)
            this = npos(n);
            thist = find(Z{thisr}(this,:));
            thisk = find(topZ{thisr}(thist,:));
            Z{thisr}(this,:) = 0;
            tsum = sum(Z{thisr},1);
            if tsum(thist)==0
                % Empty table - delete it
                Z{thisr}(:,thist) = [];
                tsum(thist) = [];
                topZ{thisr}(thist,:) = [];
                sumtopZ = zeros(1,K);
                for r = 1:R
                    sumtopZ = sumtopZ + sum(topZ{r},1);
                end
                if sumtopZ(thisk) == 0
                    % Empty top component, delete it
                    K = K - 1;
                    for r = 1:R
                        topZ{r}(:,thisk) = [];
                    end
                end
            end
            
            
            instcountk = zeros(1,K);
            tabcountk = zeros(1,K);
            for r = 1:R
                tabcountk = tabcountk + sum(topZ{r},1);
                instcountk = instcountk + Z{r}(this,:)*topZ{r};
            end
            
            alphastar = alpha + instcountk;
            betastar = beta + tabcountk - instcountk;
            KLike = alphastar./(alphastar + betastar);
            
            nTab = size(topZ{thisr},1);
            tabLike = zeros(nTab+1,1);
            for t = 1:nTab
                tabLike(t) = log(sum(Z{thisr}(:,t))) + log(KLike(find(topZ{thisr}(t,:))));
                in = Z{thisr}(:,t)==1;
                out = x(:,thisr)==1 & Z{thisr}(:,t)==0;
                out(this) = 0;
                tabLike(t) = tabLike(t) + sum(qlikein(this,in,thisr)) + sum(qlikeout(this,out,thisr));
            end
            KLikenew = alpha/(alpha+beta);
            
            margthing = [tabcountk.*KLike gamma.*KLikenew];
            margthing = margthing./(sum(tabcountk + gamma));
            
            tabLike(end) = log(newtab) + log(sum(margthing));
            out = x(:,thisr)==1;
            out(this) = 0;
            tabLike(end) = tabLike(end) + sum(qlikeout(this,out,thisr));
            
            post = exp(tabLike - max(tabLike));
            post = post./sum(post);
            
            pos = find(rand<=cumsum(post));
            pos = pos(1);
            if pos<=nTab
                % Current table
                Z{thisr}(this,pos) = 1;
            else
                % New table
                Z{thisr}(this,end+1) = 1;
                % Assign to a k
                post = margthing./sum(margthing);
                pos = find(rand<=cumsum(post));
                pos = pos(1);
                if pos<=K
                    topZ{thisr}(end+1,pos) = 1;
                else
                    for r = 1:R
                        topZ{r}(:,end+1) = 0;
                    end
                    topZ{thisr}(end+1,end) = 1;
                    
                    K = K + 1;
                end
            end
            
        end
        subplot(2,R,thisr);imagesc(Z{thisr});
        subplot(2,R,thisr+R);imagesc(Z{thisr}*topZ{thisr});drawnow
    end
    
    % Re-sample assignments of whole tables
    ksum = zeros(1,K);
    for r = 1:R
        ksum = ksum + sum(topZ{r},1);
    end
    featsums = zeros(totP,K);
    for r = 1:R
        featsums = featsums + Z{r}*topZ{r};
    end
    
    for thisr = 1:R
        for t = 1:size(topZ{thisr},1)
           [sum(sum(x)) sum(sum(featsums))]
           tempx = Z{thisr}(:,t);
           % Un-assign the table
           thisk = find(topZ{thisr}(t,:));
           topZ{thisr}(t,:) = 0;
           ksum(thisk) = ksum(thisk) - 1;
           featsums(:,thisk) = featsums(:,thisk) - tempx;
           if ksum(thisk) == 0
                % Delete it
                K = K - 1;
                for r = 1:R
                    topZ{r}(:,thisk) = [];
                end
                ksum(thisk) = [];
                featsums(:,thisk) = [];
           end
           
           % Compute the likelihood under each k
           Like = zeros(1,K+1);
           alphastar = alpha + featsums;
           betastar = beta + (R-1) - featsums;
           
           Like(1:K) = sum(log((repmat(tempx,1,K).*alphastar + repmat(1-tempx,1,K).*betastar)./(alphastar + betastar)),1);
           Like(1:K) = Like(1:K) + log(ksum);
           Like(end) = sum(tempx)*log(alpha/(alpha+beta)) + sum(1-tempx)*log(beta/(alpha+beta));
           Like(end) = Like(end) + log(gamma);
           
           post = exp(Like - max(Like));
           post = post./sum(post);
           
           pos = find(rand<=cumsum(post));
           
           pos = pos(1);
           
           if pos<=K
               topZ{thisr}(t,pos) = 1;
               ksum(pos) = ksum(pos) + 1;
               featsums(:,pos) = featsums(:,pos) + tempx;
           else
               K = K + 1;
               ksum = [ksum 1];
               for r = 1:R
                   topZ{r}(:,end+1) = 0;
               end
               topZ{thisr}(t,end) = 1;
               featsums(:,end+1) = tempx;
           end
        end
        subplot(2,R,thisr);imagesc(Z{thisr});
        subplot(2,R,thisr+R);imagesc(Z{thisr}*topZ{thisr});drawnow
    end
    
end
a = 1;
