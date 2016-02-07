import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import org.omg.Messaging.SyncScopeHelper;

import com.aliasi.corpus.XValidatingObjectCorpus;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;
import com.aliasi.classify.Classified;
import com.aliasi.corpus.ObjectHandler;
import com.aliasi.crf.ChainCrfChunker;
import com.aliasi.crf.ChainCrfFeatureExtractor;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.io.LogLevel;
import com.aliasi.io.Reporter;
import com.aliasi.io.Reporters;
import com.aliasi.stats.AnnealingSchedule;
import com.aliasi.stats.RegressionPrior;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

public class GeneMatcher {
	static final double CHUNK_SCORE = 1.0;
	static final int MAX_N_GRAM = 8;
	static final int NUM_CHARS = 256;
	static final double LM_INTERPOLATION = MAX_N_GRAM; // default behavior
	private MapDictionary<String> dictionary;
	private TokenizerFactory myTokenizerFactory;
	private ExactDictionaryChunker dictionaryChunker;
	private boolean isTagged = true;
	private ArrayList<String> stopwords;
	private ChainCrfChunker crfChunker;
	private ArrayList<String> resultTagger;
	private ArrayList<String> resultTaggerTag = new ArrayList<String>();
	private ArrayList<String> res = new ArrayList<String>();

	public GeneMatcher(ChainCrfChunker crfChunker) {
		this.crfChunker = crfChunker;

		// TODO Auto-generated constructor stub
	}

	private void init() {

		// Read Dictionary
		dictionary = this.readDictionary("Ressources/dyctionary_genenames.txt");
		stopwords = readStopwords("Ressources/stopwords");

		dictionary = removeStopwords(dictionary, stopwords);

		Content content = readFile("Ressources/test5_not_annotated.iob");
		
		Content annotatedContent = readFile("Ressources/train5_annotated.iob");
		// content = removeStopwords(content, stopwords);
		System.out.println("chunking...");
		chunkContent(content);

		/*
		 * for (String a : resultTagger){
		 * 
		 * System.out.println(a); }
		 */

		writeResult(resultTagger, "result.iob");

		// evaluate(content);
	}

