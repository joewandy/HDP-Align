function hdp = init_hdp(input_hdp)

    data = input_hdp.data;

    hdp.gtI = input_hdp.I;          % for debugging
    
    hdp.J = size(data, 2);          % number of replicates
    hdp.NSAMPS = 100;              % number of samples

    % hdp.mu_0 = mean(mean(data));  % base distribution mean
    hdp.mu_0 = 0;                   % base distribution mean
    hdp.sigma_0_prec = 1/1000;      % base distribution prec

    hdp.top_alpha = 5;              % metabolite DP concentration param    
    hdp.alpha = 1;                  % cluster DP concentration param

    hdp.gamma_prec = 1;             % precision for cluster-level Gaussian
    hdp.delta_prec = 1;             % precision for metabolite-level Gaussian

    hdp.I = 1;                      % initial number of metabolites
    hdp.fi = zeros(1, hdp.I);       % 1 x I, number of clusters assigned to metabolite i

    % sample initial metabolite RT    
    hdp.ti = normrnd(hdp.mu_0, sqrt(1/hdp.sigma_0_prec), 1, hdp.I); % 1 x I, the metabolite's RT

    for j = 1:hdp.J

        hdp.file{j}.K = 1;                                  % number of clusters in this file
        hdp.file{j}.data = data(:, j);
        hdp.file{j}.N = length(hdp.file{j}.data);
        
        hdp.file{j}.ground_truth = input_hdp.file{j}.ground_truth; % for debugging

        % K x I, clusters to metabolites assignment
        top_Z = rand(hdp.file{j}.K, hdp.I);
        top_Z = (top_Z==repmat(max(top_Z, [], 2), 1, hdp.I));
        hdp.file{j}.top_Z = top_Z;
        
        % N x K, peaks to clusters assignment
        Z = rand(hdp.file{j}.N, hdp.file{j}.K);             % just randomly assign to start with
        Z = (Z==repmat(max(Z, [], 2), 1, hdp.file{j}.K));   % turn to binary values
        hdp.file{j}.Z = Z;
                                    
        % 1 x K, sample initial the clusters' RT
        for k=hdp.file{j}.K
            parent_i = find(hdp.file{j}.top_Z(k, :)); % find cluster's parent metabolite
            ti = hdp.ti(parent_i);
            tij = normrnd(ti, sqrt(1/hdp.delta_prec)); % then sample cluster rt given ti
            hdp.file{j}.ti(k) = tij;
        end

        % 1 x I, no. of clusters under each metabolite
        hdp.fi = hdp.fi + sum(hdp.file{j}.top_Z, 1);    

        % 1 x K, no. of peaks under each cluster
        hdp.file{j}.count_Z = sum(hdp.file{j}.Z, 1);
          
    end

end
