%% Generate alignment data
clear all;
% Create 20 pretend metabolites (all have 10 peaks)

% (note all +10 are to avoid -ve values)
masses = rand(10,20)*100 + 10; % Masses uniformly distributed between 0 and 100
intenses = rand(10,20)*100 + 10; % Same for intensities
rettime = rand(1,100)*100 + 100; % And RT

% Create a unique peak ID
pID = reshape(1:200,10,20); % We can use this to test

NFiles = 2; % Generate this many files

pmet = 1; % Assume probability of each metabolite is 1
ppeak = 0.7; % Probability of individual peaks


% Parameters to generate correlations
ain = 8;bin = 1;aout = 1;bout = 1;p0in = 0.1; p0out = 0.9;

rtstd0 = 5;
mstd = 3;
istd = 3;

prefix = 'synthdata1';

for f = 1:NFiles
    met = [];ID =[];
    mass = []; inte = []; rt = []; Q = [];
    for m = 1:20 % loop over metabolites
        if rand>pmet
            continue
        else
            rtall = rettime(m) + randn*rtstd0;
            for pk = 1:10 % loop over peaks
                if rand>ppeak
                    continue
                else
                    met(end+1,m) = 1;
                    mass = [mass;masses(pk,m) + randn*mstd];
                    inte = [inte;intenses(pk,m) + randn*istd];
                    ID = [ID;pID(pk,m)];
                    rt = [rt;rtall+randn];
                end
            end
        end
    end
    
    N = length(mass);
    if size(met,2)<20
        met = [met zeros(N,20-size(met,2))];
    end
    
    ZZ = met*met';
    
    % Generate Q
    for n = 1:N-1
        for m = n+1:N
            if ZZ(n,m)==1
                % Same cluster
                if rand<p0in
                    Q(n,m) = nan;
                    Q(m,n) = nan;
                else
                    tc = betarnd(ain,bin);
                    tc = tc*2-1;
                    Q(n,m) = tc;
                    Q(m,n) = tc;
                end
            else
                if rand<p0out
                    Q(n,m) = nan;
                    Q(m,n) = nan;
                else
                    tc = betarnd(aout,bout);
                    tc = tc*2 - 1;
                    Q(n,m) = tc;
                    Q(m,n) = tc;
                end
            end
        end
    end
    
    % Write out the file
    fname = [prefix '_' num2str(f) '.csv'];
    fout = fopen(fname,'w');
    
    for n = 1:N
        fprintf(fout,'%g,%g,%g',mass(n),inte(n),rt(n));
        fprintf(fout,',%g',Q(n,:));
        fprintf(fout,',%g\n',ID(n));
    end
    
    fclose(fout);
end


