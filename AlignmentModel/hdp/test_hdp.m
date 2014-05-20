clear all; close all; clc;

fprintf('HDP generator\n');
synthdata = hdp_generator_2
synthdata.ti
synthdata.fi

fprintf('Run HDP\n');
hdp = init_hdp(synthdata);
samples = do_hdp(hdp);

fprintf('Result\n');
print_hdp_result(samples);
