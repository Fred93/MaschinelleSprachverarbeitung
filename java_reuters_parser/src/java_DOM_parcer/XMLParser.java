

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.crypto.spec.PSource.PSpecified;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser implements Runnable {

	public final static int BODY = 1;
	public final static int TITLE = 2;
	public static final String TOPICS = "TOPICS";
	public static final String PEOPLE = "PEOPLE";
	public static final String PLACES = "PLACES";
	private File f;
	private TaskManager manager;
	private int amountDocs;
	private int amountTokenBody = 0;
	private int amountTokenTitle = 0;
	private HashMap<String, Integer> allTokens = new HashMap<>();
	
	private HashMap<String, Integer> topics = new HashMap<>();
	private HashMap<String, Integer> people = new HashMap<>();
	private HashMap<String, Integer> places = new HashMap<>();
	
	private int amountTopics;

	private int amountPlaces;

	private int amountPeople;
	
	private ExecutorService executor;
	

	public XMLParser(TaskManager manager, File f) {
		this.f = f;
		this.manager = manager;
	}

	@Override
	public void run() {
		executor = Executors.newFixedThreadPool(4);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			   
		DocumentBuilder builder;
		//File xmlFile = convertToXML(f);
		try {
			builder = dbFactory.newDocumentBuilder();
			Document doc = builder.parse(f);
			   
		   
			NodeList titles = doc.getElementsByTagName("TITLE");
			NodeList bodies = doc.getElementsByTagName("BODY");
		   
			//Analyze BODY/TITLE
			amountTokenBody = analyzeTextToken(bodies);
			amountTokenTitle = analyzeTextToken(titles);
			
			
			NodeList docs = doc.getElementsByTagName("REUTERS");
			amountDocs = docs.getLength();
			
			
			
			//zählt auch distinct
			amountTopics = analyzeEntity(doc, this.TOPICS);
			amountPlaces = analyzeEntity(doc, this.PLACES);
			amountPeople = analyzeEntity(doc, this.PEOPLE);
			
			executor.shutdown();
			while(!executor.isTerminated()){
			}
			
			//Map<String, Integer> sortedMap = HashMapSorter.sortByComparator(allTokensBody);
			//System.out.println(sortedMap);
			//manager.addTokenMap(allTokensBody, BODY);
			
			
			
			manager.addValues(amountDocs, amountTokenBody, amountTokenTitle, amountTopics, amountPlaces, amountPeople);
			manager.addTokenMap(allTokens);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int analyzeTextToken(NodeList nodeList){
		int ctr = 0;
		//Iterate over nodeList (Title/Body)
		for (int i = 0; i < nodeList.getLength(); i++) {
			
			Node p = nodeList.item(i);
			String TextContent = p.getTextContent().toLowerCase();
			//StringTokenizer stringTokenizer = new StringTokenizer(TextContent, " \t\n\r\f,.\";", false);
			StringTokenizer stringTokenizer = new StringTokenizer(TextContent, " \t\n\r\f,.\";", false);
			ctr=ctr+ stringTokenizer.countTokens();
			
			Runnable worker = new TokenAnalyzer(this, stringTokenizer);
			executor.execute(worker);
		} 
		return ctr;
	}
	
	public int analyzeEntity(Document doc, String type){
		int count=0;
		NodeList list = doc.getElementsByTagName(type);
		Set<String> distinctSet = new HashSet<String>();
		
		//Iterate over list of nodes
		for (int i=0; i<list.getLength(); i++){
			
			if (list.item(i).hasChildNodes()){
				NodeList listChildren= list.item(i).getChildNodes();
				count +=listChildren.getLength();
				for (int b=0; b<listChildren.getLength(); b++){
					distinctSet.add(listChildren.item(b).getTextContent());
				} 
			}	
		}
		
		manager.mergeSets(distinctSet,type);
		//  System.out.println(distinctSet.size());
		
		return count;
	}
	
	public synchronized void addTokenMap(HashMap<String, Integer> tokens) {
		tokens.forEach((k,v)->allTokens.merge(k, v, (v1,v2) -> (v1+v2)));	
	}
	
	
	
	public File convertToXML(File f){
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		File xmlFile = null;
		try {
			
			//1) First add a root node as file isn't well formed.
			String ln;
		   
			
			//a)Temp file
			xmlFile = new File(f.getAbsolutePath()+".xml");
			xmlFile.createNewFile();
			
			
			//c)Write to file
			FileWriter fw = new FileWriter(xmlFile.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write("<ReutersRoot>" + "\n");
			
			//go over each file reading and writing into the new file 
			
				BufferedReader br = new BufferedReader(new FileReader(f));
				
				while ((ln = br.readLine()) != null) {
				if(!ln.contains("DOCTYPE")) {
						bw.write(ln.replaceAll("&#","")+"\n");
					} 
				}
				
				br.close();
				
			
			bw.write("</ReutersRoot>");
		
			
			bw.flush();
			bw.close();
			
			
		
		} catch (Exception e) {
			
			System.out.println(e.toString());
		
		}
		return xmlFile;
	}

	public synchronized void addEntityValues(int count, Set<String> distinctSet, String type) {
		if (type == this.TOPICS){
			amountTopics = count;
		}else if (type == this.PLACES) {
			amountPlaces = count;
		}else{
			amountPeople = count;
		}
		
	}

/*	public synchronized void passTokens(HashMap<String, Integer> tokens, int type) {
		if (type == BODY){
		
			tokens.forEach((k,v)->allTokensBody.merge(k, v, (v1,v2) -> (v1+v2)));
		}
		
	}*/
	


}
