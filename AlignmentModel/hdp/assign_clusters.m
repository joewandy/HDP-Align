function hdp = assign_clusters(hdp)

    for j = randperm(hdp.J)
        for k = randperm(hdp.file{j}.K)

            % get the cluster RT
            cluster_rt = hdp.file{j}.ti(k);

            % remove cluster from model
            parent_i = find(hdp.file{j}.top_Z(k, :)); % find cluster's parent metabolite
            hdp.file{j}.top_Z(k, parent_i) = 0; % remove assignment of cluster to parent metabolite
            hdp.fi(parent_i) = hdp.fi(parent_i) - 1; % decrease count of clusters under parent metabolite

            % does this result in an empty metabolite ?
            if (hdp.fi(parent_i) == 0)

                % delete the metabolite across all replicates
                for rep = 1:hdp.J
                    hdp.file{rep}.top_Z(:, parent_i) = [];
                end
                
                % delete top-level info too
                hdp.fi(parent_i) = [];
                hdp.ti(parent_i) = [];
                hdp.I = hdp.I - 1;

            end
                        
            % for existing metabolite
            log_prior = log(hdp.fi);
            mu = hdp.ti;
            temp = hdp.delta_prec;
            prec = repmat(temp, 1, hdp.I);
            
            % add the terms for the new metabolite
            log_prior = [log_prior, log(hdp.top_alpha)];
            mu = [mu, hdp.mu_0];
            temp = 1/(1/hdp.delta_prec + 1/hdp.sigma_0_prec);
            prec = [prec, temp];

            % compute likelihood and posterior            
            log_likelihood = -0.5*log(2*pi) + 0.5*log(prec) - 0.5*prec.*(cluster_rt - mu).^2;
            log_post = log_prior + log_likelihood;                        
            
            % pick the metabolite            
            post = exp(log_post - max(log_post));
            post = post./sum(post);
            selected_i = find(rand<=cumsum(post), 1);            
            
            if (selected_i <= hdp.I)

                % current metabolite
                hdp.file{j}.top_Z(k, selected_i) = 1;
                hdp.fi(selected_i) = hdp.fi(selected_i) + 1;

            else

                % new metabolite
                hdp.I = hdp.I + 1;

                % resize cluster-metabolite assignment in all files to include the new metabolite
                for rep = 1:hdp.J
                    new_column = zeros(hdp.file{rep}.K, 1);
                    hdp.file{rep}.top_Z = [hdp.file{rep}.top_Z, new_column]; 
                end                                                           

                % assign the cluster under this new metabolite
                hdp.file{j}.top_Z(k, selected_i) = 1;
                hdp.fi = [hdp.fi, 1];
                prec = hdp.delta_prec + hdp.sigma_0_prec;
                mu = 1/prec * (hdp.delta_prec*cluster_rt + hdp.sigma_0_prec*hdp.mu_0);
                new_ti = normrnd(mu, sqrt(1/prec));
                hdp.ti = [hdp.ti, new_ti];   

            end                
                                   
        end
    end

end
