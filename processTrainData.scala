import org.clulab.processors.fastnlp.FastNLPProcessor
import scala.io.Source
import java.io.File
import java.io.BufferedWriter
import java.io.FileWriter

object ProcessTrainData {

	def main(args:Array[String]) {
		//retrieve text files
		val directory = args(0)
		val trainFiles = new File(directory)
		val txtFiles = trainFiles.listFiles.filter(_.getName.endsWith(".txt"))

		val cdp = new ConLLDataPreproccesor()

		for (txtFile <- txtFiles) {
			//retrieve the corresponding .ann files
			var annFileName = directory + "/" + txtFile.getName.replaceAll(".txt", ".ann")
			var txtFileName = directory + "/" + txtFile.getName

			cdp.posTag(txtFileName)
			cdp.applyNERLabels(txtFileName, annFileName)
		}

	}
}

class ConLLDataPreproccesor() {

	val nlpProc = new FastNLPProcessor()

	//create the output folder upon instantiation
	val outputDir = new File("annotatedFiles")
	outputDir.mkdirs

	def posTag(txtFileName:String) {
		//tag the text file with POS tags
		println("--annotating file: " + txtFileName)
		val src = Source.fromFile(txtFileName)
		var doc = nlpProc.annotate(src.getLines.mkString("\n"))

		var fileString = outputDir + "/" + txtFileName.slice(6, txtFileName.length).replaceAll(".txt", ".pos")
		val bw = new BufferedWriter(new FileWriter(new File(fileString)))

		for (s <- doc.sentences) {
			var words = s.words
			var tags = s.tags.get
			var lemma = s.lemmas.get

			for ((word, i) <- words.zipWithIndex) {
				
				bw.write(word + "\t" + tags(i) + "\t" + lemma(i) + "\n")

			}

			bw.write("\n")
		}

		bw.close
		src.close

	}

	def applyNERLabels(txtFileName:String, annFileName:String) {

		var namedEntities = Source.fromFile(annFileName).getLines.toList
		var posTaggedFile = Source.fromFile("annotatedFiles/" + (txtFileName.slice(6, txtFileName.length).replaceAll(".txt", ".pos")))
		//println("Applying NER labels from" + annFileName)

		var termsList:List[(String, String, Int, Int)] = List() //list to store terms, cats, and positions

		for (line <- namedEntities) {
			var splitData = line.split("\t")

			//there are relation annotations in .ann files; only using terms for now
			if (splitData(0).startsWith("T") == true) {
				var catPosition = splitData(1)
				var category = defineCategory(catPosition) //category and position
				var text = splitData(2) //string (possibly multi-word)
				var startPosition = catPosition.split(" ")(1).toInt
				var endPosition = catPosition.split(" ")(2).toInt

				termsList = termsList :+ (text, category, startPosition, endPosition)
			}
		}

		var fileString = outputDir + txtFileName.slice(6, txtFileName.length).replaceAll(".txt", ".conll")
		val bw = new BufferedWriter(new FileWriter(new File(fileString)))

		var counter = 0
		var positionCounter = 0
		for (line <- posTaggedFile.getLines) {

			var word = line.split("\t")(0)
			var wordLength = word.length
			var foundFlag = false
			var termIdx = 0

			if (line == "") {
				//for blank lines, just skip and move to the next
				foundFlag = true
				bw.write("\n")
			}

			while (foundFlag == false && termIdx < termsList.length) {

				var termData = termsList(termIdx)
				var term = termData._1
				var category = termData._2

				if (term contains word) {
					//found a match, so check the position
					var termStart = termData._3
					var termEnd = termData._4

					if (positionCounter >= termStart && positionCounter <= termEnd) {
						//it's in the right place so give it the appropriate label
						if (term.startsWith(word) == true) {
							//B-category
							bw.write(line.stripLineEnd + "\tB-" + category + "\n")
							foundFlag = true
						} else if (term contains word) {
							//I-category; middle or end of term
							bw.write(line.stripLineEnd + "\tI-" + category + "\n")
							foundFlag = true
						}
					} else {
						termIdx += 1 //word matched but position didn't, move on
					}
				} else {
					termIdx += 1 //no match, check the next term
				}
			}

			if (foundFlag == false) {
				//if we get here and the flag isn't set, then it's not a term or part of a term
				//println("Why am I writing an O here?")
				//println(line)
				bw.write(line.stripLineEnd + "\tO\n")
			}

			//position incrementation logic
			if (word == "," || word == "/") {
				positionCounter += wordLength
			} else {
				positionCounter += (wordLength + 1)
			}

		}
		posTaggedFile.close
		bw.close

		val oldFile = new File(fileString.replaceAll(".conll", ".pos"))
		oldFile.delete
	}

	def defineCategory(rawCat:String):String = {
		val cat = rawCat.split(" ")(0)
		var letter:String = ""
		if (cat == "Process") {
			letter = "P"
		} else if (cat == "Material") {
			letter = "M"
		} else if (cat == "Task") {
			letter = "T"
		}

		letter

	}
}