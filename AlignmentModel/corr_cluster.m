function out = corr_cluster(Q,varargin)

NSAMPS = 100;
alpha = 1;

for i = 1:2:length(varargin)-1
    switch lower(varargin{i})
        case 'nsamps'
            NSAMPS = varargin{i+1};
        case 'alpha'
            alpha = varargin{i+1};
    end
end


N = size(Q,1);


p0in = 0.001;
p0out = 0.5;
ain = 8;bin = 1;
aout = 1;bout = 1;

Q(Q==1) = 1-1e-6;
Q(Q==-1) = -1+1e-6;

iszero = (Q==0);
isnonzero = (Q~=0);

qlikein = zeros(size(Q));
qlikeout = zeros(size(Q));
qlikein(iszero) = log(p0in);
qlikein(isnonzero) = log(1-p0in) + log(betapdf((Q(isnonzero)+1)/2,ain,bin));
qllikeout(iszero) = log(p0out);
qlikeout(isnonzero) = log(1-p0out) + log(betapdf((Q(isnonzero)+1)/2,aout,bout));

qlikein = sum(qlikein,3);
qlikeout = sum(qlikeout,3);

qlikeout = qlikeout.*(1-eye(N)); % Remove the diagonal component

K = 1;
Z = repmat(1,N,1);
Kall = zeros(NSAMPS,1);

ZZall = zeros(N);
allZ = zeros(N,NSAMPS);
for s = 1:NSAMPS
    order = randperm(N);
    for n = 1:N
        this = order(n);
        thisk = find(Z(this,:));
        Z(this,thisk) = 0;
        sZ = sum(Z,1);
        if sZ(thisk)==0
            K = K - 1;
            Z(:,thisk) = [];
            sZ(thisk) = [];
        end
        
        
        like = sum(repmat(qlikein(:,this),1,K).*Z,1) + sum(repmat(qlikeout(:,this),1,K).*(1-Z),1);
        like = [like sum(qlikeout(:,this))];
        
        post = like + log([sZ alpha]);
        
        post = exp(post - max(post));
        post = post./sum(post);
        
        newk = find(rand<=cumsum(post));
        newk = newk(1);
        
        if newk>K
            K = K + 1;
        end
        Z(this,newk) = 1;
    end
    zI = [];
    for k = 1:K
        zI = [zI;find(Z(:,k))];
    end
    % subplot(121)
    % imagesc(Z);
    % subplot(122)
    % imagesc(Q(zI,zI));drawnow
    % printf('.');
    Kall(s) = K;
    ZZall = ZZall + 1.0*Z*Z';
    [r,c] = find(Z);
    [r I] = sort(r,'ascend');
    allZ(:,s) = c(I);
end
out.allZ = allZ;
out.ZZall = ZZall ./ NSAMPS;

out.Z = Z;
out.Kall = Kall;

% Find the least squares clustering
out.bestsse = inf;
out.bestZ = [];
for s = 1:NSAMPS
    tempZ = full(sparse([1:N]',out.allZ(:,s),1));
    tempZZ = tempZ*tempZ';
    out.sse(s) = sum(sum((tempZZ - out.ZZall).^2));
    if out.sse(s) < out.bestsse
        out.bestsse = out.sse(s);
        out.bestZ = tempZ;
    end
end


