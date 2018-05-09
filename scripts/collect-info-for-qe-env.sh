#!/bin/bash

if [ $# -lt 1 ] 
then
	echo "$0 <number>"
	exit
fi

sites="10.245.128.41 10.245.128.45 10.245.128.25"
number=$1

for site in $sites
do
	echo "working on $site..."
	java -jar ../build/libs/ecs-shell-1.0-SNAPSHOT.jar -DECS=$site -DNUM=$number resources.txt > ~/Documents/QE-gc-constant-load-test/resource-$site.txt
	java -jar ../build/libs/ecs-shell-1.0-SNAPSHOT.jar -DECS=$site get_delete_repo.txt > ~/Documents/QE-gc-constant-load-test/deleted-repo-$site.txt

done
