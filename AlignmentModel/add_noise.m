function peak = add_noise(peak, mass_tol, rt_tol)

    true_mass = peak.mass;
    mass_noise = rand() * mass_tol;
    peak.mass = true_mass + mass_noise;

    true_rt = peak.rt;
    rt_noise = normrnd(true_rt, rt_tol);
    peak.rt = true_rt + rt_noise;

end
