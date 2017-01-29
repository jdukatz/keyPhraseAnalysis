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
			var chunks = s.chunks.get

			for ((word, i) <- words.zipWithIndex) {
				
				bw.write(word + "\t" + tags(i) + "\t" + chunks(i) + "\n")

			}
		}

		bw.close
		src.close

	}

	def applyNERLabels(txtFileName:String, annFileName:String) {

		var namedEntities = Source.fromFile(annFileName).getLines.toList
		var posTaggedFile = Source.fromFile("annotatedFiles/" + (txtFileName.slice(6, txtFileName.length).replaceAll(".txt", ".pos")))

		var termsList:List[(String, String)] = List() //list to store terms and categorys

		for (line <- namedEntities) {
			var splitData = line.split("\t")

			//there are relation annotations in .ann files; only using terms for now
			if (splitData(0).startsWith("T") == true) {
				var category = defineCategory(splitData(1)) //category and position
				var text = splitData(2) //string (possibly multi-word)

				termsList = termsList :+ (text, category)
			}
		}

		var fileString = outputDir + txtFileName.slice(6, txtFileName.length).replaceAll(".txt", ".conll")
		val bw = new BufferedWriter(new FileWriter(new File(fileString)))

		var counter = 0
		for (line <- posTaggedFile.getLines) {

			var word = line.split("\t")(0)

			try {
				if (termsList(counter)._1.startsWith(word) == true) {
					//B-category
					bw.write(line.stripLineEnd + "\tB-" + termsList(counter)._2 + "\n")
				} else if (termsList(counter)._1.endsWith(word) == true) {
					//I-category
					bw.write(line.stripLineEnd + "\tI-" + termsList(counter)._2 + "\n")
					counter += 1
				} else if ((termsList(counter)._1 contains word) == true) {
					//I-category
					bw.write(line.stripLineEnd + "\tI-" + termsList(counter)._2 + "\n")
				} else { //not a term
					//O
					bw.write(line.stripLineEnd + "\tO\n")
				}
			} catch {
				//we've labelled all the terms from the .ann, so put O on everything else
				case iob:IndexOutOfBoundsException => bw.write(line.stripLineEnd + "\tO\n")
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