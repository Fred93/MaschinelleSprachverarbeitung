import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import com.aliasi.chunk.CharLmHmmChunker;
import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.ApproxDictionaryChunker;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;
import com.aliasi.hmm.HmmCharLmEstimator;
import com.aliasi.spell.FixedWeightEditDistance;
import com.aliasi.spell.WeightedEditDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;
import com.aliasi.corpus.TagHandler;
import com.aliasi.corpus.parsers.GeneTagParser;

public class geneMatcher {
	static final double CHUNK_SCORE = 1.0;
	static final int MAX_N_GRAM = 8;
	static final int NUM_CHARS = 256;
	static final double LM_INTERPOLATION = MAX_N_GRAM; // default behavior
	
	public TrieDictionary<String> readDictionary(String dic_path) throws FileNotFoundException{
		TrieDictionary<String> dictionary = new TrieDictionary<String>();
	    Scanner scan = new Scanner(new File(dic_path));
	    
	    while(scan.hasNextLine()){
	        String line = scan.nextLine();
	        dictionary.addEntry(new DictionaryEntry<String>(line,"gene"));
	    }
	    
	    return dictionary;
	}
	

	private void chunk(ExactDictionaryChunker chunker, String text) {
		Chunking chunking = chunker.chunk(text);
		CharSequence cs = chunking.charSequence();
	    for (Chunk chunk : chunking.chunkSet()) {
	    	int start = chunk.start();
            int end = chunk.end();
            CharSequence str = cs.subSequence(start,end);
            double distance = chunk.score();
            String match = chunk.type();
            System.out.printf("%15s  %15s   %8.1f\n",
                              str, match, distance);
	    }
		
	}
	// java TrainGeneTag <trainingInputFile> <modelOutputFile>
    @SuppressWarnings("deprecation")
	public static void main(String[] args) throws IOException {
		geneMatcher matcher = new geneMatcher();
	
	/*	
		File corpusFile=new File("Ressources/training_annotated.iob");
		File modelFile = new File("Ressources/model");
		
		 System.out.println("Setting up Chunker Estimator");
		    TokenizerFactory factory
		      = IndoEuropeanTokenizerFactory.INSTANCE;
		    HmmCharLmEstimator hmmEstimator
		      = new HmmCharLmEstimator(MAX_N_GRAM,NUM_CHARS,LM_INTERPOLATION);
		    CharLmHmmChunker chunkerEstimator = new CharLmHmmChunker(factory,hmmEstimator);
		    
		    System.out.println("Setting up Data Parser");
		    @SuppressWarnings("deprecation")
		    GeneTagParser parser 
	            = new GeneTagParser();  // PLEASE IGNORE DEPRECATION WARNING
		    
			parser.setHandler(handler);
		    
		    System.out.println("Training with Data from File=" + corpusFile);
		    parser.parse(corpusFile);
		    
		    System.out.println("Compiling and Writing Model to File=" + modelFile);
	        AbstractExternalizable.compileTo(chunkerEstimator,modelFile);
			*/ 
		    
		    
		   
		 TrieDictionary<String> dictionary=matcher.readDictionary("Ressources/dyctionary_genenames.txt");
		
		 ExactDictionaryChunker dictionaryChunkerTT
	        = new ExactDictionaryChunker(dictionary,
	                                     IndoEuropeanTokenizerFactory.INSTANCE,
	                                     true,true);
		 String text="the the the fsdfs fsi wedw zzzz iiii";
		
		 double maxDistance = 2.0;
		 WeightedEditDistance editDistance
	        = new FixedWeightEditDistance(0,-1,-1,-1,Double.NaN);
		 ApproxDictionaryChunker chunker
	        = new ApproxDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.INSTANCE,
	                                      editDistance,maxDistance);
		matcher.chunk(dictionaryChunkerTT,text);
		
	}
	

}
