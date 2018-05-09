#!/bin/bash

#
#  this script generates csv format for each chunk type.  the files were created by chunk-record.sh
#
#  column as the following:
#  chunk name, chunk number, capacity, avg. capacity, sealed, avg. sealed, ec copies, normal copies, disk usage
#

if [ $# -lt 1 ]
then
        echo "not enough parameter"
        echo "syntax: $0 <ecs_ip>"
        exit
fi

ecs=$1
chunk_types="LOCAL-REPO-DELETED LOCAL-REPO-HEALTHY PARITY-REPO-HEALTHY COPY-REPO-DELETED COPY-REPO-HEALTHY"

for type in ${chunk_types}
do
    grep ${type} gc-test-${ecs}*.txt | grep GB | sed -e 's/,//g'  -e 's/GB//g' -e 's/│/,/g' > $ecs-$type.csv
done