package java_DOM_parcer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLParser implements Runnable {
	private File f;
	private TaskManager manager;
	private int amountDocs;
	private int amountTokenBody = 0;
	private int amountTokenTitle = 0;
	
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
			   
			NodeList docs = doc.getElementsByTagName("REUTERS");
			amountDocs = docs.getLength();
		   
			NodeList titles = doc.getElementsByTagName("TITLE");
			NodeList bodies = doc.getElementsByTagName("BODY");
		   
			amountTokenBody = getAmountToken(bodies);
			amountTokenTitle = getAmountToken(titles);
			
			manager.addValues(amountDocs, amountTokenBody, amountTokenTitle);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public int getAmountToken(NodeList nodeList){
		int ctr=0;
		//for (int i = 0; i < nodeList.getLength(); i++) {
		for (int i = 0; i < 1; i++) {
			Node p = nodeList.item(i);
			if (p.getParentNode().getNodeName()=="TEXT"){
				StringTokenizer st = new StringTokenizer(p.getTextContent());
				ctr=ctr+ st.countTokens();
				TokenAnalyzer worker = new TokenAnalyzer(st);
				(new Thread(worker)).start();
		     } 
		}
		return ctr;
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

}
