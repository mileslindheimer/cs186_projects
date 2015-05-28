#!/bin/bash
# I was getting an infinite loop while running this, but
# it works... I just could figure out how to fix the input so it kept looping
# through all the tokens...
TOKEN_COUNTS=token_counts.csv
IFS=$'\n'
#while read -u 9 line; do
#	word="${line#*,}"
#	word=$(echo $word | tr -d '\r\n')
#	echo -n $word >> $TOKEN_COUNTS
#	echo -n , >> $TOKEN_COUNTS
#	grep -w -c $word 'tokens.csv' >>$TOKEN_COUNTS
#done 9< tmp.csv
