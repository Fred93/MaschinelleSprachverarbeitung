import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;
import com.aliasi.chunk.Chunking;
import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

public class GeneMatcher {
	static final double CHUNK_SCORE = 1.0;
	static final int MAX_N_GRAM = 8;
	static final int NUM_CHARS = 256;
	static final double LM_INTERPOLATION = MAX_N_GRAM; // default behavior
	private MapDictionary<String> dictionary;
	private ExactDictionaryChunker edictionaryChunker;
	private boolean isTagged = true;
	private ArrayList<String> stopwords;
	private ArrayList<String> resultTagger;
	private ArrayList<String> resultTaggerTag = new ArrayList<String>();
	private ArrayList<String> res = new ArrayList<String>();
	private String testFileName;
	private String resultFileName;
	private Scanner scan;

	public GeneMatcher(String testFileName, String resultFileName) {
		this.testFileName = testFileName;
		this.resultFileName = resultFileName;
	}

	private void init() {
		//File corpusFile = new File("Ressources/training_annotated.iob");
		//File modelFile = new File("Ressources/model");

		// Read Dictionary
		dictionary = this.readDictionary("dictionary_genenames.txt");
		stopwords = readStopwords("englishStopwords.txt");
		
		dictionary = removeStopwords(dictionary, stopwords);
		// Set TokenizerFactory

		// Init Chunker
		edictionaryChunker = new ExactDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.INSTANCE, true, false); // not
																														// case
		//Content content = readFile("Ressources/training_annotated.iob");
		Content content = readFile(testFileName);
		//content = removeStopwords(content, stopwords);
		chunkContent(edictionaryChunker, content);
		writeResult(resultTagger, resultFileName);
		
		evaluate(content);
	}

	public MapDictionary<String> readDictionary(String dic_path) {
		MapDictionary<String> dictionary = new MapDictionary<String>();
		Scanner scan;
		scan = new Scanner(this.getClass().getClassLoader().getResourceAsStream(dic_path));
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			dictionary.addEntry(new DictionaryEntry<String>(line, "gene", CHUNK_SCORE));
		}
		scan.close();
		return dictionary;
	}
	
	public ArrayList<String> readStopwords(String filename){
		ArrayList<String> words = new ArrayList<String>();
		InputStream input = this.getClass().getClassLoader().getResourceAsStream(filename);
		scan = new Scanner(input);
		while (scan.hasNextLine()) {
			String line = scan.nextLine();
			words.add(line.substring(0, line.length()-1));
		}
		scan.close();
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

	private void chunk(ExactDictionaryChunker edictionaryChunker2, String text) {
		Chunking chunking = edictionaryChunker2.chunk(text);
		if (chunking.chunkSet().size() == 0){
			resultTagger.add(text + "\tO");
			resultTaggerTag.add("O");
		}else{
			resultTagger.add(text + "\tB-protein");
			resultTaggerTag.add("B-protein");
		}
	}
	
	public void evaluate(Content content){
		res = content.getTags();
		int FP = 0;
		int TP = 0;
		int FN = 0;
		if (res.size() == resultTaggerTag.size()){
			for (int i = 0; i < res.size(); i++) {
				
				//System.out.println(resultTaggerTag.get(i));
				if (res.get(i).equals("O") && resultTaggerTag.get(i).equals("B-protein"))FP++;
				if (res.get(i).equals("B-protein") && resultTaggerTag.get(i).equals("O")){
					FN++;
				}
				if (res.get(i).equals("B-protein") && resultTaggerTag.get(i).equals("B-protein"))TP++;
			}
			double precision = TP*1.0/(TP+FP);
			double recall = TP*1.0/(TP+FN);
			double f = 2*recall*precision/(recall+precision);
			System.out.println("Precision: " + precision);
			System.out.println("Recall: " + recall);
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
						words.add(s[0]);
						//is Tagged
						if (s.length == 2){
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
	
	public void chunkContent(ExactDictionaryChunker edictionaryChunker2, Content content){
		resultTagger = new ArrayList<String>();
		res= new ArrayList<String>();
		ArrayList<String> words = content.getWords();
		for (String line : words) {
			this.chunk(edictionaryChunker2, line);
		}
	}

	public static void main(String[] args) throws IOException {
		String testFileName = args[0];
		String resultFileName = args[1];
		GeneMatcher matcher = new GeneMatcher(testFileName, resultFileName);
		matcher.init();
		System.out.println("Finished NER");
	}

}
