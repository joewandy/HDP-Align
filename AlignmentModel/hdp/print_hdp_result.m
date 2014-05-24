function sorted_res = print_hdp_result(samples, debug)

    last = samples(end);

    if debug

        last.I    
        last.ti
        last.fi
    
        % across all files, for every peak, find its parent metabolite
        res = zeros(last.I, last.gtI);
        for j=1:last.J

            Z = last.file{j}.Z;
            top_Z = last.file{j}.top_Z;

            N = last.file{j}.N;
            rts = last.file{j}.data;
            gts = last.file{j}.ground_truth;
            for n = 1:N
                table = find(Z(n, :));
                i = find(top_Z(table, :));
                gti = last.file{j}.ground_truth(n); % the actual parent
                res(i, gti) = res(i, gti) + 1; 
            end
        
        end
        
        % visualise for the last sample
        ncol = size(res, 2);
        res = res ./ repmat(max(res,[],2), 1, ncol);
        figure;
        imagesc(res); colorbar;
        title('Inferred vs. Ground Truth');
        ylabel('Inferred Metabolite ID');
        xlabel('True Metabolite ID');

    end

    % print the number of top components across samples
    topcount = [];
    for s=1:length(samples)
        samp = samples(s);
        topcount = [topcount, samp.I];
    end
    figure;
    title('No. of top components');
    hist(topcount);

    % across all samples, find out which peak is put together with other peaks
    % shitty code ..
    peakvspeak_all = []; % [peakID1,file1,peakID2,file2,count]
    for s = 1:length(samples)
    
        fprintf('Processing sample %d\n', s);
    
        samp = samples(s);
        peakvspeak = []; % [peakID1,file1,peakID2,file2]
        for i = 1:samp.I

            % across all files, get all the peaks under this top component
            component_peaks = []; % [peakID,file] for all j
            for j = 1:samp.J
                top_Z = samp.file{j}.top_Z;
                cluspos = find(top_Z(:, i));
                if ~isempty(cluspos) % top component might be empty in some file
                    Z = samp.file{j}.Z;
                    for kidx = length(cluspos)
                        k = cluspos(kidx);
                        % get all the peaks under this cluster                            
                        peakpos = find(Z(:, k));
                        peakids = samp.file{j}.peakID(peakpos);
                        temp = [peakids, repmat(j, length(peakids), 1)]; % peakID,file for this j
                        component_peaks = [component_peaks; temp]; % combine
                    end                
                end
            end

            % convert from vector of [peakID,file] into [peakID1,file1,peakID2,file2]
            for n = 1:size(component_peaks, 1)
                this_file = component_peaks(n, 2);
                otherpos = find(component_peaks(:, 2) ~= this_file);
                other_peaks = component_peaks(otherpos, :);
                this_peak = component_peaks(n, :);
                entries = repmat(this_peak, size(other_peaks, 1), 1); % create copies of this peak
                entries = [entries, other_peaks]; % put it side by side with the other co-occuring peaks                
                peakvspeak = [peakvspeak; entries];
            end            
   
        end % end top component loop
        
        for pidx = 1:size(peakvspeak, 1)
            entry = peakvspeak(pidx, :);
            if (isempty(peakvspeak_all))
                peakvspeak_all = [peakvspeak_all; [entry, 1]];
            else
                cond1 = peakvspeak_all(:,1) == entry(1);            
                cond2 = peakvspeak_all(:,2) == entry(2);
                cond3 = peakvspeak_all(:,3) == entry(3);            
                cond4 = peakvspeak_all(:,4) == entry(4);
                count = peakvspeak_all(cond1&cond2&cond3&cond4, 5);
                if ~isempty(count)
                    count = count + 1;
                    peakvspeak_all(cond1&cond2&cond3&cond4, 5) = count;
                else
                    peakvspeak_all = [peakvspeak_all; [entry, 1]];                
                end
            end
        end
        
    end % end sample loop

    [res, pos] = sort(peakvspeak_all(:, 5), 'descend');
    sorted_res = peakvspeak_all(pos, :);
    sorted_res(:, 5) = sorted_res(:, 5) ./ length(samples);
    figure;
    hist(sorted_res(:, 5)); 
    title('Posterior similarity distribution');   
    
end
