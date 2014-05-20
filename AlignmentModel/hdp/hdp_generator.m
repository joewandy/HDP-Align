function data = hdp_generator

    %% Generate some data from a HDP
    base_mean = 0;
    base_prec = 1e-3;

    obs_prec = 1;

    J = 5; % Number of files
    N = 100; % 100 observations in all files
    top_components = [];
    top_counts = [];
    nTop = 0;

    alpha = 1; %Table DP
    gamma = 10; % Component DP

    for j = 1:J
        Z{j} = [];
        x{j} = [];
        cluster_counts{j} = [];
        tables_to_top{j} = [];
        nClusters{j} = 0;
        for n = 1:N
            % Choose a table
            probs = [cluster_counts{j} alpha];
            probs = cumsum(probs./sum(probs));
            table_choice = find(rand<=probs,1);
            if table_choice > nClusters{j}
                % Have to make a new table
                nClusters{j} = nClusters{j} + 1;
                Z{j}(n,1) = nClusters{j};
                cluster_counts{j}(nClusters{j}) = 1;
                
                % Choose a top level component for this new table
                probs = [top_counts gamma];
                probs = cumsum(probs./sum(probs));
                top_choice = find(rand<=probs,1);
                
                if top_choice > nTop
                    % Create a new top level
                    nTop = nTop + 1;
                    top_counts(1,nTop) = 1;
                    top_components(1,nTop) = randn./sqrt(base_prec) + base_mean;
                    tables_to_top{j}(nClusters{j}) = nTop;
                    x{j}(n,1) = rand./sqrt(obs_prec) + top_components(tables_to_top{j}(nClusters{j}));
                else
                    % Use the current one
                   tables_to_top{j}(nClusters{j}) = top_choice;
                   top_counts(top_choice) = top_counts(top_choice) + 1;
                   x{j}(n,1) = rand./sqrt(obs_prec) + top_components(tables_to_top{j}(nClusters{j}));
                end
               
            else
                % Add to the current table
                Z{j}(n,1) = table_choice;
                cluster_counts{j}(table_choice) = cluster_counts{j}(table_choice) + 1;
                x{j}(n,1) = rand./sqrt(obs_prec) + top_components(tables_to_top{j}(table_choice));
            end
        end
                
    end

    % Plot the data
    data = [];
    close all
    for j = 1:J
        [f,xi] = ksdensity(x{j});
        plot(xi,f);
        hold all
        data = [x{j}, data];
    end
    
    data
    nTop
    top_components
    top_counts

end
