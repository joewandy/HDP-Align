function [sorted_res, samples] = run_hdp(path, cluster_rt, cluster_mass, debug, label, NSAMPS, BURN_IN)

% read all the csv files in path
input_hdp.J = 0;                      % number of replicates
csv_path = [path, '/*.csv'];
fls = dir(csv_path);
data_means = [];
data_rt_all = [];
data_mass_all = [];
for j=1:length(fls)
    
    input_hdp.J = input_hdp.J + 1;

    filename = fls(j).name;
    filepath = [path, '/', filename];
    X = importdata(filepath);
    
    % store peak ID
    input_hdp.file{j}.peakID = X.data(:, 1);
    
    % store RT
    data_rt = X.data(:, 3);
    input_hdp.file{j}.data_rt = data_rt;
    data_rt_all = [data_rt_all; data_rt];
    
    % store mass
    data_mass = X.data(:, 2);
    input_hdp.file{j}.data_mass = data_mass;
    data_mass_all = [data_mass_all; data_mass];
    
    % store intensity
    input_hdp.file{j}.data_intensity = X.data(:, 4);    
    
end

% model parameters

input_hdp.NSAMPS = NSAMPS;               % total number of samples
input_hdp.BURN_IN = BURN_IN;              % burn-in samples  

input_hdp.mu_0 = mean(data_rt_all);   % base distribution mean for RT
input_hdp.sigma_0_prec = 1/5e6;      % base distribution prec for RT
input_hdp.psi_0 = mean(data_mass_all);% base distribution mean for mass
input_hdp.rho_0_prec = 1/5e6;         % base distribution prec for mass

input_hdp.alpha_rt = 10;               % RT cluster DP concentration param
input_hdp.alpha_mass = 10;             % mass cluster DP concentration param
input_hdp.top_alpha = 10;              % top-level DP concentration

% synth data

input_hdp.delta_prec = 1/(30*30);          % precision for top components
input_hdp.gamma_prec = 1/(2*2);          % precision for RT cluster mixture components
input_hdp.rho_prec = 1/(0.0025*0.0025);      % precision for mass cluster mixture components

% lange data P1

% input_hdp.delta_prec = 1/100;          % precision for top components
% input_hdp.gamma_prec = 1/100;          % precision for RT cluster mixture components
% input_hdp.rho_prec = 1/(0.05);      % precision for mass cluster mixture components

% glyco data -- shit result

% input_hdp.alpha_rt = 10;               % RT cluster DP concentration param
% input_hdp.alpha_mass = 10;             % mass cluster DP concentration param
% input_hdp.top_alpha = 10;              % top-level DP concentration

% input_hdp.delta_prec = 1/(10*10);          % precision for top components
% input_hdp.gamma_prec = 1/(10*10);          % precision for RT cluster mixture components
% input_hdp.rho_prec = 1/(0.05*0.05);      % precision for mass cluster mixture components

% run HDP
fprintf('Run HDP\n');
initialised = init_hdp(input_hdp, debug);
samples = do_hdp(initialised, cluster_rt, cluster_mass, debug);

fprintf('Result\n');
sorted_res=print_hdp_result(samples, cluster_rt, cluster_mass, debug, path, label);

% store the result

if cluster_rt & cluster_mass
    result_path = [path, '/hdp_result_rt_mass.mat'];
elseif cluster_rt
    result_path = [path, '/hdp_result_rt.mat'];
elseif cluster_mass
    result_path = [path, '/hdp_result_mass.mat'];
end
save('-v6', result_path, 'sorted_res');
