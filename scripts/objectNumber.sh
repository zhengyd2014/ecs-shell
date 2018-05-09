#!/bin/bash
##
#####################################################
##                                                 ##
##       Copyright EMC Corporation  2016           ##
##                                                 ##
## This script will show number of items in bucket ##
##                                                 ##
#####################################################
## PIB 12/14/2016
##
USER=root
PASSWORD=ChangeMe
LINE_MAX=9999
IP=""
STORAGEPOOL=""
rm key
MINPARAMS=3

echo "-----------------------------------"
echo "All the command-line parameters are: "$*""
echo $#

if [ $# -lt "$MINPARAMS" ]
then
  echo
  echo "This script needs at least $MINPARAMS command-line arguments!"
  exit 1
fi


IP=$1
NS=$2
BUCKET=$3

#read -p "Input the IP address of the first node of the ECS: " IP
#read -p "Input the Namespace: " NS
#read -p "Input the Bucket: " BUCKET

printf "'" > key
curl -i -k  https://$IP:4443/login -u $USER:$PASSWORD|grep -Eo 'X-SDS.{254}'|sed 's/\(.*\)../\1/'|awk '{print $1 $2"\x27"}'   >> key


#Get the information from the bucket):
rm ss1.txt
echo curl -s -k -X GET -H "'Content-Type:application/json'"  -H "'ACCEPT:application/json'"  https://$IP:4443/object/billing/buckets/$NS/$BUCKET/info -H $(<key) > ss1.sh
chmod +x ss1.sh
./ss1.sh > ss1.txt
echo ""
echo "ECS:" $IP " Namespace:" $NS " Bucket:" $BUCKET
echo "Total number of objects are " `cat ss1.txt | cut -d ":" -f14 | cut -d "," -f1`

