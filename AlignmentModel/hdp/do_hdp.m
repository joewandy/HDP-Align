%% data is N x J matrix
function samples = do_hdp(hdp, cluster_rt, cluster_mass, debug)
    
    % check if octave or matlab
    isOctave = exist('OCTAVE_VERSION', 'builtin') ~= 0;
    samples = [];
    for s = 1:hdp.NSAMPS

        tic;
                       

        if cluster_rt & cluster_mass

            % update peak to cluster assignment
            hdp = assign_peaks_mass_rt(hdp, debug);                        

            % update mixture component parameters
            hdp = update_parameters_mass_rt(hdp, debug);
            
            % print stuffs
            time_taken = toc;        
            if s > hdp.BURN_IN
                fprintf('time=%5.2fs S#%05d I=%d ', time_taken, s, hdp.I);
                samples = [samples, hdp]; 
            else
                fprintf('time=%5.2fs B#%05d I=%d ', time_taken, s, hdp.I);        
            end        
            all_A = [];
            for i = 1:hdp.I
                all_A = [all_A, hdp.metabolite(i).A];
            end
            disp(strrep(['all_A = [' sprintf(' %3d', all_A) ']'], ']', ' ]'));
        
        elseif cluster_rt

            % update peak to cluster assignment
            hdp = assign_peaks_rt(hdp, debug);                

            % update mixture component parameters
            hdp = update_parameters_rt(hdp, debug);

            % print stuffs
            time_taken = toc;        
            if s > hdp.BURN_IN
                fprintf('time=%5.2fs S#%05d I=%d\n', time_taken, s, hdp.I);
                samples = [samples, hdp]; 
            else
                fprintf('time=%5.2fs B#%05d I=%d\n', time_taken, s, hdp.I);        
            end        
        
        elseif cluster_mass
        
        end                                                       
                                       
        % show message in octave before program ends
        if isOctave
            fflush(stdout);
        end
        
    end

end
