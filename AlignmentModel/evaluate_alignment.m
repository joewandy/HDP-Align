function out = evaluate_alignment(filename1, filename2, method)

    more off;
    
    # no of iterations to average f-1 score (ps. can consider vector distance ?)
    iter = 3;

    # mass kernel width, fixed to 3 for now    
    msdev = 3;
    
    # for gibbs sampling 
    num_samples = 100;
    
    results = zeros(iter, 5);
    for j = 1:iter
    
        printf('\nIteration #%d', j);    

        # alignment by matching the most similar cluster to this one
        result = align_cluster(filename1, filename2, msdev, num_samples);
        results(j, :) = [ result.f1, result.tpr, result.fpr result.tp result.fp ];

    endfor

    results
    
    printf('Means');
    avg_score = mean(results, 1); % along the first dimension
    out = [ avg_score(1), avg_score(2), avg_score(3), avg_score(4), avg_score(5) ];
    printf('\tf1=%.3f\ttpr=%.3f\tfpr=%.3f\ttp=%d\tfp=%d\n', out(1), out(2), out(3), out(4), out(5));
