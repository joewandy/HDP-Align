Np = 100; % number of peaks
alpha = 10; % DP concentration parameter

pos = CRP(alpha, Np); % assign peaks to tables via CRP
Nm = max(pos) % number of metabolites

% create the assignment matrix of metabolites to peaks
S = sparse(Nm, Np);
for i = 1:Np
    j = pos(i);
    S(j, i) = 1;
end

% generate theoretical masses for each metabolite formula


