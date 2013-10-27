function selected_peaks = generate_peaks(metabolites)

    selected_peaks = [];

    % sel_meta = randi(length(metabolites));
    % selected_metabolites = randsample(metabolites, sel_meta);
    sel_meta = length(metabolites);
    selected_metabolites = metabolites;
    fprintf('Selected metabolites = %d\n', sel_meta);

    for i = 1:sel_meta
        metabolite = metabolites(i);
        % num_peaks = length(metabolite.peaks);
        % sel_peaks = randi(num_peaks);
        % selected_peaks = [selected_peaks; randsample(metabolite.peaks, sel_peaks)];
        selected_peaks = [selected_peaks; metabolite.peaks];
    end

    % sort the selected peaks by their retention time
    [dummy, pos] = sort([selected_peaks(:).cluster_ID]);
    selected_peaks = selected_peaks(pos);    
    fprintf('Total peaks = %d\n', length(selected_peaks));

end
