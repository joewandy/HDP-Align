function hdp = assign_peaks_mass_rt(hdp, debug)

    % for j = randperm(hdp.J)
      for j = 1:hdp.J
                           
        j
          
        % for n = randperm(hdp.file{j}.N)
        for n = 1:hdp.file{j}.N          
            
            %%%%%%%%%% 0. find all the indices %%%%%%%%%%
            
            % get the peak data
            this_peak.rt = hdp.file{j}.data_rt(n);
            this_peak.mass = hdp.file{j}.data_mass(n);
            this_peak.intensity = hdp.file{j}.data_intensity(n);
            this_peak.peakID = hdp.file{j}.peakID(n);
            if debug
                this_peak.ground_truth = hdp.file{j}.ground_truth(n);
            end

            % find the RT cluster
            % k = find(hdp.file{j}.Z(n, :));
            % i = find(hdp.file{j}.top_Z(k, :)); % the RT cluster's parent metabolite 
            k = hdp.file{j}.Z(n);
            i = hdp.file{j}.top_Z(k);

            % find the mass cluster
            peak_n = find(hdp.metabolite(i).peak_n==n); % find all peaks at position n
            peak_j = find(hdp.metabolite(i).peak_j==j); % find all peaks from file j
            peak_pos = intersect(peak_n, peak_j);       % find peak at position n and from file j
            a = hdp.metabolite(i).V(peak_pos);
            assert(~isempty(peak_pos), 'Invalid position index');




            %%%%%%%%%% 1. remove peak from model %%%%%%%%%%
            
            % hdp.file{j}.Z(n, :) = 0;
            hdp.file{j}.Z(n) = 0;
            % hdp.file{j}.count_Z = sum(hdp.file{j}.Z, 1);
            hdp.file{j}.count_Z(k) = hdp.file{j}.count_Z(k) - 1;
            hdp.file{j}.sum_Z(k) = hdp.file{j}.sum_Z(k) - this_peak.rt;
            hdp.metabolite(i).V(peak_pos) = [];
            hdp.metabolite(i).peak_n(peak_pos) = [];
            hdp.metabolite(i).peak_j(peak_pos) = [];      
            hdp.metabolite(i).peak_data(peak_pos) = [];                        
            hdp.metabolite(i).fa(a) = hdp.metabolite(i).fa(a) - 1;
            hdp.metabolite(i).sa(a) = hdp.metabolite(i).sa(a) - this_peak.mass;            

            % does this result in an empty mass cluster ?
            if (hdp.metabolite(i).fa(a) == 0)
            
                % delete this mass cluster indexed by a
                hdp.metabolite(i).A = hdp.metabolite(i).A - 1;
                hdp.metabolite(i).thetas(a) = [];
                hdp.metabolite(i).fa(a) = [];
                hdp.metabolite(i).sa(a) = [];

                % decrease all indices in V that is > a by 1 since a is deleted
                affected = find(hdp.metabolite(i).V>a);
                hdp.metabolite(i).V(affected) = hdp.metabolite(i).V(affected) - 1;
                
                assert(length(hdp.metabolite(i).thetas)==hdp.metabolite(i).A, 'invalid length');                                

            end
                        
            % does this result in an empty RT cluster ?
            if (hdp.file{j}.count_Z(k) == 0)
            
                % delete the RT cluster
                hdp.file{j}.K = hdp.file{j}.K - 1; % decrease count of clusters in file j
                % hdp.file{j}.Z(:, k) = []; % delete the whole column from peaks-cluster assignment
                % decrease all indices in Z that is > k by 1 since k is deleted
                affected = find(hdp.file{j}.Z>k);
                hdp.file{j}.Z(affected) = hdp.file{j}.Z(affected) - 1;
                        

                % update for bookkeeping
                % hdp.file{j}.count_Z = sum(hdp.file{j}.Z, 1);
                hdp.file{j}.count_Z(k) = [];
                hdp.file{j}.sum_Z(k) = [];
                tij = hdp.file{j}.ti(k);
                hdp.file{j}.ti(k) = [];

                % hdp.file{j}.top_Z(k, :) = []; % remove assignment of cluster to parent metabolite
                hdp.file{j}.top_Z(k) = [];
                hdp.fi(i) = hdp.fi(i) - 1; % decrease count of clusters under parent metabolite
                hdp.si(i) = hdp.si(i) - tij;

                % does this result in an empty metabolite ?
                if (hdp.fi(i) == 0)

                    % delete the metabolite across all replicates
                    for rep = 1:hdp.J
                        % hdp.file{rep}.top_Z(:, i) = [];
                        % decrease all indices in top_Z that is > i by 1 since i is deleted
                        affected = find(hdp.file{rep}.top_Z>i);
                        hdp.file{rep}.top_Z(affected) = hdp.file{rep}.top_Z(affected) - 1;                        
                    end
                    
                    % delete top-level info too
                    hdp.fi(i) = [];
                    hdp.si(i) = [];
                    hdp.ti(i) = [];
                    hdp.I = hdp.I - 1;

                    % delete mass clusters info related to this metabolite too
                    hdp.metabolite(i) = [];

                end
                
            end
            
            
            
            
            %%%%%%%%%% 2. perform peak assignments %%%%%%%%%%
            
            % for current RT cluster, first compute the RT term, eq. #15
            ti = hdp.file{j}.ti;
            prec = hdp.gamma_prec;
            rt_term_log_like = -0.5*log(2*pi) + 0.5*log(prec) - 0.5*prec.*(this_peak.rt - ti).^2;
            
            % then for every RT cluster, compute the likelihood of this peak to be in the mass clusters linked to it, eq #16
            % first, we compute this over all existing metabolites
            metabolite_mass_like = zeros(1, hdp.I);
            for this_metabolite = 1:hdp.I
                                
                % first consider existing mass clusters
                count_fa = length(hdp.metabolite(this_metabolite).fa); 
                mass_term_prior = zeros(1, count_fa+1); % extra 1 for new cluster
                mass_term_prior(1, 1:count_fa) = hdp.metabolite(this_metabolite).fa;
                count_theta = length(hdp.metabolite(this_metabolite).thetas);
                mu = zeros(1, count_theta+1); % extra 1 for new cluster
                mu(1, 1:count_theta) = hdp.metabolite(this_metabolite).thetas;
                prec = zeros(1, count_theta+1); % extra 1 for new cluster
                temp = repmat([hdp.rho_prec], 1, hdp.metabolite(this_metabolite).A); % copy multiple times
                prec(1, 1:count_theta) = temp;
                
                % then add the term for the new mass cluster
                mass_term_prior(end) = hdp.alpha_mass;
                mass_term_prior = mass_term_prior ./ sum(mass_term_prior); % normalise
                mu(end) = hdp.psi_0;
                temp = 1/(1/hdp.rho_prec + 1/hdp.rho_0_prec);
                prec(end) = temp;

                % compute posterior probability            
                mass_term_likelihood = sqrt(prec/(2*pi)) .* exp((-prec.*(this_peak.mass-mu).^2)/2);
                mass_term_post = mass_term_prior .* mass_term_likelihood;                        
                assert(length(mass_term_post)==hdp.metabolite(this_metabolite).A+1, 'inconsistent state');

                % marginalise over all the mass clusters by summing over them             
                metabolite_mass_like(i) = sum(mass_term_post);

            end
            
            % then loop over all clusters
            mass_term_log_like = zeros(1, hdp.file{j}.K); % 1 by K, stores the mass log likelihood linked to each RT cluster
            for this_cluster = 1:hdp.file{j}.K
            
                % the RT cluster's parent metabolite
                this_metabolite = hdp.file{j}.top_Z(this_cluster);
                
                % take the log of the marginal probability over the mass clusters
                mass_term_log_like(this_cluster) = log(metabolite_mass_like(this_metabolite));
                                
            end
            
            % finally, the likelihood of peak going into a current cluster
            % is the RT term * mass term, eq #14
            current_cluster_log_like = rt_term_log_like + mass_term_log_like;
            
            % now compute the likelihood for peak going into new cluster, eq #19.             
            % first, compute p( d_jn | existing metabolite ), eq #20
            denum = sum(hdp.fi) + hdp.top_alpha;
            current_metabolite_post = zeros(1, hdp.I);
            for this_metabolite = 1:hdp.I
                prior = hdp.fi(i)/denum;
                % first compute the rt term
                mu = hdp.ti(this_metabolite);
                prec = 1/(1/hdp.gamma_prec + 1/hdp.delta_prec);
                rt_likelihood = sqrt(prec/(2*pi)) * exp((-prec*(this_peak.rt-mu)^2)/2);
                % then compute the mass term
                mass_likelihood = metabolite_mass_like(this_metabolite);
                % multiply likelihood with prior to get the posterior p( % d_jn | existing metabolite )
                % likelihood = rt_likelihood * mass_likelihood;
                likelihood = rt_likelihood;
                posterior = prior * likelihood;
                current_metabolite_post(i) = posterior;
            end

            % then compute p( d_jn | new metabolite ), eq #21
            prior = hdp.top_alpha/denum;
            mu = hdp.mu_0;
            prec = 1/(1/hdp.gamma_prec + 1/hdp.delta_prec + 1/hdp.sigma_0_prec);
            rt_likelihood = sqrt(prec/(2*pi)) * exp((-prec*(this_peak.rt-mu)^2)/2);
            mu = hdp.psi_0;
            prec = 1/(1/hdp.rho_prec + 1/hdp.rho_0_prec);
            mass_likelihood = sqrt(prec/(2*pi)) * exp((-prec*(this_peak.mass-mu)^2)/2);            
            % likelihood = rt_likelihood * mass_likelihood;
            likelihood = rt_likelihood;
            new_metabolite_post = prior * likelihood;
            
            % sum over for eq #19
            metabolite_post = [current_metabolite_post, new_metabolite_post];
            new_cluster_log_like = log(sum(metabolite_post));
            
            % pick either existing or new RT cluster
            % set the prior
            cluster_prior = [hdp.file{j}.count_Z hdp.alpha_rt];
            cluster_prior = cluster_prior./sum(cluster_prior);                        
            % set the likelihood = p(x_nj|new metabolite) * p(y_jn|new mass cluster)
            cluster_log_like = [current_cluster_log_like, new_cluster_log_like];
            cluster_log_post = log(cluster_prior) + cluster_log_like;            
            % compute and sample from posterior
            cluster_post = exp(cluster_log_post - max(cluster_log_post));
            cluster_post = cluster_post./sum(cluster_post);
            k = find(rand<=cumsum(cluster_post), 1);

            if (k > hdp.file{j}.K)
            
                % new cluster
                hdp.file{j}.K = hdp.file{j}.K + 1; % update counts of clusters

                % resize peak-cluster assignment to include the new cluster
                % new_column = zeros(hdp.file{j}.N, 1); 
                % hdp.file{j}.Z = [hdp.file{j}.Z, new_column];
                % hdp.file{j}.Z(n, k) = 1; % put the peak into the cluster
                hdp.file{j}.count_Z = [hdp.file{j}.count_Z, 0];
                hdp.file{j}.sum_Z = [hdp.file{j}.sum_Z, 0];

                % resize cluster-metabolite assignment to include the new cluster
                % new_row = zeros(1, hdp.I);
                % hdp.file{j}.top_Z = [hdp.file{j}.top_Z; new_row];
                hdp.file{j}.top_Z = [hdp.file{j}.top_Z, 0];

                % decide which metabolite to assign the new cluster to
                metabolite_post = metabolite_post./sum(metabolite_post); % remember to convert to probability distribution
                i = find(rand<=cumsum(metabolite_post), 1); % sample from the posterior with a cointoss

                if (i <= hdp.I)

                    % current metabolite
                    % hdp.file{j}.top_Z(k, i) = 1;
                    hdp.file{j}.top_Z(k) = i;
                    hdp.fi(i) = hdp.fi(i) + 1;

                else

                    % new metabolite
                    hdp.I = hdp.I + 1;

                    % resize cluster-metabolite assignment in all files to include the new metabolite
                    % for rep = 1:hdp.J
                    %     new_column = zeros(hdp.file{rep}.K, 1);
                    %     hdp.file{rep}.top_Z = [hdp.file{rep}.top_Z, new_column]; 
                    % end                                                           

                    % assign the cluster under this new metabolite
                    % hdp.file{j}.top_Z(k, i) = 1;
                    hdp.file{j}.top_Z(k) = i;
                    hdp.fi = [hdp.fi, 1];
                    hdp.si = [hdp.si, 0];
                    
                    % generate ti given data RT
                    temp = 1 / (1/hdp.gamma_prec + 1/hdp.delta_prec);
                    prec = temp + hdp.sigma_0_prec;
                    mu = 1/prec * (temp*this_peak.rt + hdp.sigma_0_prec*hdp.mu_0);
                    new_ti = normrnd(mu, sqrt(1/prec));
                    hdp.ti = [hdp.ti, new_ti];      
                    
                    % create empty mass cluster data structures for use later
                    hdp.metabolite(i).A = 0;
                    hdp.metabolite(i).fa = [];
                    hdp.metabolite(i).sa = [];                    
                    hdp.metabolite(i).V = [];
                    hdp.metabolite(i).peak_j = [];
                    hdp.metabolite(i).peak_n = [];
                    hdp.metabolite(i).peak_data = [];
                                                            
                end               
                
                % generate tij given ti and data RT
                prec = hdp.gamma_prec + hdp.delta_prec;
                mu = 1/prec * (hdp.gamma_prec*this_peak.rt + hdp.delta_prec*hdp.ti(i));
                new_tij = normrnd(mu, sqrt(1/prec));
                hdp.file{j}.ti = [hdp.file{j}.ti, new_tij];
                
                hdp.si(i) = hdp.si(i) + new_tij;

            end

            % now given RT cluster k, we can assign peak to the mass clusters linked to k
            % mass_term_post = mass_term_posterior_dist{k};            
            % mass_term_post = mass_term_post./sum(mass_term_post); % convert to probability distribution
            % a = find(rand<=cumsum(mass_term_post), 1);            

            % now perform peak to mass cluster assignments
            % i = find(hdp.file{j}.top_Z(k, :)); % find the parent metabolite first            
            i = hdp.file{j}.top_Z(k);

            % for existing mass cluster
            count_fa = length(hdp.metabolite(i).fa);             
            log_prior = zeros(1, count_fa+1);
            log_prior(1, 1:count_fa) = log(hdp.metabolite(i).fa);
            count_theta = length(hdp.metabolite(i).thetas);            
            mu = zeros(1, count_theta+1); % extra 1 for new cluster
            mu(1, 1:count_theta) = hdp.metabolite(i).thetas;
            prec = zeros(1, count_theta+1); % extra 1 for new cluster
            temp = repmat([hdp.rho_prec], 1, hdp.metabolite(i).A); % copy multiple times
            prec(1, 1:count_theta) = temp;
            
            % add the terms for the new mass cluster
            log_prior(end) = log(hdp.alpha_mass);
            mu(end) = hdp.psi_0;
            temp = 1/(1/hdp.rho_prec + 1/hdp.rho_0_prec);
            prec(end) = temp;

            % compute likelihood and posterior            
            log_likelihood = -0.5*log(2*pi) + 0.5*log(prec) - 0.5*prec.*(this_peak.mass - mu).^2;
            log_post = log_prior + log_likelihood;                        

            % pick the mass cluster
            post = exp(log_post - max(log_post));
            post = post./sum(post);
            a = find(rand<=cumsum(post), 1);           
                    
            if (a > hdp.metabolite(i).A)

                % make a new mass cluster
                hdp.metabolite(i).A = hdp.metabolite(i).A + 1;
                hdp.metabolite(i).fa(a) = 0;
                hdp.metabolite(i).sa(a) = 0;

                % sample new mass cluster value
                sigma_a = hdp.rho_prec + hdp.rho_0_prec;
                mu_a = 1/sigma_a * (hdp.rho_prec*this_peak.mass + hdp.rho_0_prec*hdp.psi_0);
                theta_ia = normrnd(mu_a, sqrt(1/sigma_a));
                hdp.metabolite(i).thetas(a) = theta_ia;
                assert(length(hdp.metabolite(i).thetas)==hdp.metabolite(i).A, 'invalid length');                

            end                   




            %%%%%%%%%% 3. add peak back into model %%%%%%%%%%
            
            % assign the peak under mass cluster a
            hdp.metabolite(i).fa(a) = hdp.metabolite(i).fa(a) + 1; % update count of peaks under a
            hdp.metabolite(i).sa(a) = hdp.metabolite(i).sa(a) + this_peak.mass; % update sum mass of peaks under a
            hdp.metabolite(i).V = [hdp.metabolite(i).V, a];            
            hdp.metabolite(i).peak_n = [hdp.metabolite(i).peak_n, n];
            hdp.metabolite(i).peak_j = [hdp.metabolite(i).peak_j, j];
            hdp.metabolite(i).peak_data = [hdp.metabolite(i).peak_data, this_peak];
                        
            % assign the peak under RT cluster k
            % hdp.file{j}.Z(n, :) = 0;
            % hdp.file{j}.Z(n, k) = 1;
            hdp.file{j}.Z(n) = k;
            % hdp.file{j}.count_Z = sum(hdp.file{j}.Z, 1);            
            hdp.file{j}.count_Z(k) = hdp.file{j}.count_Z(k) + 1;
            hdp.file{j}.sum_Z(k) = hdp.file{j}.sum_Z(k) + this_peak.rt;




            %%%%%%%%%% 4. maintain model state consistency %%%%%%%%%%
            
            % maintain that V can never contain 0                        
            has_zeros = find(hdp.metabolite(i).V==0);
            condition1 = ~isempty(has_zeros);
            condition2 = length(has_zeros)>0;
            if condition1
                assert(condition2, 'inconsistent state');
            end
            
            % maintain that everything else is consistent
            peak_n = find(hdp.metabolite(i).peak_n==n); % find all peaks at position n
            peak_j = find(hdp.metabolite(i).peak_j==j); % find all peaks from file j
            peak_pos = intersect(peak_n, peak_j);       % find peak at position n and from file j
            assert(~isempty(peak_pos), 'Invalid position index');            
            assert(length(hdp.metabolite(i).peak_n) == length(hdp.metabolite(i).peak_j), 'inconsistent state');
            assert(length(hdp.metabolite(i).thetas)==hdp.metabolite(i).A, 'inconsistent state');
            assert(length(hdp.metabolite(i).fa)==hdp.metabolite(i).A, 'inconsistent state');
            assert(length(hdp.metabolite(i).sa)==hdp.metabolite(i).A, 'inconsistent state');
            
            % check more, the sums and all that
            

        end % end peak loop
                        
    end

end
