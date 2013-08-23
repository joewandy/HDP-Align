#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
STANDARDS=~/work/research/data/Standards

source $DIR/common.sh
CURRENT=filtered_

DATA=( std1 std2 std3 )
MODE=( NEG POS )
TYPE=( mzMatch random greedy )
#DATA=( std3 )
#MODE=( NEG )
#TYPE=( mzMatch )
for data in ${DATA[@]}; do
for mode in ${MODE[@]}; do
for t in ${TYPE[@]}; do
	$JAVA org.ronandaly.metasign.ExtractClustering $STANDARDS/${CURRENT}labelled/${data}_${mode}.$t.peakml relation.id > $STANDARDS/${CURRENT}clustering/${data}_${mode}.$t.pearson.assignments.csv 
done
done
done

