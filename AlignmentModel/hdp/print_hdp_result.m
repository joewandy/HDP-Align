function print_hdp_result(samples)
    
    % across all files, for every peak, find its parent metabolite
    hdp = samples(end);
    res = zeros(hdp.I, hdp.gtI);
    for j=1:hdp.J

        Z = hdp.file{j}.Z;
        top_Z = hdp.file{j}.top_Z;

        N = hdp.file{j}.N;
        rts = hdp.file{j}.data;
        gts = hdp.file{j}.ground_truth;
        for n = 1:N
            table = find(Z(n, :));
            i = find(top_Z(table, :));
            gti = hdp.file{j}.ground_truth(n); % the actual parent
            res(i, gti) = res(i, gti) + 1; 
        end
    
    end
    
    ncol = size(res, 2);
    res = res ./ repmat(max(res,[],2), 1, ncol);
    figure;
    imagesc(res); colorbar;
    title('Inferred vs. Ground Truth');
    ylabel('Inferred Metabolite ID');
    xlabel('True Metabolite ID');

    % print the number of top components across samples
    topcount = [];
    for s=1:length(samples)
        samp = samples(s);
        topcount = [topcount, samp.I];
    end
    figure;
    title('No. of top components');
    hist(topcount);

    hdp.I    
    hdp.ti
    hdp.fi
    
end
