#!/bin/bash
reset

set -e #aborts when any command fails
set -x #enable echo

#build
mvn clean package

#run
java -Xmx4g -cp target/FG.jar fusionGraph.Main genFusedGraphs=1,rankFromFusedGraphsByQuerying=1,similarityFusionGraph=WGU,dataset=Soccer,descriptors=BIC/ACC
