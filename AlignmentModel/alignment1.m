%% Try an inefficient alignment algorithm
clear all;close all;
%% Generate some data
R = 5; % Replicates
K = 6; % Clusters
for r = 1:R
    observed{r} = [];
    x{r} = [];
    Q{r} = [];
    trueID{r} = [];
    kID{r} = [];
end
kappa = 10;
thisID = 0;
for k = 1:K
    nMasses = poissrnd(5);
    vals = rand(nMasses,1)*100; % Uniform from 0 to 100
    for v = 1:nMasses
        thisID = thisID + 1;
        for r = 1:R
            if rand<=0.9
                x{r} = [x{r};vals(v) + randn/sqrt(kappa)];
                trueID{r} = [trueID{r};thisID];
                kID{r} = [kID{r};k];
            end
        end
    end        
end

ain = 3; bin = 1;
aout = 1; bout = 1;

for r = 1:R
    N = size(x{r},1);
    Q{r} = zeros(N);
    for n = 1:N-1
        for m = n+1:N
            if kID{r}(n)==kID{r}(m)
                Q{r}(n,m) = betarnd(ain,bin);
                Q{r}(m,n) = Q{r}(n,m);
            else
                Q{r}(n,m) = betarnd(aout,bout);
                Q{r}(m,n) = Q{r}(n,m);
            end
        end
    end
end


%% Run the sampler
% Initialise everything into random clusters and single positions
mu0 = 50;
kappa0 = 1e-6;
K = 3;
ain = 3;bin = 1;
aout = 1;bout = 1;
clear N
for r = 1:R
    N{r} = size(x{r},1);
    Z{r} = rand(N{r},K);
    Z{r} = (Z{r} == repmat(max(Z{r},[],2),1,K));
    Zposn{r} = repmat(1,N{r},1);
end

NSAMPS = 100;

subZ = [];
for k = 1:K
    subZ{k}.ID = 1;
    subZ{k}.sumy = sum(x{1}(find(Z{1}(:,k))));
    subZ{k}.N = sum(Z{1}(:,k));
    for r = 2:R
        subZ{k}.sumy = subZ{k}.sumy + sum(x{r}(find(Z{r}(:,k))));
        subZ{k}.N = subZ{k}.N + sum(Z{r}(:,k));
    end
end



for r = 1:R
    qlikein{r} = log(betapdf(Q{r},ain,bin));
    qlikeout{r} = log(betapdf(Q{r},aout,bout));
    qlikein{r}(isinf(qlikein{r})) = 1;
    qlikeout{r}(isinf(qlikeout{r})) = 1;
