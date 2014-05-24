function hdp = hdp_generator_2

    isOctave = exist('OCTAVE_VERSION', 'builtin') ~= 0;

    %% Generate some data from a HDP

    hdp.NSAMPS = 100;               % number of samples
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

    for j = 1:hdp.J

        hdp.file{j}.K = 0;           
        hdp.file{j}.ti = zeros(hdp.file{j}.K, 1);
        hdp.file{j}.fi = zeros(hdp.file{j}.K, 1);

        hdp.file{j}.Z = []; % N by 1
        hdp.file{j}.count_Z = zeros(hdp.file{j}.K, 1);

        hdp.file{j}.data_rt = []; % N by 1
        hdp.file{j}.ground_truth = []; % N by 1
        
        hdp.file{j}.peakID = []; % N by 1
        peakID = 0;
        for n = 1:hdp.N
        
            % peak has some probability of disappearing
            if rand>0.8
                continue
            end
        
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
            hdp.file{j}.Z = [hdp.file{j}.Z; k];
            hdp.file{j}.count_Z(k) = hdp.file{j}.count_Z(k) + 1;
            tij = hdp.file{j}.ti;
            rt = normrnd(tij, sqrt(inv(hdp.gamma_prec)));
            hdp.file{j}.data_rt = [hdp.file{j}.data_rt; rt];
            hdp.file{j}.peakID = [hdp.file{j}.peakID; peakID];
            peakID = peakID + 1;
            
            % Keep track of peak to top as well
            hdp.file{j}.ground_truth = [hdp.file{j}.ground_truth; i];

        end     
        
    end
    
    % Plot the data
    close all
    for j = 1:hdp.J
        if ~isOctave
            [f,xi] = ksdensity(hdp.file{j}.data_rt); % no equivalent function in octave ?!
            plot(xi,f);
            hold all
        end
    end
        
end
