function out = evaluate_alignment(filename1, filename2, method)

    more off;
    
    # no of iterations to average f-1 score (ps. can consider vector distance ?)
    iter = 3;

    # mass kernel width, fixed to 3 for now    
    msdev = 3;
    
    # for gibbs sampling 
    num_samples = 100;
    
    results = zeros(iter, 8);
    for j = 1:iter
    
        printf('\nIteration #%d', j);    

        # alignment by matching the most similar cluster to this one
        result = align_cluster(filename1, filename2, msdev, num_samples);
        results(j, :) = [ result.f1 result.precision result.recall, result.fpr result.tp result.fp result.tn result.fn ];

    endfor

    results
    
    printf('Means\n');
    avg_score = mean(results, 1); % along the first dimension
    out = [ avg_score(1), avg_score(2), avg_score(3), avg_score(4), avg_score(5), avg_score(6) avg_score(7) avg_score(8) ];
    printf('\tf1=%.3f precision=%.3f recall(TPR)=%.3f fpr=%.3f\n', out(1), out(2), out(3), out(4));
    printf('\ttp=%d fp=%d tn=%d fn=%d\n', out(5), out(6), out(7), out(8));
