import sys
import csv
import re

header_keys = ['title','author','release_date','ebook_id','language']

def is_header(line):
	for entry in ['Title:','Author:','Date:','[','Language:']:
		if entry in line:
			return True
	return False 

ebooks_file = sys.argv[1]
ebooks = open(ebooks_file, 'r')

header = {}
csv_buffer = []

line = ebooks.readline()
while line != '':
	if 'Title:' in line:
		while not "*** END OF THE" in line:
			if "Title:" in line:
				title = line[7:-2]
				header['title'] = title.strip()
			elif "Author:" in line:
				author = line[8:-2]
				header['author'] = author.strip()
			elif "Release Date:" in line:
				end_of_date = line.index('[')-1
				date = line[14:end_of_date]
				header['release_date'] = date.strip()
				id_index = line.index('#')
				end_of_id = line.index(']')
				header['ebook_id'] = line[id_index+1:end_of_id]
			elif "Language:" in line:
				language = line[10:-2]
				header['language'] = language
			elif "*** START OF THE" in line:
				for key in header_keys:
					if key not in header.keys():
						header[key] = 'null'
				for key in header_keys:
					csv_buffer.append(header[key])	
				body = ""
				line = ebooks.readline()
				while "*** END OF THE" not in line:
					body+=line
					line = ebooks.readline()
				csv_buffer.append(body)
				break
			line = ebooks.readline()
		with open('ebook.csv', 'a') as csvfile:
			writer = csv.writer(csvfile, quoting=csv.QUOTE_MINIMAL)
			writer.writerow(csv_buffer)
			csvfile.close()		
			header = {}
			csv_buffer = []
	else:
		line = ebooks.readline()
ebooks.close()
