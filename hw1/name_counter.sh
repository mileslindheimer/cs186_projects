#!/bin/bash
TOKEN_COUNTS=token_counts.csv
IFS=$'\n'
while read -r NAME;
do
	grep -i ^$NAME,. $TOKEN_COUNTS >> $1 
done
