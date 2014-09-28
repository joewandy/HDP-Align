function hdp = assign_peaks_rt(hdp, debug)

    for j = randperm(hdp.J)
                            
        for n = randperm(hdp.file{j}.N)
            
            % get the peak data
            this_peak.rt = hdp.file{j}.data_rt(n);
            this_peak.mass = hdp.file{j}.data_mass(n);
            this_peak.intensity = hdp.file{j}.data_intensity(n);
            this_peak.peakID = hdp.file{j}.peakID(n);
            if debug
                this_peak.ground_truth = hdp.file{j}.ground_truth(n);
            end

            % remove peak from model
            k = find(hdp.file{j}.Z(n, :));
            hdp.file{j}.Z(n, :) = 0;
            hdp.file{j}.count_Z = sum(hdp.file{j}.Z, 1);
                        
            % does this result in an empty cluster ?
            if (hdp.file{j}.count_Z(k) == 0)
            
                % delete the cluster
                hdp.file{j}.K = hdp.file{j}.K - 1; % decrease count of clusters in file j
                hdp.file{j}.Z(:, k) = []; % delete the whole column from peaks-cluster assignment        

                % update for bookkeeping
                hdp.file{j}.count_Z = sum(hdp.file{j}.Z, 1);
                hdp.file{j}.ti(k) = [];

                % find cluster's parent metabolite
                i = find(hdp.file{j}.top_Z(k, :));
                hdp.file{j}.top_Z(k, :) = []; % remove assignment of cluster to parent metabolite
                hdp.fi(i) = hdp.fi(i) - 1; % decrease count of clusters under parent metabolite

                % does this result in an empty metabolite ?
                if (hdp.fi(i) == 0)

                    % delete the metabolite across all replicates
                    for rep = 1:hdp.J
                        hdp.file{rep}.top_Z(:, i) = [];
                    end
                    
                    % delete top-level info too
                    hdp.fi(i) = [];
                    hdp.ti(i) = [];
                    hdp.I = hdp.I - 1;

                end
                    
            end

            cluster_prior = [hdp.file{j}.count_Z hdp.alpha_rt];
            cluster_prior = cluster_prior./sum(cluster_prior);
            
            % for current local RT cluster
            ti = hdp.file{j}.ti;
            prec = hdp.gamma_prec;
            current_cluster_log_like = -0.5*log(2*pi) + 0.5*log(prec) - 0.5*prec.*(this_peak.rt - ti).^2;
                        
            % for new cluster: compute p( xnj | existing metabolite )
            Q = sum(hdp.fi) + hdp.top_alpha;
            current_metabolite_post = [];
            for i = 1:hdp.I
                prior = hdp.fi(i)/Q;
                mu = hdp.ti(i);
                prec = 1/(1/hdp.gamma_prec + 1/hdp.delta_prec);
                likelihood = sqrt(prec/(2*pi)) * exp((-prec*(this_peak.rt-mu)^2)/2);
                posterior = prior * likelihood;
                current_metabolite_post = [current_metabolite_post, posterior];
            end

            % for new cluster: compute p( xnj | new metabolite )
            prior = hdp.top_alpha/Q;
            mu = hdp.mu_0;
            prec = 1/(1/hdp.gamma_prec + 1/hdp.delta_prec + 1/hdp.sigma_0_prec);
            likelihood = sqrt(prec/(2*pi)) * exp((-prec*(this_peak.rt-mu)^2)/2);
            new_metabolite_post = prior * likelihood;

            % pick the cluster
            metabolite_post = [current_metabolite_post, new_metabolite_post];
            new_cluster_like = sum(metabolite_post);
            cluster_log_like = [current_cluster_log_like, log(new_cluster_like)];
            cluster_log_post = log(cluster_prior) + cluster_log_like;
            cluster_post = exp(cluster_log_post - max(cluster_log_post)); % why divide by max ?
            cluster_post = cluster_post./sum(cluster_post);
            k = find(rand<=cumsum(cluster_post), 1);

            if (k > hdp.file{j}.K)
            
                % new cluster
                hdp.file{j}.K = hdp.file{j}.K + 1; % update counts of clusters

                % resize peak-cluster assignment to include the new cluster
                new_column = zeros(hdp.file{j}.N, 1); 
                hdp.file{j}.Z = [hdp.file{j}.Z, new_column];
                hdp.file{j}.Z(n, k) = 1; % put the peak into the cluster

                % resize cluster-metabolite assignment to include the new cluster
                new_row = zeros(1, hdp.I);
                hdp.file{j}.top_Z = [hdp.file{j}.top_Z; new_row];

                % decide which metabolite to assign the new cluster to
                metabolite_post = metabolite_post./sum(metabolite_post);
                i = find(rand<=cumsum(metabolite_post)); % sample from the posterior with a cointoss
                i = i(1);

                if (i <= hdp.I)

                    % current metabolite
                    hdp.file{j}.top_Z(k, i) = 1;
                    hdp.fi(i) = hdp.fi(i) + 1;

                else

                    % new metabolite
                    hdp.I = hdp.I + 1;

                    % resize cluster-metabolite assignment in all files to include the new metabolite
                    for rep = 1:hdp.J
                        new_column = zeros(hdp.file{rep}.K, 1);
                        hdp.file{rep}.top_Z = [hdp.file{rep}.top_Z, new_column]; 
                    end                                                           

                    % assign the cluster under this new metabolite
                    hdp.file{j}.top_Z(k, i) = 1;
                    hdp.fi = [hdp.fi, 1];
                    
                    % generate ti given data
                    temp = 1 / (1/hdp.gamma_prec + 1/hdp.delta_prec);
                    prec = temp + hdp.sigma_0_prec;
                    mu = 1/prec * (temp*this_peak.rt + hdp.sigma_0_prec*hdp.mu_0);
                    new_ti = normrnd(mu, sqrt(1/prec));
                    hdp.ti = [hdp.ti, new_ti];                    

                end               
                
                % generate tij from tij and data
                prec = hdp.gamma_prec + hdp.delta_prec;
                mu = 1/prec * (hdp.gamma_prec*this_peak.rt + hdp.delta_prec*hdp.ti(i));
                new_tij = normrnd(mu, sqrt(1/prec));
                hdp.file{j}.ti = [hdp.file{j}.ti, new_tij];

            end

            % add peak back into model            
            hdp.file{j}.Z(n, :) = 0;
            hdp.file{j}.Z(n, k) = 1;
            hdp.file{j}.count_Z = sum(hdp.file{j}.Z, 1);

        end % end peak loop
                        
    end

end
