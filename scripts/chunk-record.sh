#!/bin/bash

if [ $# -lt 1 ]
then
        echo "not enough parameter"
        echo "syntax: $0 <ecs_ip>"
        exit
fi

ecs=$1
while [ true ]
do
        suffix=`date +%m-%d-h%H-%M`
        name="gc-test-${ecs}-${suffix}.txt"
        java -jar ecs-shell-1.0-SNAPSHOT.jar batch.txt -DECS=${ecs} | tee -a $name
        date
        echo sleeping for 1 hours
        sleep 3600
done