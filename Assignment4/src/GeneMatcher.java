import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.corpus.Parser;
import com.aliasi.corpus.StringParser;
import com.aliasi.dict.ApproxDictionaryChunker;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.LowerCaseTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
//import com.aliasi.corpus.TagHandler;

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
	private ArrayList<Boolean> resultTaggerB = new ArrayList<Boolean>();
	private ArrayList<Boolean> res = new ArrayList<Boolean>();
	public GeneMatcher() {
		init();
	}

	private void init() {
		File corpusFile = new File("Ressources/training_annotated.iob");
		File modelFile = new File("Ressources/model");

		// Read Dictionary
		dictionary = this.readDictionary("Ressources/dyctionary_genenames.txt");
		stopwords = readStopwords("Ressources/stopwords");

		// Read Input text

		// Set TokenizerFactory
		myTokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;

		// Init Chunker
		dictionaryChunker = new ExactDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.INSTANCE, true, false); // not
																														// case
																														// sensitive
		
		
		chunkFile(dictionaryChunker, "Ressources/training_annotated.iob");
		writeResult(resultTagger, "result.iob");
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
	private ArrayList<String> removeStopwords(ArrayList<String> txt, ArrayList<String> stopwords){
		ArrayList<String> newTxt = new ArrayList<String>();
		for(String line:txt){
			if(!stopwords.contains(line)){
				newTxt.add(line);
			}
		}
		return newTxt;
	}

	private void chunk(ExactDictionaryChunker chunker, String text) {
		Chunking chunking = chunker.chunk(text);
		CharSequence cs = chunking.charSequence();
		if (chunking.chunkSet().size() == 0){
			resultTagger.add(text + "\tO");
			resultTaggerB.add(false);
		}
		for (Chunk chunk : chunking.chunkSet()) {
			int start = chunk.start();
			int end = chunk.end();
			CharSequence str = cs.subSequence(start, end);
			double distance = chunk.score();
			String match = chunk.type();
			//System.out.printf("%15s  %15s   %8.1f\n", str, match, distance);
			resultTagger.add(text + "\tB-Protein");
			resultTaggerB.add(true);
		}

	}
	
	public void evaluate(){
		System.out.println(res.size());
		System.out.println(resultTaggerB.size());
		int correctlyLabled = 0;
		if (res.size() == resultTaggerB.size()){
			for (int i = 0; i < res.size(); i++) {
				if (res.get(i) == resultTaggerB.get(i)){
					correctlyLabled++;
				}
			}
			System.out.println("Accuracy: " + correctlyLabled*1.0/res.size());
		}else{
			System.out.println("Wrong Size");
		}
	}
	
	public void writeResult(ArrayList<String> res, String filename){
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
	
	public void chunkFile(ExactDictionaryChunker chunker, String dir){
		resultTagger = new ArrayList<String>();
		res= new ArrayList<Boolean>();
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
	}

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
		GeneMatcher matcher = new GeneMatcher();

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

		System.out.println("Setting up Data Parser");
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
