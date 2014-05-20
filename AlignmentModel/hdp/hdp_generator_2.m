function hdp = hdp_generator_2

    %% Generate some data from a HDP

    hdp.N = 100;                    % num peaks
    hdp.J = 5;                      % number of replicates
    hdp.mu_0 = 0;                   % base distribution mean
    hdp.sigma_0_prec = 1/1000;      % base distribution prec

    hdp.top_alpha = 5;             % metabolite DP concentration param    
    hdp.alpha = 1;                 % cluster DP concentration param

    hdp.gamma_prec = 1;             % precision for cluster-level Gaussian
    hdp.delta_prec = 1;             % precision for metabolite-level Gaussian

    hdp.I = 0;                      % initial number of metabolites
    hdp.fi = [];    
    hdp.ti = [];

    data = [];
    for j = 1:hdp.J

        hdp.file{j}.K = 0;           
        hdp.file{j}.ti = zeros(hdp.file{j}.K, 1);
        hdp.file{j}.fi = zeros(hdp.file{j}.K, 1);

        hdp.file{j}.Z = zeros(hdp.N, 1);
        hdp.file{j}.count_Z = zeros(hdp.file{j}.K, 1);

        hdp.file{j}.data = zeros(hdp.N, 1);
        hdp.file{j}.ground_truth = zeros(hdp.N, 1);
        
        for n = 1:hdp.N
        
            % Choose a table for this peak
            probs = [hdp.file{j}.count_Z hdp.alpha];
            probs = cumsum(probs./sum(probs));
            k = find(rand<=probs, 1);
            if k > hdp.file{j}.K

                % Have to make a new table
                hdp.file{j}.K = hdp.file{j}.K + 1;
                hdp.file{j}.count_Z(k) = 0; % later initialize
                
                % Choose a top level component for this new table
                probs = [hdp.fi hdp.top_alpha];
                probs = cumsum(probs./sum(probs));
                i = find(rand<=probs, 1);
                if i > hdp.I
                    % Create a new top level
                    hdp.I = hdp.I + 1;
                    hdp.ti(i) = normrnd(hdp.mu_0, sqrt(inv(hdp.sigma_0_prec)));
                    hdp.fi(i) = 0; % later initialize
                end
                
                % Assign table to top level
                hdp.file{j}.top_Z(k) = i;
                hdp.fi(i) = hdp.fi(i) + 1;
                ti = hdp.ti(i);
                hdp.file{j}.ti = normrnd(ti, sqrt(inv(hdp.delta_prec)));

            end

            % Assign peak to table
            hdp.file{j}.Z(n) = k;
            hdp.file{j}.count_Z(k) = hdp.file{j}.count_Z(k) + 1;
            tij = hdp.file{j}.ti;
            hdp.file{j}.data(n) = normrnd(tij, sqrt(inv(hdp.gamma_prec)));
            
            % Keep track of peak to top as well
            hdp.file{j}.ground_truth(n) = i;       

        end     

        data = [data, hdp.file{j}.data];
        
    end
    
    % Plot the data
    data = [];
    close all
    for j = 1:hdp.J
        [f,xi] = ksdensity(hdp.file{j}.data);
        plot(xi,f);
        hold all
        data = [data, hdp.file{j}.data];
    end
    
    hdp.data = data;
    
end
