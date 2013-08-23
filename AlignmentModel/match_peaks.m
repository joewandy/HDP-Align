function out = match_peaks(from_cluster, to_cluster, Z1, Z2, synthdata1, synthdata2)

    ppm = 3;
    from_peaks = find(Z1(:, from_cluster))';
    to_peaks = find(Z2(:, to_cluster))';
    no_of_peaks = length(from_peaks);

    to_masses = (to_peaks);
    to_rettime = synthdata2.rettime(to_peaks);
    to_pID = synthdata2.pID(to_peaks);
    to_intenses = synthdata2.intenses(to_peaks);
    
    # the number of peaks matched
    out.no_of_matches = 0;

    for j = 1:no_of_peaks
       from_mass = synthdata1.masses(from_peaks(j));
       delta = compute_ppm(from_mass, ppm);
       rt = synthdata1.rettime(from_peaks(j));
       common = get_peaks_in_mass_range(from_mass, delta, to_peaks, synthdata2.masses);
       if length(common) > 0

           # loop through all found peaks in mass range, matching one by one           
           for k = 1:length(common)

               # these two lines must come in this order
               out.no_of_matches += 1;
               out.peak_alignment(out.no_of_matches, :) = [ from_peaks(j) common(k) 0 0 0 ];

               # set flag for correct alignment
               left = synthdata1.pID(from_peaks(j));
               right = synthdata2.pID(common(k));
               out.peak_alignment(out.no_of_matches, 4) = left;
               out.peak_alignment(out.no_of_matches, 5) = right;
               if left == right
                   out.peak_alignment(out.no_of_matches, 3) = 1;
               endif
               
           endfor
           
       endif
    endfor

