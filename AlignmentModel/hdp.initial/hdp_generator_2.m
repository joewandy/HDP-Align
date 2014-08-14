function hdp = hdp_generator_2

    isOctave = exist('OCTAVE_VERSION', 'builtin') ~= 0;

    %% Generate some data from a HDP

    hdp.NSAMPS = 100;               % number of samples
    hdp.BURN_IN = 100;              % burn-in samples

    hdp.N = 100;                    % num peaks
    hdp.J = 5;                      % number of replicates
    hdp.I = 0;                      % initial number of metabolites

    hdp.mu_0 = 0;                   % base distribution mean for RT
    hdp.sigma_0_prec = 1/5000;      % base distribution prec for RT
    hdp.psi_0 = 100;                % base distribution mean for mass
    hdp.rho_0_prec = 1/100;         % base distribution prec for mass

    hdp.alpha_rt = 1;               % RT cluster DP concentration param
    hdp.alpha_mass = 1;             % mass cluster DP concentration param
    hdp.top_alpha = 10;             % top-level DP concentration

    hdp.delta_prec = 1;             % precision for top components
    hdp.gamma_prec = 1;             % precision for RT cluster mixture components
    hdp.rho_prec = 1;               % precision for mass cluster mixture components
    
    hdp.fi = [];    
    hdp.ti = [];
    
    for j = 1:hdp.J

        hdp.file{j}.K = 0;           
        hdp.file{j}.ti = zeros(hdp.file{j}.K, 1);
        hdp.file{j}.fi = zeros(hdp.file{j}.K, 1);

        hdp.file{j}.top_Z = []; % 1 by K
        hdp.file{j}.Z = []; % 1 by N
        hdp.file{j}.count_Z = zeros(1, hdp.file{j}.K);

        hdp.file{j}.peakID = []; % N by 1
        hdp.file{j}.data_rt = []; % N by 1
        hdp.file{j}.data_mass = []; % N by 1
        hdp.file{j}.data_intensity = []; % N by 1
        hdp.file{j}.ground_truth = []; % N by 1

        peakID = 0;
        for n = 1:hdp.N
        
            % peak has some probability of disappearing
            if rand>0.8
                continue
            end
        
            % Choose an RT table for this peak
            probs = [hdp.file{j}.count_Z hdp.alpha_rt];
            probs = cumsum(probs./sum(probs));
            k = find(rand<=probs, 1);
            if k > hdp.file{j}.K

                % Have to make a new RT table
                hdp.file{j}.K = hdp.file{j}.K + 1;
                hdp.file{j}.count_Z(k) = 0; % later initialize
                
                % Choose a top level component for this new RT table
                probs = [hdp.fi hdp.top_alpha];
                probs = cumsum(probs./sum(probs));
                i = find(rand<=probs, 1);
                if i > hdp.I

                    % Create a new top level
                    hdp.I = hdp.I + 1;
                    hdp.ti(i) = normrnd(hdp.mu_0, sqrt(1/hdp.sigma_0_prec));
                    hdp.fi(i) = 0; % later initialize
                    
                    % for mass clusters
                    hdp.metabolite(i).A = 0;
                    hdp.metabolite(i).V = [];   % peak to mass clusters assignment
                    hdp.metabolite(i).fa = [];  % num of peaks in mass clusters                    
                    
                end
                
                % Assign RT table to top level
                hdp.file{j}.top_Z(k) = i;
                hdp.fi(i) = hdp.fi(i) + 1;
                ti = hdp.ti(i);
                hdp.file{j}.ti = normrnd(ti, sqrt(1/hdp.delta_prec));

            end

            % Assign peak to RT table
            hdp.file{j}.Z = [hdp.file{j}.Z, k];
            hdp.file{j}.count_Z(k) = hdp.file{j}.count_Z(k) + 1;
            tij = hdp.file{j}.ti;
            rt = normrnd(tij, sqrt(1/hdp.gamma_prec));
            hdp.file{j}.data_rt = [hdp.file{j}.data_rt; rt];
            hdp.file{j}.peakID = [hdp.file{j}.peakID; peakID];
            peakID = peakID + 1;
            
            % Keep track of peak to top as well
            hdp.file{j}.ground_truth = [hdp.file{j}.ground_truth; i];

            % now assign peaks in top component into mass tables
            i = hdp.file{j}.top_Z(k);            
            probs = [hdp.metabolite(i).fa hdp.alpha_mass];
            probs = cumsum(probs./sum(probs));
            a = find(rand<=probs, 1);
            if a > hdp.metabolite(i).A
                        
                % Have to make a new mass table
                hdp.metabolite(i).A = hdp.metabolite(i).A + 1;
                hdp.metabolite(i).fa(a) = 0; % later initialize
                hdp.metabolite(i).theta(a) = normrnd(hdp.psi_0, sqrt(inv(hdp.rho_0_prec)));
            
            end
            
            % Assign peak to mass table
            hdp.metabolite(i).V = [hdp.metabolite(i).V, a];
            hdp.metabolite(i).fa(a) = hdp.metabolite(i).fa(a) + 1;
            theta_ia = hdp.metabolite(i).theta(a);
            mass = normrnd(theta_ia, sqrt(inv(hdp.rho_prec)));
            hdp.file{j}.data_mass = [hdp.file{j}.data_mass; mass];   
            
            % not using intensity yet
            intensity = 0;
            hdp.file{j}.data_intensity = [hdp.file{j}.data_intensity; intensity];
            
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
