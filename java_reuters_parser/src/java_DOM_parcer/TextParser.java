package java_DOM_parcer;


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
import java.util.StringTokenizer;




public class TextParser {
	
	
	private static String directory;
	private static File xmlfile;
	
	
	private static int TotalNumberofFiles;
	private static int TotalNumberofDocs;
	private static int TotalNumberofTokenTextTitle;
	private static int TotalNumberofTokenTextBody;

	
	 public static void main(String[] args) {
		   
			directory = "reuters21578";
			
			File dir = new File(directory);
			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return (name.toLowerCase().endsWith(".sgm") & !name.contains("017"));
				}
			});
			
			TotalNumberofFiles=files.length;
			
			/*for (File file:files){
				System.out.println(file);
			}*/
			
			loadSGMFiles(files);
			
			parseXML(xmlfile);
			
			printProperties();
}
	 private static void printProperties() {
		// TODO Auto-generated method stub
		 System.out.println("Total number of Files : "+TotalNumberofFiles);
		 System.out.println("Total number of Documents: " + TotalNumberofDocs);
		 System.out.println("Total Number of Tokens of TEXT/TITLE: " + TotalNumberofTokenTextTitle);
		 System.out.println("Total Number of Tokens of TEXT/BODY: " + TotalNumberofTokenTextBody);
			
		
	}
	private static void parseXML(File XMLfile) {
	
		 
		 DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		   
		   
		   try{
			   
			   
		   DocumentBuilder builder = dbFactory.newDocumentBuilder();
		   Document doc = builder.parse(XMLfile);
		   
		   NodeList docs = doc.getElementsByTagName("REUTERS");
		   TotalNumberofDocs = docs.getLength();
		   
		   NodeList texts = doc.getElementsByTagName("TITLE");
		   NodeList bodies = doc.getElementsByTagName("BODY");
		   TotalNumberofTokenTextTitle=0;
		   TotalNumberofTokenTextBody=0;
		   
		   for (int i = 0; i < texts.getLength(); i++) {
	       Node p = texts.item(i);
      if (p.getParentNode().getNodeName()=="TEXT"){
    	  
    	  StringTokenizer st = new StringTokenizer(p.getTextContent());
    	  TotalNumberofTokenTextTitle=TotalNumberofTokenTextTitle+ st.countTokens();
    	  
      } 
	       }
		   
		   for (int j = 0; j < bodies.getLength(); j++) {
		       Node p = bodies.item(j);
	      if (p.getParentNode().getNodeName()=="TEXT"){
	    	  
	    	  StringTokenizer st = new StringTokenizer(p.getTextContent());
	    	  TotalNumberofTokenTextBody=TotalNumberofTokenTextBody+ st.countTokens();
	    	  
	      } 
		       }
		   
			   
		   }
		   catch (ParserConfigurationException e)
		   {
			   e.printStackTrace();
		   }
		   catch (SAXException e)
		   {
			   e.printStackTrace();
		   }
		   catch (IOException e)
		   {
			   e.printStackTrace();
		   }
	   }
		
	
	public static void loadSGMFiles(File[] f) {
			
			
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setNamespaceAware(true);
			//SAXDefaultHandler handler = new SAXDefaultHandler(documentCollection);
			
			try {
				
				//1) First add a root node as file isn't well formed.
				String ln;
			   
				
				//a)Temp file
				xmlfile = new File(directory + "/complete.xml");
				xmlfile.createNewFile();
				
				
				//c)Write to file
				FileWriter fw = new FileWriter(xmlfile.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				
				bw.write("<ReutersRoot>" + "\n");
				
				//go over each file reading and writing into the new file 
				for (File sgmFile : f) {
				
					BufferedReader br = new BufferedReader(new FileReader(sgmFile));
					
					while ((ln = br.readLine()) != null) {
					if(!ln.contains("DOCTYPE")) {
							bw.write(ln.replaceAll("&#","")+"\n");
						} 
					}
					
					br.close();
					
				}
				
				bw.write("</ReutersRoot>");
			
				
				bw.flush();
				bw.close();
				
			
			} catch (Exception e) {
				
				System.out.println(e.toString());
			
			}
		}
	 
}
