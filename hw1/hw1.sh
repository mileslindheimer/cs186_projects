#!/bin/bash
# bash command-line arguments are accessible as $0 (the bash script), $1, etc.
# echo "Running" $0 "on" $1
#echo "Replace the contents of this file with your solution."
echo "Starting..."
EBOOK_CSV=ebook.csv
TOKENS_CSV=tokens.csv
TOKEN_COUNTS=token_counts.csv
NAME_COUNTS=name_counts.csv
POPULAR_NAMES=popular_names.txt
# rest all but header in ebook_csv, then parse ebooks
read -r firstline < $EBOOK_CSV
echo $firstline > $EBOOK_CSV
echo "parsing ebooks"
python parse_ebook.py $1
# reset tokens_csv, extract tokens
read -r firstline<$TOKENS_CSV
echo $firstline > $TOKENS_CSV
echo "extracting tokens"
python extract_tokens.py $EBOOK_CSV
# reset token_counts, count tokens
read -r firstline < $TOKEN_COUNTS
echo $firstline > $TOKEN_COUNTS
echo "counting tokens"
cat $TOKENS_CSV | sort | uniq > tmp.csv
./count_tokens.sh
#reset name_counts, count names
read -r firstline < $NAME_COUNTS
echo $firstline > $NAME_COUNTS
echo "counting names"
cat $POPULAR_NAMES | ./name_counter.sh $NAME_COUNTS
echo "done"
exit 0
