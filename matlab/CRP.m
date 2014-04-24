function [assignments, counts] = CRP(alpha, N)

% initialise everything
assignments = zeros(N,1); % assignments for each object
counts = zeros(N,1); % table counts (N is the max possible K)
assignments(1) = 1; % assign object 1 to table 1
counts(1) = 1; % adjust counts
counts(2) = alpha; % "fake" counts for table K+1
K = 1; % number of unique clusters

% sequentially assign other objects via CRP
for i = 2:N

    % generate random number, and convert to a "quasi-count"
    u = rand; % generate uniform random number
    u = u * (i - 1 + alpha); % multiply by the CRP normalising contant

    % find the corresponding table
    z = 1; % indexing variable for the table
    while u > counts(z)
        u = u - counts(z); % subtract off that probability mass
        z = z + 1; % move to the next table
    end

    % record the outcome and adjust
    assignments(i) = z; % make the assignment
    if z == K+1 % if it’s a new table
        counts(z) = 1; % assign real count
        counts(z+1) = alpha; % move the "fake" counts to next table
        K = K+1; % update the number of clusters
    else % if it’s an old table
        counts(z) = counts(z) + 1; % increment count
    end

end

% truncate the counts matrix for neatness. also, this takes
% care of the "fake" count mass in count(K+1)
counts = counts(1:K);
