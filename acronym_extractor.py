import os
from nltk.tokenize import word_tokenize
import re

def main():
	pattern = r'^[A-Z]{3,5}'

	files = os.listdir('splitData/trainData')
	acr_dict = {}
	for file in files:
		if file[-4:] == '.txt':
			f = open('splitData/trainData/' + file)
			text = f.read()
			tokens = word_tokenize(text)
			for t in tokens:
				if re.search(pattern, t):
					match = find_words_for_acr(t, tokens)
					acr_dict.update(match)
	print(acr_dict)

def find_words_for_acr(acronym, tokens):
	num_words = len(acronym)
	match = {}
	i = 0
	while i < (len(tokens) - num_words):
		n_gram = []
		for j in range(i, i + num_words):
			n_gram.append(tokens[j])
		letters = [word[0] for word in n_gram]
		potential_acronym = ''.join(letters).upper()
		if acronym == potential_acronym:
			match = {acronym: n_gram}
		i += 1
	return match

if __name__=='__main__': main()