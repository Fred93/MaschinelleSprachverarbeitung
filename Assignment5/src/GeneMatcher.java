import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.aliasi.chunk.BioTagChunkCodec;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkerEvaluator;
import com.aliasi.chunk.Chunking;
import com.aliasi.chunk.TagChunkCodec;
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
	private ArrayList<String> resultTagger;
	private ArrayList<String> resultTaggerTag = new ArrayList<String>();
	private ArrayList<String> res = new ArrayList<String>();

	private void init() {
		File corpusFile = new File("Ressources/training_annotated.iob");
		File modelFile = new File("Ressources/model");

		// Read Dictionary
		dictionary = this.readDictionary("Ressources/dyctionary_genenames.txt");
		stopwords = readStopwords("Ressources/stopwords");
		
		dictionary = removeStopwords(dictionary, stopwords);
		// Read Input text

		// Set TokenizerFactory
		myTokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;

		// Init Chunker
		dictionaryChunker = new ExactDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.INSTANCE, true, false); // not
		
		// case
		/*Corpus<ObjectHandler<Chunking>> corpus
        = new GeneCorpus();																											// sensitive
		
		ChainCrfChunker crfChunker = ChainCrfChunker.estimate(corpus,
                                   tagChunkCodec,
                                   tokenizerFactory,
                                   featureExtractor,
                                   addIntercept,
                                   minFeatureCount,
                                   cacheFeatures,
                                   prior,
                                   priorBlockSize,
                                   annealingSchedule,
                                   minImprovement,
                                   minEpochs,
                                   maxEpochs,
                                   reporter);
		*/
		
		Content content = readFile("Ressources/training_annotated.iob");
		
		//content = removeStopwords(content, stopwords);
		//chunkContent(dictionaryChunker, content);
		
		
		
		writeResult(resultTagger, "result.iob");
		
		evaluate(content);
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
	
	public ArrayList<String> readStopwords(String filename){
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
	
	//TODO Handle tags
	private MapDictionary<String> removeStopwords(MapDictionary<String> dictionary, ArrayList<String> stopwords){
		MapDictionary<String> newList = new MapDictionary<String>();
		for(DictionaryEntry<String> line:dictionary){
			if(!containsIgnoreCase(stopwords, line.phrase())){
				newList.addEntry(line);	
			}
		}
		return newList;
	}

	private void chunk(ExactDictionaryChunker chunker, String text) {
		Chunking chunking = chunker.chunk(text);
		CharSequence cs = chunking.charSequence();
		if (chunking.chunkSet().size() == 0){
			resultTagger.add(text + "\tO");
			resultTaggerTag.add("O");
		}
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			CharSequence str = cs.subSequence(start, end);
			double distance = chunk.score();
			String match = chunk.type();
			//System.out.printf("%15s  %15s   %8.1f\n", str, match, distance);
			resultTagger.add(text + "\tB-protein");
			resultTaggerTag.add("B-protein");
		}
	}
	
	public void evaluate(Content content){
		res = content.getTags();
		ArrayList<String> word = content.getWords();
		System.out.println(res.size());
		System.out.println(resultTaggerTag.size());
		int FP = 0;
		int TP = 0;
		int FN = 0;
		int TN = 0;
		if (res.size() == resultTaggerTag.size()){
			for (int i = 0; i < res.size(); i++) {
				
				//System.out.println(resultTaggerTag.get(i));
				if (res.get(i).equals("O") && resultTaggerTag.get(i).equals("O"))TN++;
				if (res.get(i).equals("O") && resultTaggerTag.get(i).equals("B-protein"))FP++;
				if (res.get(i).equals("B-protein") && resultTaggerTag.get(i).equals("O")){
					System.out.println(word.get(i));
					FN++;
				}
				if (res.get(i).equals("B-protein") && resultTaggerTag.get(i).equals("B-protein"))TP++;
			}
			double precision = TP*1.0/(TP+FP);
			double recall = TP*1.0/(TP+FN);
			double f = 2*recall*precision/(recall+precision);
			System.out.println(TN);
			System.out.println(TP);
			System.out.println(FP);
			System.out.println(FN);
			System.out.println("Precision: " + precision);
			System.out.println("Recal: " + recall);
			System.out.println("F-Measure: " + f);
		}else{
			System.out.println("Wrong Size");
		}
	}
	
	public boolean containsIgnoreCase(ArrayList<String> arr, String s){
		for (String el : arr) {
			if (el.equalsIgnoreCase(s)){
				return(true);
			}
		}
		return(false);
	}
	
	public static void writeResult(ArrayList<String> res, String filename){
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filename));
			for (String line:res){
				bw.write(line + "\n");
			}
			bw.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public Content readFile(String path){
		ArrayList<String> words = new ArrayList<String>();
		ArrayList<String> tags = new ArrayList<String>();
		Scanner scan = null;
		try {
			scan = new Scanner(new File(path));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (isTagged){
					//Ignore Empty Lines
					if (!(line.equals("")||line.equals(" "))){
						String[] s = line.split("\t");
						
						//is Tagged
						if (s.length == 2){
							words.add(s[0]);
							tags.add(s[1]);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			if (scan != null){
				scan.close();
			}
		}
		
		return(new Content(words, tags));
	}
	
	public void chunkContent(ChainCrfChunker crfChunker, GeneCorpus corpus){
		resultTagger = new ArrayList<String>();
		res= new ArrayList<String>();
	//	ArrayList<String> words = corpus.;
		//for (String line : words) {
		//	this.chunk(crfChunker, line);
		//}
	}
	
	/*public void chunkFile(ExactDictionaryChunker chunker, String dir){
		resultTagger = new ArrayList<String>();
		res= new ArrayList<String>();
		try {
			Scanner scan = new Scanner(new File(dir));
			while (scan.hasNextLine()) {
				String line = scan.nextLine();
				if (isTagged){
					//Ignore Empty Lines
					if (!(line.equals("")||line.equals(" "))){
						String[] s = line.split("\t");
						
						//is Tagged
						if (s.length == 2){
							line = s[0];
							
							//Remember Result
							if (s[1].equals("O")){
								res.add(false);
							}else{
								res.add(true);
							}
						}
						this.chunk(chunker, line);
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/

	/*
	private CharLmHmmChunker trainHMM(TokenizerFactory factory, HmmCharLmEstimator hmmEstimator, String dir)
			throws FileNotFoundException {
		CharLmHmmChunker chunkerEstimator = new CharLmHmmChunker(factory, hmmEstimator);
		Scanner scan = new Scanner(new File(dir));

		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			chunkerEstimator.trainDictionary(line, "B-protein");
		}

		return chunkerEstimator;
	}
	*/

	// java TrainGeneTag <trainingInputFile> <modelOutputFile>
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		
		File dataDir = new File("Ressources");
        GeneCorpus corpus = new GeneCorpus(dataDir);
        ObjectHandler<Chunking> printer
            = new ObjectHandler<Chunking>() {
                    public void handle(Chunking chunking) {
                        System.out.println(chunking);
                    }
                };
        System.out.println("\nTRAIN");
        corpus.visitTrain(printer);
        
        System.out.println("\nTEST");
        corpus.visitTest(printer);
		
        TokenizerFactory tokenizerFactory
        = IndoEuropeanTokenizerFactory.INSTANCE;
    boolean enforceConsistency = true;
    TagChunkCodec tagChunkCodec
        = new BioTagChunkCodec(tokenizerFactory,
                               enforceConsistency);
        
    
    int minFeatureCount = 1;

    boolean cacheFeatures = true;

    boolean addIntercept = true;

    double priorVariance = 4.0;
    boolean uninformativeIntercept = true;
    RegressionPrior prior
        = RegressionPrior.gaussian(priorVariance,
                                   uninformativeIntercept);
    int priorBlockSize = 3;

    double initialLearningRate = 0.05;
    double learningRateDecay = 0.995;
    AnnealingSchedule annealingSchedule
        = AnnealingSchedule.exponential(initialLearningRate,
                                        learningRateDecay);

    double minImprovement = 0.00001;
    int minEpochs = 10;
    int maxEpochs = 10;

    Reporter reporter
        = Reporters.stdOut().setLevel(LogLevel.DEBUG);
    
    
    ChainCrfFeatureExtractor<String> featureExtractor
    = new SimpleChainCrfFeatureExtractor();

    
    
    ChainCrfChunker crfChunker = ChainCrfChunker.estimate(corpus,
                               tagChunkCodec,
                               tokenizerFactory,
                               featureExtractor,
                               addIntercept,
                               minFeatureCount,
                               cacheFeatures,
                               prior,
                               priorBlockSize,
                               annealingSchedule,
                               minImprovement,
                               minEpochs,
                               maxEpochs,
                               reporter);
    
    
    
    System.out.println("compiling");
    @SuppressWarnings("unchecked") // required for serialized compile
        ChainCrfChunker compiledCrfChunker
        = (ChainCrfChunker)
        AbstractExternalizable.serializeDeserialize(crfChunker);
    System.out.println("compiled");

    System.out.println("\nEvaluating");
    ChunkerEvaluator evaluator
        = new ChunkerEvaluator(compiledCrfChunker);

    corpus.visitTest(evaluator);
    
    System.out.println("\nEvaluation");
    System.out.println(evaluator);
    
    
    System.out.println("Tagging....");
    
    //ArrayList<String> resultTagger = new ArrayList<String>();
    //ArrayList<String> resultTaggerTag = new ArrayList<String>();
    
    
	Chunking chunking=crfChunker.chunk("High-dose growth hormone does not affect proinflammatory cytokine (tumor necrosis factor-alpha, interleukin-6, and interferon-gamma ) release from activated peripheral blood mononuclear cells or after minimal to moderate surgical stress.");
	CharSequence cs = chunking.charSequence();
	System.out.println(cs.toString());
	System.out.println(chunking.chunkSet());
	
	
	
	/*
	if (chunking.chunkSet().size() == 0){
		resultTagger.add("text" + "\tO");
		resultTaggerTag.add("O");
	}
	for (Chunk chunk : chunking.chunkSet()) {
		int start = chunk.start();
		int end = chunk.end();
		CharSequence str = cs.subSequence(start, end);
		double distance = chunk.score();
		String match = chunk.type();
		//System.out.printf("%15s  %15s   %8.1f\n", str, match, distance);
		resultTagger.add("text" + "\tB-protein");
		resultTaggerTag.add("B-protein");
	}
    
	GeneMatcher.writeResult(resultTagger, "result.iob");
	
	*/
	
	
    //corpus.visitTest_untaged(evaluator);
    
		/*
		GeneMatcher matcher = new GeneMatcher();
		matcher.init();
		*/

		// List<String> genes = new ArrayList<String>();

		// TokenizerFactory factory
		// = IndoEuropeanTokenizerFactory.INSTANCE;
		// fCharLmEstimator hmmEstimator
		// = new HmmCharLmEstimator(MAX_N_GRAM,NUM_CHARS,LM_INTERPOLATION);
		// CharLmHmmChunker chunkerEstimator = new
		// CharLmHmmChunker(factory,hmmEstimator);
		// CharLmHmmChunker
		// chunkerEstimator=matcher.trainHMM(factory,hmmEstimator,"Ressources/dyctionary_genenames.txt");
		// chunkerEstimator.handle(arg0);

		//System.out.println("Setting up Data Parser");
		// @SuppressWarnings("deprecation")
		// GeneTagParser parser = new GeneTagParser(); // PLEASE IGNORE
		// DEPRECATION WARNING
		// parser.setHandler(chunkerEstimator);
		// System.out.println("Training with Data from File=" + corpusFile);
		// parser.parse(corpusFile);

		// System.out.println("Compiling and Writing Model to File=" +
		// modelFile);
		// AbstractExternalizable.compileTo(chunkerEstimator,modelFile);

		/*
		 * 
		 * TrieDictionary<String> dictionary=matcher.readDictionary(
		 * "Ressources/dyctionary_genenames.txt");
		 * 
		 * ExactDictionaryChunker dictionaryChunkerTT = new
		 * ExactDictionaryChunker(dictionary,
		 * IndoEuropeanTokenizerFactory.INSTANCE, true,true); String text=
		 * "the the the fsdfs fsi wedw zzzz iiii";
		 * 
		 * double maxDistance = 2.0; WeightedEditDistance editDistance = new
		 * FixedWeightEditDistance(0,-1,-1,-1,Double.NaN);
		 * ApproxDictionaryChunker chunker = new
		 * ApproxDictionaryChunker(dictionary,
		 * IndoEuropeanTokenizerFactory.INSTANCE, editDistance,maxDistance);
		 * matcher.chunk(dictionaryChunkerTT,text);
		 */
	}

}
