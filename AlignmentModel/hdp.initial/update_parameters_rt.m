function hdp = update_parameters_rt(hdp, debug)

    %%%%% update RT clusters %%%%%        

    for j = 1:hdp.J
        hdp.file{j}.sum_Z = sum( repmat(hdp.file{j}.data_rt, 1, hdp.file{j}.K) .* hdp.file{j}.Z, 1);        
        for k = 1:hdp.file{j}.K
            % find parent metabolite of this cluster
            i = find(hdp.file{j}.top_Z(k, :));
            ti = hdp.ti(i);
            % find the peaks under this cluster
            child_peaks = find(hdp.file{j}.Z(:, k));
            sum_xn = sum(hdp.file{j}.data_rt(child_peaks));
            count_peaks = length(child_peaks);
            % draw new tij
            prec = hdp.delta_prec + count_peaks*hdp.gamma_prec;
            mu = (1/prec) .* ( ti*hdp.delta_prec + hdp.gamma_prec*sum_xn );
            hdp.file{j}.ti(k) = normrnd(mu, sqrt(1/prec));
        end
    end                          
                    
    %%%%% update metabolite RT %%%%%
    
    for i = 1:hdp.I
    
        % find all clusters under metabolite i
        sum_clusters = 0;
        count_clusters = 0;
        for j = 1:hdp.J
            child_clusters = find(hdp.file{j}.top_Z(:, i)); % find the clusters
            sum_clusters = sum_clusters + sum(hdp.file{j}.ti(child_clusters)); % sum the cluster RTs 
            count_clusters = count_clusters + length(child_clusters); % count the clusters
        end

        % draw new ti given the cluster RTs
        prec = hdp.sigma_0_prec + count_clusters*hdp.delta_prec;
        mu = (1/prec) .* ( hdp.mu_0*hdp.sigma_0_prec + hdp.delta_prec*sum_clusters );
        hdp.ti(i) = normrnd(mu, sqrt(1/prec));
        
    end                    


end
