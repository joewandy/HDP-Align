function metabolites = generate_metabolites(num_metabolites, num_clusters, cl_mean_min, cl_mean_max, cl_std, num_peaks, mass_max, mass_min)

    metabolites = [];
    cluster_ID = 1;
    for i = 1:num_metabolites

        metabolite.ID = i;

        % generate some clusters for each metabolite
        gen_cl = randi(num_clusters);
        clusters = [];
        for j = 1:gen_cl

            cluster.ID = cluster_ID;
            cluster_ID = cluster_ID + 1;

            % sample means, uniformly distributed from min to max
            cl_mean = cl_mean_min + (cl_mean_max - cl_mean_min) * rand();
            cluster.mean = cl_mean;
            cluster.std = cl_std;
            clusters = [clusters; cluster];

        end
        metabolite.clusters = clusters;

        % generate masses & rts for the peaks
        peaks = [];
        sel_peaks = poissrnd(num_peaks);
        for k = 1:sel_peaks

            mass = mass_min + (mass_max - mass_min) .* rand();
            peak.mass = mass;

            % pick one cluster randomly
            selected_cluster = randsample(clusters, 1);
            peak.cluster_ID = selected_cluster.ID;
            
            % generate RT of the peak, which is normally distributed
            rt = normrnd(selected_cluster.mean, selected_cluster.std);        
            peak.rt = rt;
            
            peaks = [peaks; peak];
            
        end
        metabolite.peaks = peaks;
        
        metabolites = [metabolites; metabolite];

    end

end
