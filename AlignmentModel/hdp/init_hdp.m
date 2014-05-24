function hdp = init_hdp(input_hdp, debug)

    % copy all the parameters from the input object ..
    
    hdp.J = input_hdp.J;                        % number of replicates
    hdp.NSAMPS = input_hdp.NSAMPS;              % total number of samples
    hdp.BURN_IN = input_hdp.BURN_IN;            % initial burn-in samples

    hdp.mu_0 = input_hdp.mu_0;                  % mean for base distribution of metabolite RT
    hdp.sigma_0_prec = input_hdp.sigma_0_prec;  % prec for base distribution of metabolite RT
    hdp.psi_0 = input_hdp.psi_0;                % mean for base distribution of mass
    hdp.rho_0 = input_hdp.rho_0;                % precision for base distribution of mass
    
    hdp.alpha_rt = input_hdp.alpha_rt;          % RT cluster DP concentration param
    hdp.alpha_mass = input_hdp.alpha_mass;      % mass cluster DP concentration param
    hdp.top_alpha = input_hdp.top_alpha;        % metabolite DP concentration param    

    hdp.gamma_prec = input_hdp.gamma_prec;      % precision for RT cluster mixture components
    hdp.rho_prec = input_hdp.rho_prec;          % precision for mass cluster mixture components
    hdp.delta_prec = input_hdp.delta_prec;      % precision for top component Gaussians    
    
    if debug
        hdp.gtI = input_hdp.I;                  % for debugging
    end
    
    % sample initial metabolite RT    
    hdp.I = 1;                                  % initial number of metabolites
    hdp.fi = zeros(1, hdp.I);                   % 1 x I, number of clusters assigned to metabolite i    
    hdp.ti = normrnd(hdp.mu_0, sqrt(1/hdp.sigma_0_prec), 1, hdp.I); % 1 x I, the metabolite's RT

    for j = 1:hdp.J

        hdp.file{j}.K = 1; % number of clusters in this file
        hdp.file{j}.peakID = input_hdp.file{j}.peakID;
        hdp.file{j}.N = length(hdp.file{j}.peakID);
        hdp.file{j}.data_rt = input_hdp.file{j}.data_rt;
        hdp.file{j}.data_mass = input_hdp.file{j}.data_mass;
        hdp.file{j}.data_intensity = input_hdp.file{j}.data_intensity;
        
        if debug
            hdp.file{j}.ground_truth = input_hdp.file{j}.ground_truth; % for debugging
        end

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
                
        for i = 1:hdp.I
            hdp.metabolite{i}.A = 0; % number of mass cluster in this metabolite
            hdp.metabolite{i}.V = []; % assignment of peak n to mass cluster a
            hdp.metabolite{i}.fa = []; % number of peaks under each mass cluster
            hdp.metabolite{i}.sa = []; % sum of masses of peaks under each mass cluster
        end
                          
    end

end
