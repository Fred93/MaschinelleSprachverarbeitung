import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
	private HashMap<String, Integer> allTokens = new HashMap<>();
	
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
		try {
			builder = dbFactory.newDocumentBuilder();
			Document doc = builder.parse(f);
			   
		   
			NodeList titles = doc.getElementsByTagName("TITLE");
			NodeList bodies = doc.getElementsByTagName("BODY");
		   
			//Analyze BODY/TITLE
			analyzeTextToken(bodies);
			analyzeTextToken(titles);
		
			NodeList docs = doc.getElementsByTagName("REUTERS");
			amountDocs = docs.getLength();
			
			amountTopics = analyzeEntity(doc, XMLParser.TOPICS);
			amountPlaces = analyzeEntity(doc, XMLParser.PLACES);
			amountPeople = analyzeEntity(doc, XMLParser.PEOPLE);
			
			executor.shutdown();
			while(!executor.isTerminated()){
			}
			manager.addValues(amountDocs, amountTopics, amountPlaces, amountPeople);
			manager.addTokenMap(allTokens);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public void analyzeTextToken(NodeList nodeList){
		//Iterate over nodeList (Title/Body)
		for (int i = 0; i < nodeList.getLength(); i++) {
			
			Node p = nodeList.item(i);
			String TextContent = p.getTextContent().toLowerCase();
			//StringTokenizer stringTokenizer = new StringTokenizer(TextContent, " \t\n\r\f,.\";", false);
			StringTokenizer stringTokenizer = new StringTokenizer(TextContent, " \t\n\r\f", false);
			
			Runnable worker = new TokenAnalyzer(this, stringTokenizer);
			executor.execute(worker);
		}
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
		return count;
	}
	
	public synchronized void addTokenMap(HashMap<String, Integer> tokens) {
		tokens.forEach((k,v)->allTokens.merge(k, v, (v1,v2) -> (v1+v2)));	
	}
	
	public synchronized void addEntityValues(int count, Set<String> distinctSet, String type) {
		if (type == XMLParser.TOPICS){
			amountTopics = count;
		}else if (type == XMLParser.PLACES) {
			amountPlaces = count;
		}else{
			amountPeople = count;
		}
		
	}

}
