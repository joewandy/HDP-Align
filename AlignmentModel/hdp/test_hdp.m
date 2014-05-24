clear all; close all; clc;

isOctave = exist('OCTAVE_VERSION', 'builtin') ~= 0;

fprintf('HDP generator\n');
synthdata = hdp_generator_2;
synthdata.ti
synthdata.fi
if isOctave
    fflush(stdout);
end

fprintf('Run HDP\n');
debug = true;
hdp = init_hdp(synthdata, debug);
samples = do_hdp(hdp);

fprintf('Result\n');
print_hdp_result(samples, debug);
