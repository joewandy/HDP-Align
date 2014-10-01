function [log_prior, log_likelihood] = get_dp_log_prior_likelihood(x, component_prec, hyperparam_mean, hyperparam_prec, dp_alpha, c_counts, c_sums)

    % compute prior probability for K existing table and new table
    prior = [c_counts dp_alpha];
    prior = prior./sum(prior);  

    % for current k
    param_beta = hyperparam_prec + (component_prec*c_counts);
    param_alpha = (1./param_beta).*((hyperparam_prec*hyperparam_mean) +  (component_prec*c_sums));

    % for new k
    last_param_beta = hyperparam_prec;
    last_param_alpha = hyperparam_mean;
    param_beta = [param_beta, last_param_beta];
    param_alpha = [param_alpha, last_param_alpha];
    
    % compute log likelihood
    prec = 1./((1./param_beta)+(1/component_prec));
    log_likelihood = -0.5*log(2*pi) + 0.5*log(prec) - 0.5*prec.*(x - param_alpha).^2;
    log_prior = log(prior);

end
