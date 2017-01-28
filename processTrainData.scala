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
			//var annFile = new File(annFileName)
			var txtFileName = directory + "/" + txtFile.getName

			println("----POS TAGGING FILES----")
			cdp.posTag(txtFileName)
			//cdp.applyNERLabels(txtFileName, annFileName)
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

		var fileString = outputDir + "/" + txtFileName.slice(6, txtFileName.length)
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
		//annotate text with NER tags from .ann file
		//println("I'm applying NER tags from " + annFile.getName + " to " + txtFile.getName + "!")
		var rawText = Source.fromFile(txtFileName)
		var namedEntities = Source.fromFile(annFileName).getLines.toList
		var posTaggedFile = Source.fromFile("annotatedFiles/" + (txtFileName))
		for (line <- namedEntities) {
			var splitData = line.split("\t")

			//there are relation annotations in .ann files; only using terms for now
			if (splitData(0).startsWith("T") == true) {
				var category = splitData(1) //category and position
				var text = splitData(2) //string (possibly multi-word)
				var positionStart = category.split(" ")(1).toInt
				var positionEnd = category.split(" ")(2).toInt

				var wordInText = rawText.slice(positionStart, positionEnd)


			}
		}
	}

	def outputConllFile() {
		//will take in data and arrange into file
	}
}