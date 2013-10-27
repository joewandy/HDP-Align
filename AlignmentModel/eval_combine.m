function prec = eval_combine(data1, data2, mass_tol, rt_tol, use_clustering)

    % compute distances and scores
    masses = [data1.mass, data2.mass];
    dmz = std(masses);
    distances = zeros(length(data1), length(data2));
    for i = 1:length(data1)
        for j = 1:length(data2)
            peak1 = data1(i);
            peak2 = data2(j);
            if (use_clustering)
                peak_dist = mahalanobis(peak1, peak2, mass_tol, 1e10);
            else
                peak_dist = mahalanobis(peak1, peak2, mass_tol, rt_tol);
            end             
            distances(i, j) = peak_dist;
        end
    end
    Q = distances > 0;
    max_dist = max(max(distances));
    W = Q .* 1-(distances./max_dist);
    % figure;
    % imagesc(W); colorbar;
    % title('W');
    % xlabel('file1');
    % ylabel('file2');

    W_p = W;
    if (use_clustering)

        % assume perfect clustering => same clustering for both files
        Z1 = [data1.cluster_ID]';
        Z2 = [data1.cluster_ID]';
        Z = zeros(length(Z1), length(Z2));
        for i = 1:length(Z1)
            for j = 1:length(Z2)
                if Z1(i) == Z2(j)
                    Z(i, j) = 1;
                end
            end
        end
        % figure;
        % imagesc(Z); colorbar;
        % title('Z');

        A = Z;
        B = Z;
        W_p = Q .* (A*W*B);
        % figure;
        % imagesc(W_p); colorbar;
        % title('Q .* (A*W*B)');

    end;    
         
    % compute some measure
    % correct alignment lies along the diagonal 
    % (since peaks are ordered and have 1-to-1 correspondence)
    ground_truth = (1:length(data1))';

    % alignment is simply taking the highest score
    [Y, I] = max(W_p, [], 2);   

    % TP = correct alignment, FP = incorrect alignment
    TP = length(find(I == ground_truth));
    FP = length(find(I ~= ground_truth));
    prec = TP / (TP + FP);

end
