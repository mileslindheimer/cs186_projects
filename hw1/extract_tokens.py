import sys
import csv
import re

csv.field_size_limit(sys.maxint)
csvfile_name = sys.argv[1]
with open('ebook.csv', 'r') as readfile:
	reader = csv.DictReader(readfile, quoting=csv.QUOTE_MINIMAL)
	for row in reader:
		ebook_id = row['ebook_id']
		tokens = row['body']
		tokens = re.split('\W+|[a\-z]', str(tokens))
		#tokens = re.split('\W+', str(tokens))
		with open('tokens.csv', 'a') as writefile:
			for token in tokens:
				token = re.sub('[_]', ' ', token)
				token = token.strip()
				if token.isdigit() or token == '\n' or token == '\r' or token == '\r\n' or token == ' ' or token =='' or (len(token)>1 and (token[0].isdigit() or token[1].isdigit())):
					continue
				if '\r\n' in token:
					token = token[:-2]
				writer = csv.writer(writefile, quoting=csv.QUOTE_MINIMAL)
				writer.writerow([ebook_id, token.strip().lower()])
			writefile.close()
	readfile.close()

