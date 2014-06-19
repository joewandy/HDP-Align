clear all; close all; clc;

isOctave = exist('OCTAVE_VERSION', 'builtin') ~= 0;

synthdata = hdp_generator_2;
synthdata.NSAMPS = 20;
synthdata.BURN_IN = 0;

cluster_rt = true;
cluster_mass = true;
debug = true;

fprintf('SYNTHETIC DATA\n\n');
fprintf('I = %d\n\n', synthdata.I);
fprintf('[ti, fi] =\n\n');
[sorted_ti, pos] = sort(synthdata.ti);
sorted_fi = synthdata.fi(pos);
disp([sorted_ti', sorted_fi']);
fprintf('\n');

if isOctave
    fflush(stdout);
end

fprintf('RUN HDP\n\n');
hdp = init_hdp(synthdata, debug);
samples = do_hdp(hdp, cluster_rt, cluster_mass, debug);

print_hdp_result(samples, cluster_rt, cluster_mass, debug, '/home/joewandy/workspace/AlignmentModel/hdp', 'test_hdp');

last = samples(end);
fprintf('\nLAST SAMPLE\n\n');
fprintf('I = %d\n\n', last.I);
fprintf('[ti, fi] =\n\n');
[sorted_ti, pos] = sort(last.ti);
sorted_fi = last.fi(pos);
disp([sorted_ti', sorted_fi']);
fprintf('\n');

fprintf('SYNTHETIC DATA\n\n');
fprintf('I = %d\n\n', synthdata.I);
fprintf('[ti, fi] =\n\n');
[sorted_ti, pos] = sort(synthdata.ti);
sorted_fi = synthdata.fi(pos);
disp([sorted_ti', sorted_fi']);
fprintf('\n');
