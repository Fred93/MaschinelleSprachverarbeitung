package java_DOM_parcer;

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
	private File f;
	private TaskManager manager;
	private int amountDocs;
	private int amountTokenBody = 0;
	private int amountTokenTitle = 0;
	private HashMap<String, Integer> allTokensBody = new HashMap<>();
	
	private int Topic;

	private int Places;

	private int People;
	

	public XMLParser(TaskManager manager, File f) {
		this.f = f;
		this.manager = manager;
	}

	@Override
	public void run() {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			   
		DocumentBuilder builder;
		File xmlFile = convertToXML(f);
		try {
			builder = dbFactory.newDocumentBuilder();
			Document doc = builder.parse(xmlFile);
			   
		   
			NodeList titles = doc.getElementsByTagName("TITLE");
			NodeList bodies = doc.getElementsByTagName("BODY");
		   
			amountTokenBody = analyzeToken(bodies, BODY, "TEXT");
			amountTokenTitle = analyzeToken(titles, TITLE, "TEXT");
			NodeList docs = doc.getElementsByTagName("REUTERS");
			amountDocs = docs.getLength();
			
			
			
			//zählt auch distinct
			Topic = EntitiesCounter(doc, "TOPICS");
			Places = EntitiesCounter(doc, "PLACES");
			People = EntitiesCounter(doc, "PEOPLE");
			
			//Map<String, Integer> sortedMap = HashMapSorter.sortByComparator(allTokensBody);
			//System.out.println(sortedMap);
			//manager.addTokenMap(allTokensBody, BODY);
			
			
			
			manager.addValues(amountDocs, amountTokenBody, amountTokenTitle, Topic, Places, People);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int analyzeToken(NodeList nodeList, int type, String parent){
		int ctr = 0;
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		
		for (int i = 0; i < nodeList.getLength(); i++) {
			
			Node p = nodeList.item(i);
			
			if (p.getParentNode().getNodeName()==parent){
				
				
				
				
				String TextContent = p.getTextContent().toLowerCase();
				//p.setTextContent(TextContent);
				StringTokenizer st = new StringTokenizer(TextContent, " \t\n\r\f,.\";", false);
				ctr=ctr+ st.countTokens();
				
				if (type==1){
				Runnable worker = new TokenAnalyzer(manager, st, type);
				executor.execute(worker);
				}
				
				
		     } 
		}
		executor.shutdown();
		while(!executor.isTerminated()){
		}
		return ctr;
	}
	
	public int EntitiesCounter(Document doc, String type){
		int count=0;
		NodeList list = doc.getElementsByTagName(type);
		Set<String> distinctSet = new HashSet<String>();
		for (int i=0; i<list.getLength(); i++){
			
			if (list.item(i).hasChildNodes()){
				
			  NodeList listChildren= list.item(i).getChildNodes();
			  
			  count +=listChildren.getLength();
			  for (int b=0; b<listChildren.getLength(); b++){
				  
					//System.out.println(listChildren.item(b).getTextContent());
					
					distinctSet.add(listChildren.item(b).getTextContent());
			  }
			  
			}
			
			
		}
		
		manager.mergeSets(distinctSet,type);
		//  System.out.println(distinctSet.size());
		
		return count;
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

	public void passTokens(HashMap<String, Integer> tokens, int type) {
		if (type == BODY){
		
			tokens.forEach((k,v)->allTokensBody.merge(k, v, (v1,v2) -> (v1+v2)));
		}
		
	}
	


}
