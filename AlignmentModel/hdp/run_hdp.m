function sorted_res = run_hdp(path)

% read all the csv files in path
input_hdp.J = 0;                      % number of replicates
csv_path = [path, '/*.csv'];
fls = dir(csv_path);
data_means = [];
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
    data_rt_all = [data_rt_all, data_rt];
    
    % store mass
    data_mass = X.data(:, 2);
    input_hdp.file{j}.data_mass = data_mass;
    data_mass_all = [data_mass_all, data_mass];
    
    % store intensity
    input_hdp.file{j}.data_intensity = X.data(:, 4);    
    
end

% model parameters

input_hdp.NSAMPS = 200;               % total number of samples
input_hdp.BURN_IN = 100;              % burn-in samples  

input_hdp.mu_0 = mean(data_rt_all);   % mean for base distribution of metabolite RT
input_hdp.sigma_0_prec = 1/5000;      % prec for base distribution of metabolite RT
input_hdp.psi_0 = mean(data_mass_all);% mean for base distribution of mass
input_hdp.rho_0 = 1/1000;             % precision for base distribution of mass

input_hdp.alpha_rt = 1;               % RT cluster DP concentration param
input_hdp.alpha_mass = 1;             % mass cluster DP concentration param
input_hdp.top_alpha = 1;              % top-level DP concentration

input_hdp.gamma_prec = 1/60;          % precision for RT cluster mixture components
input_hdp.rho_prec = 1/0.1;           % precision for mass cluster mixture components
input_hdp.delta_prec = 1/30;          % precision for top components

% run HDP
fprintf('Run HDP\n');
debug = false;
initialised = init_hdp(input_hdp, debug);
samples = do_hdp(initialised);

fprintf('Result\n');
sorted_res=print_hdp_result(samples, debug);

% store the result
result_path = [path, '/hdp_result.mat'];
save('-v6', result_path, 'sorted_res');
