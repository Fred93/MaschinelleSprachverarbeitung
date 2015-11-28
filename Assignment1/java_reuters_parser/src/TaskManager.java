import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class TaskManager {	
	
	private static String directory;
	static long startTime;
	private int totalAmountDocs;
	private int Topic;
	private int Places;
	private int People;
	private File[] files;
	private HashMap<String, Integer> allTokens = new HashMap<>();
	private Set<String> distinctTopics = new HashSet<String>();
	private Set<String> distinctPeople = new HashSet<String>();
	private Set<String> distinctPlaces = new HashSet<String>();

	public TaskManager(String dirrectory){
		dirrectory = directory;
		readFiles();
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
		allTokens = HashMapSorter.sortByComparator(allTokens);
		printProperties();
	}

	public void readFiles(){
		File dir = new File(directory);
		files = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return (name.toLowerCase().endsWith(".xml") & !name.contains("017"));
			}
		});
	}
	
	 private void printProperties() {
		 System.out.println("Total number of Documents: " + totalAmountDocs);
		 System.out.println("Total Number of Tokens: " + countToken(allTokens));	
		 System.out.println("Total Number of entities of topics: " + Topic);
		 System.out.println("Total Number of entities of topics distinct: " + distinctTopics.size());
		 System.out.println("Total Number of entities of places: " + Places);
		 System.out.println("Total Number of entities of places distinct: " + distinctPlaces.size());
		 System.out.println("Total Number of entities of people: " + People);
		 System.out.println("Total Number of entities of people distinct: " + distinctPeople.size());
		 
		 //writeAsCSV(allTokens);
		 System.out.println("Top 100 tokens: ");
		 int tokensprinted =0;
		 for(Entry<String, Integer> entry : allTokens.entrySet()) {
			 tokensprinted++;
			 String key = entry.getKey();
			 int value = entry.getValue();
			 System.out.println(key + ": \t"+ value +  " ");
			 if (tokensprinted==100){
				 break;
			 }
		 }
		 long endTime = System.nanoTime();
		 System.out.println("Took "+(endTime - startTime)/1000000000.0 + " s"); 
		 
	 }
		
	private int countToken(HashMap<String, Integer> allTokens) {
		int sum = 0;
		for(Entry<String, Integer> entry : allTokens.entrySet()) {
			 sum = sum + entry.getValue();
		 }
		return sum;
	}

	@SuppressWarnings("unused")
	private void writeAsCSV(HashMap<String, Integer> allTokens) {
		try {
			
			System.out.println("Write csv...");
			FileWriter writer = new FileWriter("test1.csv");
			//Write Header
			writer.append("Token");
			writer.append(" ");
			writer.append("Amount");
			writer.append("\n");
			
			
			LinkedList<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(allTokens.entrySet());
			//Write Data
			System.out.println(list.size());
			int ctr = 0;
			for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
				if (ctr%600000 == 0 && ctr != 0){
					System.out.println("wrote " + ctr + " rows");
					writer.flush();
					writer.close();
					writer = new FileWriter("test2.csv");
					writer.append("Token");
					writer.append(" ");
					writer.append("Amount");
					writer.append("\n");
				}
				ctr ++;
				Map.Entry<String, Integer> entry = it.next();
				writer.append("'" + entry.getKey()+"'");
				writer.append(" ");
				writer.append(entry.getValue()+"");
				writer.append("\n");
			}
			System.out.println(ctr);
		    writer.flush();
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	public synchronized void addValues(int amountDocs, int Topic, int Places,	int People){
		this.totalAmountDocs += amountDocs;
		this.Topic +=  Topic;
		this.Places += Places;
		this.People += People;
	}
	 
	public static void main(String[] args) {
		startTime = System.nanoTime();
		if (args.length==0){
			System.out.println("Please spacify directory as input parameter");
		} else{
			directory = args[0];	
			TaskManager tp = new TaskManager(directory);
			tp.startAnalyzing();	
		}
	}

	public synchronized void addTokenMap(HashMap<String, Integer> tokens) {
		tokens.forEach((k,v)->allTokens.merge(k, v, (v1,v2) -> (v1+v2)));	
	}
	
	public synchronized void mergeSets(Set<String> distinctValues, String type){
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
