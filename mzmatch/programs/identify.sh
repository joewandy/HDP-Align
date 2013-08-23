#!/bin/bash

DIR="$( cd "$( dirname "$0" )" && pwd )"
STANDARDS=~/work/research/data/Standards

source $DIR/common.sh

#export JAVA="java -Xmx1024M -ea -cp $DIR/../bin:$DIR/../../cmdline/bin:$DIR/../../domsax/bin:$DIR/../../peakml/bin:$DIR/../lib/*:$DIR/../../peakml/lib/*:/Users/rdaly/work/workspacejeeindigo/MetaSign/build/classes/main:/Users/rdaly/work/workspacejeeindigo/MetaSign/lib/*"

CURRENT=filtered_

DATA=( std1 std2 std3 )
MODE=( NEG POS )
#TYPES=(mzMatch)
#MODEL_TYPES=( mixture )
#ALL_TYPES=( mzMatch mixture )

MODEL_TYPES=(mixture mixture2 mixture3)
ALL_TYPES=(mzMatch greedy random mixture mixture2 mixture3)

#$JAVA org.ronandaly.metasign.Cluster -i $STANDARDS/${CURRENT}_output/std3_NEG.identified.peakml \
#	    -o $STANDARDS/$CURRENT/std3_NEG.mzMatch.peakml -ppm 3 -rtwindow 15
#$JAVA org.ronandaly.metasign.Cluster -i $STANDARDS/$CURRENT/std3_NEG.identified.peakml \
#            -o $STANDARDS/$CURRENT/std3_NEG.ignored.peakml -ppm 3 -rtwindow 15 -ignoreIntensity true
#$JAVA org.ronandaly.metasign.Cluster -i $STANDARDS/$CURRENT/std3_NEG.identified.peakml \
#            -o $STANDARDS/$CURRENT/std3_NEG.greedy.peakml -ppm 3 -rtwindow 15 -randomClustering true -parameter 1.0
#$JAVA org.ronandaly.metasign.Cluster -i $STANDARDS/$CURRENT/std3_NEG.identified.peakml  \
#            -o $STANDARDS/$CURRENT/std3_NEG.random.peakml -ppm 3 -rtwindow 15 -randomClustering true -parameter 0.5

echo "data,method,numberCovered,numberCoveredAsBP,numberClusters,numberNonSingletonClusters,numberCoveredSingleton,numberCoveredNonSingleton,truePositives,falsePositive,trueNegative,falseNegative,tpr,fpr,f1,mcc,ri,ba"

for data in ${DATA[@]}; do
for mode in ${MODE[@]}; do
for t in ${ALL_TYPES[@]}; do
	echo -n "${data}_${mode},"
	echo -n "$t,"
	$JAVA org.ronandaly.metasign.CountIds -i $STANDARDS/${CURRENT}identified_molecules/${data}_${mode}.$t.peakml
done
done
done
exit



for data in ${DATA[@]}; do
for mode in ${MODE[@]}; do
	$JAVA org.ronandaly.metasign.Cluster -i $STANDARDS/${CURRENT}output/${data}_${mode}.identified.peakml \
		    -o $STANDARDS/${CURRENT}output/${data}_${mode}.mzMatch.peakml -ppm 3 -rtwindow 15
#	$JAVA org.ronandaly.metasign.Cluster -i $STANDARDS/${CURRENT}output/${data}_${mode}.identified.peakml \
#	            -o $STANDARDS/${CURRENT}output/${data}_${mode}.ignored.peakml -ppm 3 -rtwindow 15 -ignoreIntensity true
	$JAVA org.ronandaly.metasign.Cluster -i $STANDARDS/${CURRENT}output/${data}_${mode}.identified.peakml \
	            -o $STANDARDS/${CURRENT}output/${data}_${mode}.greedy.peakml -ppm 3 -rtwindow 15 -randomClustering true -parameter 1.0
	$JAVA org.ronandaly.metasign.Cluster -i $STANDARDS/${CURRENT}output/${data}_${mode}.identified.peakml  \
	            -o $STANDARDS/${CURRENT}output/${data}_${mode}.random.peakml -ppm 3 -rtwindow 15 -randomClustering true -parameter 0.5
	for model_type in ${MODEL_TYPES[@]}; do
		$JAVA org.ronandaly.metasign.AddClustering $STANDARDS/${CURRENT}output/${data}_${mode}.identified.peakml \
			$STANDARDS/${CURRENT}clustering/${data}_${mode}.${model_type}.long.pearson.assignments.csv relation.id > $STANDARDS/${CURRENT}output/${data}_${mode}.${model_type}.peakml
	done
	for t in ${ALL_TYPES[@]}; do
		#echo "Looking at $t"
		$JAVA org.ronandaly.metasign.LabelClusters -i $STANDARDS/${CURRENT}output/${data}_${mode}.$t.peakml -o $STANDARDS/${CURRENT}labelled/${data}_${mode}.$t.peakml -ppm 3
		$JAVA mzmatch.ipeak.util.Identify -i $STANDARDS/${CURRENT}labelled/${data}_${mode}.$t.peakml -o $STANDARDS/${CURRENT}identified_molecules/${data}_${mode}.$t.peakml -ppm 3 -databases $STANDARDS/${data}.xml
		#echo -n "${data}_${mode},"
		#echo -n "$t,"
		#$JAVA org.ronandaly.metasign.CountIds -i $STANDARDS/identified_molecules/${data}_${mode}.$t.peakml
	done

		#$JAVA mzmatch.ipeak.util.Identify -i $STANDARDS/labelled/${data}_${mode}.corr.peakml -o $STANDARDS/identified_molecules/${data}_${mode}.corr.peakml -ppm 3 -databases $STANDARDS/${data}.xml
	#$JAVA org.ronandaly.metasign.CountIds -i $STANDARDS/identified_molecules/${data}_${mode}.corr.peakml
done
done








exit





