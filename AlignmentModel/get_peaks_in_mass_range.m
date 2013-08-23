% get index of peaks within the specified mass range
function out = get_peaks_in_mass_range(from_mass, delta, to_peaks, to_masses)

    % get all masses within bounds
    lower_bound = from_mass-delta;
    upper_bound = from_mass+delta;
    mass_idx = find( (to_masses >= lower_bound) & (to_masses <= upper_bound) );    

    % find which masses intersect with peaks in clusters to align
    common = intersect(mass_idx, to_peaks);

    % pick the peak with the minimum difference to our mass
    [min_val min_idx] = min(abs(to_masses(common) - from_mass));
    out = common(min_idx);
    
