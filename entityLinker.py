from acronym_extractor import getAcronyms
import os
import math

def main():
	#combining the acronym and synonym dicts that come out into one for easier searching as they are the same relation
	#instation of counts
	hyponymDict = {}
	synonymDict = {}
	goldSynonyms = 0
	goldHyponyms = 0
	goldEntCount = 0
	predictedSynonyms = 0
	correctSynonyms = 0
	predictedHyponyms = 0
	correctHyponyms = 0
	testFiles = os.listdir('relationExtractionCode/splitData/testData/')
	trainFiles = os.listdir('relationExtractionCode/splitData/trainData/')

	#training: build dict of relations contained in training data & all acronyms in training data
	for file in trainFiles:
		if file[-4:] == '.ann':
			tempDict1, tempDict2, tempList1= buildDicts('relationExtractionCode/splitData/trainData/' + file)
			hyponymDict.update(tempDict1)
			synonymDict.update(tempDict2)
			synonymDict.update(getAcronyms('relationExtractionCode/splitData/trainData/' + file[:-4] + '.txt'))



	#testing: get any possible acronyms in test data and update the acronymDict accordingly
	#then get a list of the entities for each file. test each of those entities for entries in
	#the lookup dicts and mark them if that is the case
	for file in testFiles:
		if file[-4:] == '.ann':
			synonymDict.update(getAcronyms('relationExtractionCode/splitData/testData/' + file[:-4] + '.txt'))
			tempHyponymDict, tempSynonymDict, tempList1= buildDicts('relationExtractionCode/splitData/testData/' + file)
			goldHyponyms += len(tempHyponymDict)
			goldSynonyms += len(tempSynonymDict)
			goldEntCount += len(tempList1)
			#step through entities to predict any relations, build a relation, and check it
			for entity in tempList1:
				for key,value in synonymDict.items():
					if entity == key:
						predictedSynonyms += 1
						if entity in tempSynonymDict:
							if tempSynonymDict[entity] == value:
								correctSynonyms += 1
					elif entity == value:
						predictedSynonyms += 1
						if value in tempSynonymDict:
							correctSynonyms+= 1
				
				for key,value in hyponymDict.items():
					if entity == key:
						predictedHyponyms += 1
						if entity in tempSynonymDict:
							if tempHyponymDict[entity] == value:
								correctHyponyms += 1
					elif entity == value:
						predictedHyponyms += 1
						if value in tempHyponymDict:
							correctHyponyms += 1



	#handle cases where nothing is predicted for one, the other, or both, so the program fails gracefully
	if predictedHyponyms == 0 and predictedSynonyms == 0:
		predictedHyponyms = 1
		predictedSynonyms = 1
		print("NO HYPONYMS or SYNONYMS PREDICTED")
	elif (predictedHyponyms == 0):
		predictedHyponyms = 1
		print("NO HYPONYMS PREDICTED")
	elif (predictedSynonyms == 0):
		predictedSynonyms = 1
		print("NO SYNONYMS PREDICTED")


	hypoFalseNeg = goldHyponyms-correctHyponyms
	synoFalseNeg = goldSynonyms-correctSynonyms

	hypoRecall = correctHyponyms/(correctHyponyms + hypoFalseNeg)
	synoRecall = correctSynonyms/(correctSynonyms + synoFalseNeg)
	totalRecall = (correctSynonyms+correctHyponyms)/(correctHyponyms+correctSynonyms+hypoFalseNeg+synoFalseNeg)

	hypoPrecision = correctHyponyms/predictedHyponyms
	synoPrecision = correctSynonyms/predictedSynonyms
	totalPrecision = (correctSynonyms+correctHyponyms)/(predictedSynonyms+predictedHyponyms)

	hypoF1 = math.pow((.5*((1/hypoPrecision) + (1/hypoRecall))),-1)
	#synoF1 = math.pow((.5*((1/synoPrecision) + (1/synoRecall))),-1)
	totalF1 = math.pow((.5*((1/totalPrecision) + (1/totalRecall))),-1)

	#added because of a divide by 0 error
	synoF1 = 0

	print("Predicted Synonyms: {}".format(predictedSynonyms))
	print("Predicted Hyponyms: {}".format(predictedHyponyms))
	print(goldSynonyms)
	print(goldHyponyms)
	print(goldEntCount)
	print(correctHyponyms)

	print("Hyponym Recall is: {}, Synonym Recall is: {}, Total Recall is: {}".format(hypoRecall,synoRecall,totalRecall))
	print("Hyponym Precision is: {}, Synonym Precision is: {}, Total Precision is: {}".format(hypoPrecision,synoPrecision,totalPrecision))
	print("Hyponym F1 is: {}, Synonym F1 is: {}, Total F1 is: {}".format(hypoF1,synoF1,totalF1))




def buildDicts(fileName):
	hyponymDict = {}
	synonymDict = {}
	entityDict = {}
	entityList=[]
	with open(fileName) as a:
		for line in a:
			#add an entity
			textSplit = line.split("\t")
			if ("T" in textSplit[0]):
				entityDict[textSplit[0]] = textSplit[2]
				entityList.append(textSplit[2])
			#handle hyponyms
			if ("R" in textSplit[0]):
				argSplit = textSplit[1].split(" ")
				arg1 = entityDict[argSplit[1][5:]]
				arg2 = entityDict[argSplit[2][5:]]
				hyponymDict[arg1] = arg2
			#handle synonyms
			if ("*" in textSplit[0]):
				argSplit = textSplit[1].split(" ")
				arg1 = entityDict[argSplit[1]]
				arg2 = entityDict[argSplit[2].strip()]
				synonymDict[arg1] = arg2
	return hyponymDict, synonymDict, entityList



if __name__=='__main__': main()