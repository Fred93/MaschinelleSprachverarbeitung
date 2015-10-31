package java_DOM_parcer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import java.io.File;

import java.io.FilenameFilter;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Map.Entry;
import java.util.Set;




public class TaskManager {
	
	
	private static String directory;

	
	
	private int totalNumberofFiles;
	private int totalAmountDocs;
	private int totalAmountTokenTitle;
	private int totalAmountTokenBody;
	
	private int Topic;

	private int Places;
	
	private int People;
	
	
	
	private File[] files;
	private HashMap<String, Integer> allTokensBody = new HashMap<>();
	private Set<String> distinctTopics = new HashSet<String>();
	private Set<String> distinctPeople = new HashSet<String>();
	private Set<String> distinctPlaces = new HashSet<String>();

	public TaskManager(String dirrectory){
		dirrectory = directory;
		readFiles();
		totalNumberofFiles=files.length;
	}
	
	private void startAnalyzing() {
		ExecutorService executor = Executors.newFixedThreadPool(21);
		for (int i = 0; i < files.length; i++) {
			Runnable worker = new XMLParser(this, files[i]);
			executor.execute(worker);
		}
		executor.shutdown();
		while(!executor.isTerminated()){
		}
	
		allTokensBody = HashMapSorter.sortByComparator(allTokensBody);
		
		Grafic plot = new Grafic(allTokensBody);
		Thread showPlot = new Thread(plot);
		showPlot.start();
		
		
		printProperties();
		//System.out.println(sortedMap);
	}

	public void readFiles(){
		File dir = new File(directory);
		files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				//
				
				//return (name.toLowerCase().endsWith(".sgm") & !name.contains("017"));
				return (name.toLowerCase().endsWith(".sgm") & name.contains("000"));
			}
		});
	}
	
	 private void printProperties() {
		 System.out.println("Total number of Files : "+totalNumberofFiles);
		 System.out.println("Total number of Documents: " + totalAmountDocs);
		 System.out.println("Total Number of Tokens of TEXT/TITLE: " + totalAmountTokenTitle);
		 System.out.println("Total Number of Tokens of TEXT/BODY: " + totalAmountTokenBody);	
		 System.out.println("Total Number of entities of topics: " + Topic);
		 System.out.println("Total Number of entities of topics distinct: " + distinctTopics.size());
		 System.out.println("Total Number of entities of places: " + Places);
		 System.out.println("Total Number of entities of places distinct: " + distinctPlaces.size());
		 System.out.println("Total Number of entities of people: " + People);
		 System.out.println("Total Number of entities of people distinct: " + distinctPeople.size());
		 
		 
		 System.out.println("Top 100 tokens: ");
		 int tokensprinted =0;
		 for(Entry<String, Integer> entry : allTokensBody.entrySet()) {
			 tokensprinted++;
			    String key = entry.getKey();
			    int value = entry.getValue();
			    System.out.print(key + ": "+ value +  " ");
           if (tokensprinted==100){
          	break;
}
			    
			}
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
