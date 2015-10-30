package java_DOM_parcer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;




public class TaskManager {
	
	
	private static String directory;
	private static File xmlfile;
	
	
	private int totalNumberofFiles;
	private int totalAmountDocs;
	private int totalAmountTokenTitle;
	private int totalAmountTokenBody;
	
	private int Topic;
	private int TopicDistinct;
	private int Places;
	private int PlacesDistinct;
	private int People;
	private int PeopleDistinct;
	
	
	private File[] files;
	private HashMap<String, Integer> allTokensBody = new HashMap<>();
	private Set<String> distinctTopics = new HashSet<String>();
	private Set<String> distinctPeople = new HashSet<String>();
	private Set<String> distinctPlaces = new HashSet<String>();

	public TaskManager(String directory){
		this.directory = directory;
		readFiles();
		totalNumberofFiles=files.length;
	}
	
	private void startAnalyzing() {
		ExecutorService executor = Executors.newFixedThreadPool(4);
		for (int i = 0; i < files.length; i++) {
			Runnable worker = new XMLParser(this, files[i]);
			executor.execute(worker);
		}
		executor.shutdown();
		while(!executor.isTerminated()){
		}
		printProperties();
		Map<String, Integer> sortedMap = HashMapSorter.sortByComparator(allTokensBody);
		//System.out.println(sortedMap);
	}

	public void readFiles(){
		File dir = new File(directory);
		files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				//
				
				return (name.toLowerCase().endsWith(".sgm") & !name.contains("017"));
				//return (name.toLowerCase().endsWith(".sgm") & name.contains("000"));
			}
		});
	}
	
	 private void printProperties() {
		 System.out.println("Total number of Files : "+totalNumberofFiles);
		 System.out.println("Total number of Documents: " + totalAmountDocs);
		 System.out.println("Total Number of Tokens of TEXT/TITLE: " + totalAmountTokenTitle);
		 System.out.println("Total Number of Tokens of TEXT/BODY: " + totalAmountTokenBody);	
		 System.out.println("Total Number of Tokens of topics: " + Topic);
		 System.out.println("Total Number of Tokens of topics distinct: " + distinctTopics.size());
		 System.out.println("Total Number of Tokens of places: " + Places);
		 System.out.println("Total Number of Tokens of places distinct: " + distinctPlaces.size());
		 System.out.println("Total Number of Tokens of people: " + People);
		 System.out.println("Total Number of Tokens of people distinct: " + distinctPeople.size());
	}
	
	public void addValues(int amountDocs, int amountTokenBody, int amountTokenTitle,
	int Topic, int Places,	int People){
		this.totalAmountDocs += amountDocs;
		this.totalAmountTokenBody += amountTokenBody;
		this.totalAmountTokenTitle += amountTokenTitle;
		
		this.Topic +=  Topic;
	
		this.Places += Places;
	
		this.People += People;
	
		
		
	}
	 
	public static void main(String[] args) {
		directory = "reuters21578";	
		TaskManager tp = new TaskManager(directory);
		tp.startAnalyzing();
	}

	public void addTokenMap(HashMap<String, Integer> tokens, int type) {
		if (type == XMLParser.BODY){
			tokens.forEach((k,v)->allTokensBody.merge(k, v, (v1,v2) -> (v1+v2)));
		}
		
	}
	public void mergeSets(Set<String> distinctValues, String type){
		if (type=="TOPICS"){
		//System.out.println(distinctValues.size());
		distinctTopics.addAll(distinctValues);
		//System.out.println(distinctTopics.size());
		}else if (type =="PEOPLE"){
			distinctPeople.addAll(distinctValues);
		}else if(type=="PLACES"){
			distinctPlaces.addAll(distinctValues);
		}
	}
	
	
}
