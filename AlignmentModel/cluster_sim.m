% compute probability product kernel for mass
function sim_score = cluster_sim(sigma, this_cluster, synthdata1, other_cluster, synthdata2)

    sim_score = 0;
    size_this = length(this_cluster);
    size_other = length(other_cluster);
    rt_window = 5;

    % compute the probability product kernel of all peaks in the two clusters
    m1 = synthdata1.masses(this_cluster);
    m2 = synthdata2.masses(other_cluster);
    rt1 = synthdata1.rettime(this_cluster);
    rt2 = synthdata2.rettime(other_cluster);
    rt_drift = abs(mean(rt1)-mean(rt2));
    if (rt_drift <= rt_window)
        for k = 1:size_this
            for l = 1:size_other
                gauss1.mu = m1(k);
                gauss1.cov = sigma^2;
                gauss2.mu = m2(l);
                gauss2.cov = sigma^2;
                sim_score += gauss_prod(gauss1, gauss2);                
            endfor
        endfor
        sim_score = sim_score / (size_this * size_other);
    endif
