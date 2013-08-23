% load and parse synthetic data
function out = load_data(filename)

    synthdata = csvread(filename);
    masses = synthdata(:, 1);
    intenses = synthdata(:, 2);
    rettime = synthdata(:, 3);
    pID = synthdata(:, size(synthdata, 2));
    Q = synthdata(:, 4:size(synthdata,2)-1);

    % fix data formatting
    Q(isnan(Q)) = 0;

    out.masses = masses;
    out.intenses = intenses;
    out.rettime = rettime;
    out.pID = pID;
    out.Q = Q;