	public MapDictionary<String> readDictionary(String dic_path) {
		MapDictionary<String> dictionary = new MapDictionary<String>();
		Scanner scan;
		try {
			scan = new Scanner(new File(dic_path));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				dictionary.addEntry(new DictionaryEntry<String>(line, "gene", CHUNK_SCORE));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return dictionary;
	}

	public ArrayList<String> readStopwords(String filename) {
		ArrayList<String> words = new ArrayList<String>();
		Scanner scan;
		try {
			scan = new Scanner(new File(filename));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				words.add(line);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return words;
	}

	// TODO Handle tags
	private MapDictionary<String> removeStopwords(MapDictionary<String> dictionary, ArrayList<String> stopwords) {
		MapDictionary<String> newList = new MapDictionary<String>();
		for (DictionaryEntry<String> line : dictionary) {
			if (!containsIgnoreCase(stopwords, line.phrase())) {
				newList.addEntry(line);
			}
		}
		return newList;
	}

	private void chunk(String text) {
		Chunking chunking = crfChunker.chunk(text);
		CharSequence cs = chunking.charSequence();

		// System.out.println("cs:"+ cs);
		// System.out.println("ChunkSet"+ chunking.chunkSet());
		String[] te = text.split(" ");

		if (chunking.chunkSet().size() == 0) {

			for (String word : te) {
				resultTagger.add(word + "\tO");
				resultTaggerTag.add("O");
			}
		}
		String lastMatch = null;
		Boolean passedLast = false;
		for (Chunk chunk : chunking.chunkSet()) {
			passedLast = false;
			int start = chunk.start();
			int end = chunk.end();
			CharSequence str = cs.subSequence(start, end);
			double distance = chunk.score();
			String match = chunk.type();
			System.out.printf("%15s  %15s   %8.1f\n", str, match, distance);
			for (String word : te) {
				if (lastMatch == null) {
					passedLast = true;
				} else if (word.equals(lastMatch)) {
					passedLast = true;
					continue;
				}
				if (passedLast) {
					if (word.equals(str)) {
						resultTagger.add(word + "\t" + match);

						// System.out.println(word + "\t" + match);

						resultTaggerTag.add(match);
						lastMatch = str.toString();
						break;

					} else {

						// System.out.println(word + "\tO");
						resultTagger.add(word + "\tO");
						resultTaggerTag.add("O");
					}
				}
			}
		}

	}

	public void evaluate(Content content) {
		res = content.getTags();
		ArrayList<String> word = content.getSentences();
		System.out.println(res.size());
		System.out.println(resultTaggerTag.size());
		int FP = 0;
		int TP = 0;
		int FN = 0;
		int TN = 0;
		if (res.size() == resultTaggerTag.size()) {
			for (int i = 0; i < res.size(); i++) {

				// System.out.println(resultTaggerTag.get(i));
				if (res.get(i).equals("O") && resultTaggerTag.get(i).equals("O"))
					TN++;
				if (res.get(i).equals("O") && resultTaggerTag.get(i).equals("B-protein"))
					FP++;
				if (res.get(i).equals("B-protein") && resultTaggerTag.get(i).equals("O")) {
					System.out.println(word.get(i));
					FN++;
				}
				if (res.get(i).equals("B-protein") && resultTaggerTag.get(i).equals("B-protein"))
					TP++;
			}
			double precision = TP * 1.0 / (TP + FP);
			double recall = TP * 1.0 / (TP + FN);
			double f = 2 * recall * precision / (recall + precision);
			System.out.println(TN);
			System.out.println(TP);
			System.out.println(FP);
			System.out.println(FN);
			System.out.println("Precision: " + precision);
			System.out.println("Recal: " + recall);
			System.out.println("F-Measure: " + f);
		} else {
			System.out.println("Wrong Size");
		}
	}

	public boolean containsIgnoreCase(ArrayList<String> arr, String s) {
		for (String el : arr) {
			if (el.equalsIgnoreCase(s)) {
				return (true);
			}
		}
		return (false);
	}

	public static void writeResult(ArrayList<String> res, String filename) {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filename));
			for (String line : res) {
				bw.write(line + "\n");
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public Content readFile(String path) {
		ArrayList<String> sententces = new ArrayList<String>();
		// ArrayList<String> tags = new ArrayList<String>();
		Scanner scan = null;
		try {
			scan = new Scanner(new File(path));
			String sentence = "";
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (isTagged) {
					// Ignore Empty Lines
					if (!(line.equals("") || line.equals(" ") || line.matches("###MEDLINE:\\d+"))) {
						if (line.equals(".")) {
							sentence = sentence + " " + line;
							sententces.add(sentence);
							System.out.println(sentence);
							sentence = "";
						} else {
							sentence = sentence + " " + line;
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (scan != null) {
				scan.close();
			}
		}

		return (new Content(sententces));

	}

	public void chunkContent(Content content) {
		resultTagger = new ArrayList<String>();
		res = new ArrayList<String>();
		ArrayList<String> sententces = content.getSentences();
		for (String line : sententces) {
			this.chunk(line);
		}
	}

	/*
	 * public void chunkFile(ExactDictionaryChunker chunker, String dir){
	 * resultTagger = new ArrayList<String>(); res= new ArrayList<String>(); try
	 * { Scanner scan = new Scanner(new File(dir)); while (scan.hasNextLine()) {
	 * String line = scan.nextLine(); if (isTagged){ //Ignore Empty Lines if
	 * (!(line.equals("")||line.equals(" "))){ String[] s = line.split("\t");
	 * 
	 * //is Tagged if (s.length == 2){ line = s[0];
	 * 
	 * //Remember Result if (s[1].equals("O")){ res.add(false); }else{
	 * res.add(true); } } this.chunk(chunker, line); } } } } catch
	 * (FileNotFoundException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } }
	 */

	/*
	 * private CharLmHmmChunker trainHMM(TokenizerFactory factory,
	 * HmmCharLmEstimator hmmEstimator, String dir) throws FileNotFoundException
	 * { CharLmHmmChunker chunkerEstimator = new CharLmHmmChunker(factory,
	 * hmmEstimator); Scanner scan = new Scanner(new File(dir));
	 * 
	 * while (scan.hasNextLine()) { String line = scan.nextLine();
	 * chunkerEstimator.trainDictionary(line, "B-protein"); }
	 * 
	 * return chunkerEstimator; }
	 */

	// java TrainGeneTag <trainingInputFile> <modelOutputFile>
	public static void main(String[] args) throws IOException, ClassNotFoundException {

		

		File dataDir = new File("Ressources");
		GeneCorpus corpus = new GeneCorpus(dataDir);
		ObjectHandler<Chunking> printer = new ObjectHandler<Chunking>() {
			public void handle(Chunking chunking) {
				System.out.println(chunking);
			}
		};
		
		String path = "Ressources/training5_annotated.iob";
		boolean isTagged = true;
		
		ArrayList<ArrayList<String>> sententces = new ArrayList<ArrayList<String>>();
		// ArrayList<String> tags = new ArrayList<String>();
		Scanner scan = null;
		try {
			scan = new Scanner(new File(path));
			ArrayList<String> sentence = new ArrayList<>();
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				//System.out.println(line);
				if (isTagged) {
					// Ignore Empty Lines
					if (!(line.equals("") || line.equals(" ") || line.matches("###MEDLINE:\\d+"))) {
						if (line.equals(".\tO")) {
							sentence.add(line);
							sententces.add((ArrayList<String>) sentence.clone());
							sentence = new ArrayList<>();
						} else {
							sentence.add(line);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (scan != null) {
				scan.close();
			}
		}
		
		double fMeasure = 0;
		double recall = 0;
		double precision = 0;
		for (int n = 0; n < 1; n++) {
			int amountTotal = sententces.size();
			int amountSet = amountTotal / 10;
			int random;
			int[] test = new int[amountSet];
			int[] training = new int[amountTotal - amountSet];
			for (int i = 0; i < amountSet; i++) {
				while (true) {
					random = (int) (Math.random() * amountTotal);
					boolean inSet=false;
					for (int j = 0; j < test.length; j++) {
						if (test[j] == random){
							inSet = true;
						}
					}
					if (!inSet){
						test[i] = random;
						break;
					}
				}
			}
			int counter = 0;
			for (int i = 0; i < amountTotal; i++) {
				boolean inTest = false;
				for (int j = 0; j < test.length; j++) {
					if (test[j] == i){
						inTest=true;
					}
				}
				if (!inTest){
					training[counter] = i;
					counter ++;
				}
			}
	
			//corpus.visitTest(printer);
	
			//Create TestFile
			String testDir = "CVTestFile.iob";
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new FileWriter("Ressources/"+testDir));
				for (int i = 0; i < test.length; i++) {
					ArrayList<String> curSentence = sententces.get(test[i]);
					for (String string : curSentence) {
						bw.write(string + "\n");
					}
				}
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			//Create TrainingFile
			String trainDir = "CVTrainingFile.iob";
			try {
				bw = new BufferedWriter(new FileWriter("Ressources/"+trainDir));
				for (int i = 0; i < training.length; i++) {
					ArrayList<String> curSentence = sententces.get(training[i]);
					for (String string : curSentence) {
						bw.write(string + "\n");
					}
				}
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (bw != null) {
					try {
						bw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
			boolean enforceConsistency = true;
			TagChunkCodec tagChunkCodec = new BioTagChunkCodec(tokenizerFactory, enforceConsistency);
	
			int minFeatureCount = 1;
	
			boolean cacheFeatures = true;
	
			boolean addIntercept = true;
	
			double priorVariance = 4.0;
			boolean uninformativeIntercept = true;
			RegressionPrior prior = RegressionPrior.gaussian(priorVariance, uninformativeIntercept);
			int priorBlockSize = 3;
	
			double initialLearningRate = 0.05;
			double learningRateDecay = 0.995;
			AnnealingSchedule annealingSchedule = AnnealingSchedule.exponential(initialLearningRate, learningRateDecay);
	
			double minImprovement = 0.00001;
			int minEpochs = 1;
			int maxEpochs = 1000;
	
			Reporter reporter = Reporters.stdOut().setLevel(LogLevel.DEBUG);
	
			ChainCrfFeatureExtractor<String> featureExtractor = new ChunkerFeatureExtractor();
	
			corpus.setTestFile(testDir);
			corpus.setTrainFile(trainDir);
			ChainCrfChunker crfChunker = ChainCrfChunker.estimate(corpus, tagChunkCodec, tokenizerFactory, featureExtractor,
					addIntercept, minFeatureCount, cacheFeatures, prior, priorBlockSize, annealingSchedule, minImprovement,
					minEpochs, maxEpochs, reporter);
	
			
			ChainCrfChunker compiledCrfChunker = (ChainCrfChunker) AbstractExternalizable.serializeDeserialize(crfChunker);
			
			File modelFile = new File("model");
			AbstractExternalizable.serializeTo(crfChunker, modelFile);
			
			System.out.println("\nEvaluating");
			ChunkerEvaluator evaluator = new ChunkerEvaluator(compiledCrfChunker);
	
			corpus.visitTest(evaluator);
			double curFmeasure = evaluator.evaluation().precisionRecallEvaluation().fMeasure();
			double curRecall = evaluator.evaluation().precisionRecallEvaluation().recall();
			double curPrecision = evaluator.evaluation().precisionRecallEvaluation().precision();
			System.out.println("FMeasure in Fold " + n + ": " + curFmeasure);
			fMeasure = fMeasure + curFmeasure;
			recall = recall + curRecall;
			precision = precision + curPrecision;
		}
		System.out.println("Result CV: " + fMeasure/10);
		System.out.println("Recall: " + recall);
		System.out.println("Precision: " + precision);
		
		//GeneMatcher matcher = new GeneMatcher(crfChunker);
		//matcher.init();
	}
}