end
alpha = 1; % DP parameter
for s = 1:NSAMPS
    rorder = randperm(R);
    for r = 1:R
        thisr = rorder(r);
        order = randperm(N{thisr});
        for n = 1:N{thisr}
            this = order(n);
            thisk = find(Z{thisr}(this,:));
            Z{thisr}(this,:) = 0;
            thispos = find(subZ{thisk}.ID == Zposn{thisr}(this));
            Zposn{thisr}(this) = 0;
            subZ{thisk}.sumy(thispos) = subZ{thisk}.sumy(thispos) - x{thisr}(this);
            subZ{thisk}.N(thispos) = subZ{thisk}.N(thispos) - 1;
            e = checksumy(Z,x,subZ,Zposn);
            di = abs(e(:,1)-e(:,2));
            if any(di>1e-6)
                a = 1;
                keyboard
            end
            if subZ{thisk}.N(thispos) == 0
                subZ{thisk}.N(thispos) = [];
                subZ{thisk}.ID(thispos) = [];
                subZ{thisk}.sumy(thispos) = [];
            end
            e = checksumy(Z,x,subZ,Zposn);
            di = abs(e(:,1)-e(:,2));
            if any(di>1e-6)
                a = 2;
                keyboard
            end
            sZ = sum(Z{1},1);
            for ri = 2:R
                sZ = sZ + sum(Z{ri},1);
            end
            
            if sZ(thisk) == 0
                % Empty cluster, remove
                K = K - 1;
                for ri = 1:R
                    Z{ri}(:,thisk) = [];
                end
                sZ(thisk) = [];
                subZ(thisk) = [];
            end
            
            
            
            
            % Compute likelihoods
            Like = zeros(K+1,1);
            qlike = zeros(K+1,1);
            sublike = [];
            for k = 1:K
                in = (Z{thisr}(:,k));
                out = (Z{thisr}(:,k)==0);
                out(this) = 0;
                qlike(k) = sum(qlikein{thisr}(this,in)) + sum(qlikeout{thisr}(this,out));
                Like(k) = sum(qlikein{thisr}(this,in)) + sum(qlikeout{thisr}(this,out));
                sublike{k} = [];
                for i = 1:length(subZ{k}.ID)
                    prec = kappa0 + kappa*subZ{k}.N(i);
                    mu = (1/prec)*(kappa0*mu0 + kappa*subZ{k}.sumy(i));
                    sublike{k}(i) = loggausspdf(x{thisr}(this),mu,1/((1/prec) + (1/kappa)));
                end
                
                sublike{k}(i+1) = loggausspdf(x{thisr}(this),mu0,1/((1/kappa) + (1/kappa0)));
                Like(k) = Like(k) + log(mean(exp(sublike{k})));
            end
            
            Like(end) = sum(qlikeout{thisr}(this,:)) - qlikeout{thisr}(this,this);
            Like(end) = Like(end) + loggausspdf(x{thisr}(this),mu0,1/((1/kappa) + (1/kappa0)));
            
            Like = Like + log([sZ';alpha]);
            
            post = exp(Like - max(Like));
            post = post./sum(post);
            pos = find(rand<=cumsum(post));
            pos = pos(1);
            if pos<=K
                % Current cluster
                thisk = pos;
                Z{thisr}(this,thisk) = 1;
                % Assign to a sub
                post = sublike{thisk};
                post = exp(post - max(post));
                post = post./sum(post);
                pos = find(rand<=cumsum(post));
                pos = pos(1);
                if pos<=length(subZ{thisk}.ID)
                    % Current position
                    Zposn{thisr}(this) = subZ{thisk}.ID(pos);
                    subZ{thisk}.N(pos) = subZ{thisk}.N(pos) + 1;
                    subZ{thisk}.sumy(pos) = subZ{thisk}.sumy(pos) + x{thisr}(this);
                    e = checksumy(Z,x,subZ,Zposn);
            di = abs(e(:,1)-e(:,2));
            if any(di>1e-6)
                a = 3;
                keyboard
            end
                else
                    % It's a new position
                    newID = max(subZ{thisk}.ID) + 1;
                    Zposn{thisr}(this) = newID;
                    subZ{thisk}.ID(end+1) = newID;
                    subZ{thisk}.N(end+1) = 1;
                    subZ{thisk}.sumy(end+1) = x{thisr}(this);
                    e = checksumy(Z,x,subZ,Zposn);
            di = abs(e(:,1)-e(:,2));
            if any(di>1e-6)
                a = 4;
                keyboard
            end
                end
            else
                % Make a new one
                K = K + 1;
                for ri = 1:R
                    Z{ri}(:,K) = 0;
                end
                Z{thisr}(this,K) = 1;
                % Make a new ID
                newID = 1;
                Zposn{thisr}(this) = newID;
                subZ{K}.ID = newID;
                subZ{K}.N = 1;
                subZ{K}.sumy = x{thisr}(this);
                e = checksumy(Z,x,subZ,Zposn);
            di = abs(e(:,1)-e(:,2));
            if any(di>1e-6)
                a = 5;
                keyboard
            end
            end
        end
    end
    figure(1);
    for r = 1:R
        subplot(1,R,r);imagesc(Z{r});
    end
    drawnow
%     figure(2);
%     for k = 1:K
%         cols = {'r','g','b'};
%         subplot(2,ceil(K/2),k)
%         hold off
%         for r = 1:R
%             pos = find(Z{r}(:,k));
%             for p = 1:length(pos)
%                 plot(repmat(x{r}(pos(p)),1,2),[0 1],cols{r});
%                 hold on
%             end
%         end
%     end
end