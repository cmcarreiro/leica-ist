#!/bin/bash

inputdir=$1
outputdir=$2
maxthreads=$3
numbuckets=$4

for inputfile in $inputdir/*.txt
do
    echo InputFile=​$(basename $inputfile) ​NumThreads=​1
    ./tecnicofs-nosync $inputfile $outputdir/$(basename $inputfile .txt)-1.txt 1 1 | grep "TecnicoFS completed in"
    for j in $(seq 2 $maxthreads)
    do
        echo InputFile=​​$(basename $inputfile) ​NumThreads=​$j
        ./tecnicofs-mutex $inputfile $outputdir/$(basename $inputfile .txt)-$j.txt $j $numbuckets | grep "TecnicoFS completed in"
    done
done
