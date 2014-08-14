function sorted_res = print_hdp_result(samples, cluster_rt, cluster_mass, debug, file_path, label)

    last = samples(end);

    if debug

        fprintf('I = %d\n\n', last.I);
        fprintf('[ti, fi] =\n\n');        
        [sorted_ti, pos] = sort(last.ti);
        sorted_fi = last.fi(pos);
        disp([sorted_ti', sorted_fi']);
        fprintf('\n');
    
        % across all files, for every peak, find its parent metabolite
        res = zeros(last.I, last.gtI);
        for j=1:last.J

            Z = last.file{j}.Z;
            top_Z = last.file{j}.top_Z;

            N = last.file{j}.N;
            rts = last.file{j}.data_rt;
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
        h = figure;
        imagesc(res); colorbar;
        title('Inferred vs. Ground Truth');
        ylabel('Inferred Metabolite ID');
        xlabel('True Metabolite ID');
        saveas(h, [file_path, '/', label, '_gt.png']);

    end

    % print the number of top components across samples
    topcount = [];
    for s=1:length(samples)
        sample = samples(s);
        topcount = [topcount, sample.I];
    end
    h = figure;
    title('No. of top components');
    hist(topcount);
    saveas(h, [file_path, '/', label, '_top_components.png']);    

    % across all samples, find out which peak is put together with other peaks
    % shitty code ..
    peakvspeak_map = java.util.HashMap; % key -> count
    for s = 1:length(samples)
    
        fprintf('Processing sample %d\n', s);
    
        sample = samples(s);
        for i = 1:sample.I

            if cluster_rt & cluster_mass % clustering by RT and mass

                metabolite = sample.metabolite(i);
                peak_data = metabolite.peak_data;
                files = metabolite.peak_j';
                peakids = [peak_data.peakID]';
                for a = 1:metabolite.A
                    % find all peaks under this mass cluster
                    pos = find(metabolite.V==a);
                    component_peaks = [peakids(pos), files(pos)]; % [peakID,file]
                    % convert from matrix of [peakID,file] into matrix of [peakID1,file1,peakID2,file2]
                    for n1 = 1:size(component_peaks, 1)
                        for n2 = 1:size(component_peaks, 1)
                            this_peakID = num2str(component_peaks(n1, 1));
                            this_file = num2str(component_peaks(n1, 2));
                            that_peakID = num2str(component_peaks(n2, 1));
                            that_file = num2str(component_peaks(n2, 2));
                            if this_file == that_file
                                continue
                            end
                            encoded = [this_peakID, '-', this_file, '-', that_peakID, '-', that_file];
                            if peakvspeak_map.containsKey(encoded)
                                count = peakvspeak_map.get(encoded);
                                count = count + 1;
                                peakvspeak_map.put(encoded, count);
                            else
                                peakvspeak_map.put(encoded, 1);
                            end                                     
                        end
                    end
                end

            elseif cluster_rt % clustering by RT only
            
                % across all files, get all the peaks under this top component
                component_peaks = []; % [peakID,file] for all j

                for j = 1:sample.J
                    top_Z = sample.file{j}.top_Z;
                    cluspos = find(top_Z(:, i));
                    if ~isempty(cluspos) % top component might be empty in some file
                        Z = sample.file{j}.Z;
                        for kidx = length(cluspos)
                            k = cluspos(kidx);
                            % get all the peaks under this cluster                            
                            peakpos = find(Z(:, k));
                            peakids = sample.file{j}.peakID(peakpos);
                            temp = [peakids, repmat(j, length(peakids), 1)]; % peakID,file for this j
                            component_peaks = [component_peaks; temp]; % combine
                        end
                    end
                end

                % convert from matrix of [peakID,file] into matrix of [peakID1,file1,peakID2,file2]
                for n1 = 1:size(component_peaks, 1)
                    for n2 = 1:size(component_peaks, 1)
                        this_peakID = num2str(component_peaks(n1, 1));
                        this_file = num2str(component_peaks(n1, 2));
                        that_peakID = num2str(component_peaks(n2, 1));
                        that_file = num2str(component_peaks(n2, 2));
                        if this_file == that_file
                            continue
                        end
                        encoded = [this_peakID, '-', this_file, '-', that_peakID, '-', that_file];
                        if peakvspeak_map.containsKey(encoded)
                            count = peakvspeak_map.get(encoded);
                            count = count + 1;
                            peakvspeak_map.put(encoded, count);
                        else
                            peakvspeak_map.put(encoded, 1);
                        end                                     
                    end
                end
                            
            elseif cluster_mass % clustering by mass only

            end
               
        end % end top component loop
                
    end % end sample loop

    % convert the map into array
    fprintf('Preparing data structure\n');
    entries = peakvspeak_map.entrySet.toArray;
    peakvspeak = zeros(length(entries), 5);
    for counter = 1:length(entries)

        entry = entries(counter);
        keyStr = entry.getKey;

        % toks = strsplit(keyStr, '-');
        toks = strread(keyStr, '%s', 'delimiter', '-')';

        keyArr = str2double(toks);

        value = entry.getValue;
        peakvspeak(counter, :) = [keyArr, value];

    end

    [res, pos] = sort(peakvspeak(:, 5), 'descend');
    sorted_res = peakvspeak(pos, :);
    sorted_res(:, 5) = sorted_res(:, 5) ./ length(samples);
    h = figure;
    hist(sorted_res(:, 5)); 
    title('Posterior similarity distribution');   
    saveas(h, [file_path, '/', label, '_posterior_similarity.png']);               
        
end
