#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
STANDARDS=~/work/research/data/Standards
source $DIR/common.sh

DATA=( std1 std2 std3 )
MODE=( NEG POS )
#MODEL_TYPES=(corr mixture mixture2 mixture3)
#ALL_TYPES=(mzMatch greedy random corr mixture mixture2 mixture3)

for data in ${DATA[@]}; do
for mode in ${MODE[@]}; do
$JAVA mzmatch.ipeak.filter.SimpleFilter -i $STANDARDS/identified_data/${data}_${mode}.identified.peakml -o $STANDARDS/filtered_data/${data}_${mode}.identified.peakml -minintensity 5000
done
done

