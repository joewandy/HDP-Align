function out = align_cluster(filename1, filename2, sigma, num_samples)

    more off;

    % load synthetic data 1
    synthdata1 = load_data(filename1);
    printf('\nClustering %s, num_samples=%d\n', filename1, num_samples);
    clustering1 = corr_cluster(synthdata1.Q, 'nsamps', num_samples);

    % load synthetic data 2
    synthdata2 = load_data(filename2);
    printf('Clustering %s, num_samples=%d\n', filename2, num_samples);
    clustering2 = corr_cluster(synthdata2.Q, 'nsamps', num_samples);

    # how many clusters found ?
    Z1 = clustering1.bestZ;
    Z2 = clustering2.bestZ;
    num_clusters1 = size(Z1, 2);
    num_clusters2 = size(Z2, 2);
    printf('Found %d clusters in %s\n', num_clusters1, filename1);    
    printf('Found %d clusters in %s\n\n', num_clusters2, filename2);    

    # try aligning ...
    printf('============================================================\n');    
    printf('%s\t%s\t# matches\n', filename1, filename2);
    printf('============================================================\n');
    printf('\n');
    
    out.cluster_alignment = zeros(1, 3);
    out.peak_alignment = zeros(1, 5);
    for i = 1:num_clusters1

        this_cluster = find(Z1(:, i));

        # compute similarity between clusters
        # sim_scores = [other_cluster other_cluster_similarity]
        sim_scores = zeros(num_clusters2, 2);
        for j = 1:num_clusters2
            other_cluster = find(Z2(:, j));
            cs = cluster_sim(sigma, this_cluster, synthdata1, other_cluster, synthdata2);
            sim_scores(j, :) = [j cs];
        endfor

        # match peaks to the top-N cluster with the largest score
        [dummy idx] = sort(sim_scores(:, 2), 'descend');    # sort by similarity
        sim_scores = sim_scores(idx, :);                    # reorder sim_scores by the index of sorted similarity        

        # get the first cluster, most similar one
        matched_cluster_idx = sim_scores(1, 1);
        matched_cluster_score = sim_scores(1, 2);

        matched_peaks = match_peaks(i, matched_cluster_idx, Z1, Z2, synthdata1, synthdata2);
        if matched_peaks.no_of_matches > 0

            # store the matching peaks for this cluster
            out.peak_alignment = [out.peak_alignment; matched_peaks.peak_alignment];

            # store matching clusters pair
            out.cluster_alignment = [out.cluster_alignment; i matched_cluster_idx matched_peaks.no_of_matches];
            printf('cluster %2d\t<->\tcluster %2d (%.4f)\t%d\n', i, matched_cluster_idx, matched_cluster_score, 
                matched_peaks.no_of_matches);

        endif
                             
    endfor
        
    # remove first row, the (0,0) entry, before returning them
    out.cluster_alignment = out.cluster_alignment(2:end, :);
    out.peak_alignment = out.peak_alignment(2:end, :);

    # compute tp, fp, tn, fn
    correct_idx = find(out.peak_alignment(:, 3) == 1);
    incorrect_idx = find(out.peak_alignment(:, 3) == 0);
    
    # TP: a peak aligned when it should be aligned (correctly aligned).
    # e.g. in the peak alignment list for the first dataset, alignment correct
    out.tp = length(out.peak_alignment(correct_idx, 4));

    # FP: a peak aligned when it should not be aligned (incorrectly aligned).
    # e.g. in the peak alignment list for the first dataset, alignment incorrect
    out.fp = length(out.peak_alignment(incorrect_idx, 4));

    # TN: a peak not aligned when it should not be aligned (correctly unaligned).
    # e.g. in the first dataset, not in the second dataset, not in peak alignment list
    out.tn = length(setdiff(setdiff(synthdata1.pID, synthdata2.pID), out.peak_alignment(:, 4)));

    # FN: a peak not aligned when it should be aligned (incorrectly unaligned).    
    # e.g. in the first dataset, in the second dataset, not in peak alignment list    
    out.fn = length(setdiff(intersect(synthdata1.pID, synthdata2.pID), out.peak_alignment(:, 4)));

    # computer tpr, fpr, f1
    precision = out.tp / (out.tp + out.fp);
    recall = out.tp / (out.tp + out.fn);
    out.precision = precision;
    out.recall = recall;
    out.tpr = recall;
    out.fpr = out.fp / (out.fp + out.tn);
    out.f1 = 2 * ( (precision * recall) / (precision + recall) ); 
    
    # debugging
    # out

    # other stuffs to return    
    out.synthdata1 = synthdata1;
    out.synthdata2 = synthdata2;
    out.clustering1 = clustering1;
    out.clustering2 = clustering2;
    out.Z1 = Z1;
    out.Z2 = Z2;
    out.num_clusters1 = num_clusters1;
    out.num_clusters2 = num_clusters2;
