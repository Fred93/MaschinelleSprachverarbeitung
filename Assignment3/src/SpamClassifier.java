
 
import java.io.*;
import weka.core.*;
import weka.core.converters.TextDirectoryLoader;
import weka.filters.unsupervised.attribute.StringToWordVector;
import weka.classifiers.Classifier;
import weka.filters.*;
import weka.core.*;
import weka.core.converters.*;
import weka.classifiers.trees.*;
import weka.filters.Filter;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl;

public class SpamClassifier {
	static FastVector records;
	static FastVector unlabeledRecords;
	static Instances trainingSet;
	//static Instances ESet;
	static ArrayList<String> fileNamesEmailSet;
	static StringToWordVector filter;
	static Attribute emailMessage;
	public static final int LEARNING = 1;
	public static final int CLASSIFICATION = 2;
	public int mode = SpamClassifier.LEARNING;
	
	//create Arff file from path
	public Instances loadTextstoArff(String path) throws IOException{
	TextDirectoryLoader loader = new TextDirectoryLoader();
    loader.setDirectory(new File(path));
    Instances dataRaw = loader.getDataSet();
	return dataRaw;
	
	}
	public void writeArf(Instances arff, String directory) throws IOException{
	  BufferedWriter writer = new BufferedWriter(new FileWriter(directory));
	    writer.write(arff.toString());
	    writer.flush();
	    writer.close();
	
	
	}
	
	 private void readTrainingDataset(String dataset) throws IOException {

         ArrayList<String> fileNames = new ArrayList<String>();
         ArrayList<String> dataFiles = this.listFiles(dataset, fileNames);
         String data = "";
         String emailSubject="";
         String content ="";
         for (int looper = 0; looper < dataFiles.size(); looper++) {
                 data = this.readFileAsString(dataFiles.get(looper));
        	     //System.out.println(data);
                 emailSubject=extractEmailSubject(data);
                 content=extractEmailContent(data);
                 
                 Instance rec = new Instance(2);
            
                 if (dataFiles.get(looper).contains("ham")) {
                         rec.setValue((Attribute) records.elementAt(0), "ham");
                 } else  if (dataFiles.get(looper).contains("spam")){
                         rec.setValue((Attribute) records.elementAt(0), "spam");
                 }
                 rec.setValue((Attribute) records.elementAt(1), emailSubject+content);
               
                //rec.setValue((Attribute) records.elementAt(2), content);
                 trainingSet.add(rec);
         }
 }
	 private void readUnlabeledTrainingDataset(String dataset) throws IOException {

         ArrayList<String> fileNames = new ArrayList<String>();
         ArrayList<String> dataFiles = this.listFiles(dataset, fileNames);
         String data = "";
         String emailSubject="";
         String content ="";
         for (int looper = 0; looper < dataFiles.size(); looper++) {
                 data = this.readFileAsString(dataFiles.get(looper));
        	     //System.out.println(data);
                 emailSubject=extractEmailSubject(data);
                 content=extractEmailContent(data);
                 
                 Instance rec = new Instance(2);
            
                 
                 rec.setValue((Attribute) records.elementAt(1), emailSubject+content);
               
                //rec.setValue((Attribute) records.elementAt(2), content);
                 trainingSet.add(rec);
         }
 }
	 
	 private String extractEmailSubject(String file){
		 String subject ="";
		 //ungeschickt
		 String regEx = Pattern.quote("Subject:") + "(.*?)(Date:|MiME-Version:|X-Mailer:|X-Priority:|X-List-Unsubscribe:|\\z|Sender:|Sender:|MIME-Version:|To:|\n\n(\n)*|X-Info:|Message-Id:|Content-Type:)";
		 Pattern pattern = Pattern.compile(regEx, Pattern.DOTALL);
		 Matcher matcher = pattern.matcher(file);
		 if (matcher.find())
		 {
		    //System.out.println("Subject : "+matcher.group(1));
		    subject=matcher.group(1);
		 }
		 return subject;
	 }
	 
	 private String extractEmailFature(String filee){
		 String content ="";
		 String file = filee.toLowerCase();
		 
		 
		 return content;
	 }
	 
	 
	 private String extractEmailContent(String filee){
		 String content ="";
		 String file = filee.toLowerCase();
		 //nicht alle haben content-type
		 String contentTyperegEs = Pattern.quote("content-type:") + "(.*?)\n";
		 Pattern pattern = Pattern.compile(contentTyperegEs);
		 Matcher matcherCT = pattern.matcher(file);
		 if (matcherCT.find())
		 {
			 //System.out.println(matcherCT.group(1));
			 if(matcherCT.group(1).contains("text/plain")|| matcherCT.group(1).contains("multipart/mixed")){
				 //content alles nach der ersten leeren Zeile
				 String contentRegEx ="(?m)^\\s*$(.*?)\\z";
				 Pattern patternContent = Pattern.compile(contentRegEx, Pattern.DOTALL);
				 Matcher matcherContent = patternContent.matcher(file);
				 if (matcherContent.find())
				 {
				    //System.out.println(matcherContent.group(1));
					//Lösche alle html tags, linebreaks,leere Zeilen, zahlen
					 content=matcherContent.group(1).replaceAll("(<((.|\n)*?)>)|([0-9])|=|((?m)^\\s*$)|\n\n(\n*)", "");
						
				 }
				 
			 } else if(matcherCT.group(1).contains("text/html"))
			 {
				
				 //suche html-tag teil, isz nicht immer <html>
				 String contentRegEx ="(?iu)<[a-z]*>(.*?)\\z";
				 Pattern patternContent = Pattern.compile(contentRegEx, Pattern.DOTALL);
				 Matcher matcherContent = patternContent.matcher(file);
				 if (matcherContent.find())
				 {
				//Lösche alle html tags, linebreaks,leere Zeilen, zahlen
					 content=matcherContent.group(1).replaceAll("(<((.|\n)*?)>)|([0-9])|=|((?m)^\\s*$)|\n\n(\n*)", "");
				
				 
				 //System.out.println("Ohne Sonderzeichen : "+content);
				 }
				
				 
			 }
		   
		 } else
			 //wenn kein content-type
			 if (file.contains("<html>"))
		 {
				 String contentRegEx ="(?iu)<[a-z]*>(.*?)\\z";
				 Pattern patternContent = Pattern.compile(contentRegEx, Pattern.DOTALL);
				 Matcher matcherContent = patternContent.matcher(file);
				 if (matcherContent.find())
				 {
				//Lösche alle html tags, linebreaks,leere Zeilen, zahlen
				 content=matcherContent.group(1).replaceAll("(<((.|\n)*?)>)|([0-9])|=|((?m)^\\s*$)|\n\n(\n*)", "");
				// System.out.println(content);
			 
		 }}else{
			 //content alles nach der ersten leeren Zeile
			 String contentRegEx ="(?m)^\\s*$(.*?)\\z";
			 Pattern patternContent = Pattern.compile(contentRegEx, Pattern.DOTALL);
			 Matcher matcherContent = patternContent.matcher(file);
			 if (matcherContent.find())
			 {
			    //System.out.println(matcherContent.group(1));
				//Lösche alle html tags, linebreaks,leere Zeilen, zahlen
				 content=matcherContent.group(1).replaceAll("(<((.|\n)*?)>)|([0-9])|=|((?m)^\\s*$)|\n\n(\n*)", "");
			 }
		 }
		 return content;
	 }
	 
	 
	 private String readFileAsString(String filePath) throws IOException {
		 File file = new File(filePath);
         byte[] buffer = new byte[(int) file.length()];
         BufferedInputStream f = null;
         try {
                 f = new BufferedInputStream(new FileInputStream(filePath));
                 f.read(buffer);
         } finally {
                 if (f != null)
                         try {
                                 f.close();
                         } catch (IOException ignored) {
                         }
         }
         return new String(buffer);
 }

	 
	 
	 private ArrayList<String> listFiles(String path, ArrayList<String> fileNames) {
         File dir = new File(path);
         File[] files = dir.listFiles();
         if(files.length>0) {
        	 System.out.println("yes");
         }
         
         
         for (int loop = 0; loop < files.length; loop++) {
                 if (files[loop].isDirectory()) {
                         listFiles(files[loop].getAbsolutePath(), fileNames);
                 } else
                         fileNames.add(files[loop].getAbsolutePath());
         }
         //Collections.shuffle(fileNames);
         return fileNames;
 }
 
	 
	 private void clasify(Instances emails, Classifier model) throws Exception{
		
		 }
	 
	 public Classifier loadClassifier(String rPath){
		Classifier cl = null;
		try {
			String dir = System.getProperty("user.dir");
			cl = (Classifier) weka.core.SerializationHelper.read(dir+rPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cl;
	 }
	 
	 public void saveClassifier(Classifier model, String rPath){
		 String path = System.getProperty("user.dir");
		try {
			ObjectOutputStream oos = new ObjectOutputStream(
					  new FileOutputStream(path+rPath));

		      oos.writeObject(model);
		      oos.flush();
		      oos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	 }
		 public void filterEmails(Classifier model, String pfadzuEmails) throws Exception{
			 
			 
			 Instances unlabaled_unfiltered = readUnlabeled(pfadzuEmails);
			// set class attribute
			 
			 //System.out.println("Class "+unlabaled_unfiltered.getClass().getName());
			 
			 Instances unlabeled = Filter.useFilter(unlabaled_unfiltered, filter);
			 // create copy
			 unlabeled.setClassIndex(0);
			 
			 Instances labeled = new Instances(unlabeled); 
			 writeArf(unlabeled, "unlabaled_mails_filtered.arff");
			 File file = new File("result");
			 FileWriter f = new FileWriter(file);
			 BufferedWriter s = new BufferedWriter(f);
			 
			 for (int i = 0; i < unlabeled.numInstances(); i++) {
	    		 System.out.println(unlabeled.instance(i));
	    		 String fileName= fileNamesEmailSet.get(i);
	    		 //System.out.println(fileName);
	    	     double clsLabel = model.classifyInstance(unlabeled.instance(i));
	    	     System.out.println(clsLabel);
	    	     labeled.instance(i).setClassValue(clsLabel);
	    	     s.write(fileName+ ": "+ clsLabel + "\n");
	    	     
	    	     //labeled.instance(i).setClassValue(clsLabel);
	    	     //System.out.println("Labeled  "+labeled.instance(i));
	    	   }
			 writeArf(unlabeled, "classified_mails.arff");
			 s.close();
		 }
		 public Instances readUnlabeled(String pfad) throws Exception{
			 
			 Attribute emailContent = new Attribute("emailContent", (FastVector) null);
			 //FastVector emailClass = new FastVector(2);
			 //emailClass.addElement("spam");
		     //emailClass.addElement("ham");
		     Attribute eClass= new Attribute("emailClass", (FastVector) null);
			 unlabeledRecords = new FastVector(2);
			 unlabeledRecords.addElement(eClass);
			 unlabeledRecords.addElement(emailContent);

		     //records.addElement(emailMessage);
			Instances ESet = new Instances("SpamClsfy", unlabeledRecords, 80);
			ESet.setClass(eClass);
			 fileNamesEmailSet = new ArrayList<String>();
	         ArrayList<String> dataFiles = this.listFiles(pfad, fileNamesEmailSet);
	         String data = "";
	         String emailSubject="";
	         String content ="";
	         for (int looper = 0; looper < dataFiles.size(); looper++) {
	                 data = this.readFileAsString(dataFiles.get(looper));
	        	     //System.out.println(data);
	                 emailSubject=extractEmailSubject(data);
	                 content=extractEmailContent(data);
	                 Instance rec = new Instance(2);
	                 rec.setValue(emailContent, emailSubject+content);
	                 //rec.setValue(eClass, "ham");
	                 
	                 
	                //rec.setValue((Attribute) records.elementAt(2), content);
	                 ESet.add(rec);
	         }
	         writeArf(ESet, "unlabaled_mails.arff");
			//Instances filteredStringtoWord = filterStringToWordVector(ESet);
	         
	         
	         
	         return ESet;
			 
		 }
	 
		 public Instances filterStringToWordVector(Instances arff) throws Exception{
			 
		       Instances dataFiltered = Filter.useFilter(arff, filter);
		     return dataFiltered;
		 }
		 
 
  public static void main(String[] args) throws Exception {
 
	SpamClassifier classifier = new SpamClassifier();
	//Instances arff = classifier.loadTextstoArff("mails_train");
	//classifier.writeArf(arff, "mails_raw.arff");
	
 
	 //emailMessage = new Attribute("emailMessage", (FastVector) null);
	 Attribute emailSubject = new Attribute("emailSubject", (FastVector) null);
     FastVector emailClass = new FastVector(2);
     emailClass.addElement("spam");
     emailClass.addElement("ham");
     Attribute eClass= new Attribute("emailClass", emailClass);
     records = new FastVector(2);
     records.addElement(eClass);
     records.addElement(emailSubject);

     //records.addElement(emailMessage);
     trainingSet = new Instances("SpamClsfyTraining", records, 80);
     System.out.println("TrainingSet length "+trainingSet.numInstances());
     trainingSet.setClass(eClass);
     classifier.readTrainingDataset("mails_test_subset");
     classifier.writeArf(trainingSet, "test.arff");
     
     //trainingSet = new Instances("SpamClsfyTraining", records, 80);
     //System.out.println("TrainingSet length "+trainingSet.numInstances());
     //trainingSet.setClass(eClass);
     //classifier.readUnlabeledTrainingDataset("mails_test_subset");
     //classifier.writeArf(trainingSet, "testUnlabeled.arff");
	/*
	BufferedReader breader = null;
	breader = new BufferedReader(new FileReader("mails_naive.arff"));
	trainingSet=new Instances (breader);
	*/
     
     //filter must be global
       filter = new StringToWordVector();
       filter.setLowerCaseTokens(true);
       filter.setTFTransform(true);
       filter.setIDFTransform(true);
       filter.setOutputWordCounts(true);
       trainingSet.setClass(eClass);
       filter.setInputFormat(trainingSet);
       Instances dataFiltered = Filter.useFilter(trainingSet, filter);
       //write to file
       dataFiltered.setClass(eClass);
   	   System.out.println("Class Attribute: " + dataFiltered.classAttribute());  
   	   
   	 //write instance dataFiltered into a arff file  
       classifier.writeArf(dataFiltered, "mails_filtered.arff");
       System.out.println("Attributes "+dataFiltered.numAttributes()); 
   	     
   	    
       //System.out.println(" Filtered data: " + dataFiltered);
       //currenly not the best
       classifier.mode = SpamClassifier.CLASSIFICATION;
       if (classifier.mode == SpamClassifier.LEARNING){
    	   Classifier bestModel = Crossvalidator.validate(10, dataFiltered);
           //write model to file
          classifier.saveClassifier(bestModel, "/models/J48.model");
          System.out.println("MODEL SAVED");
       }else{
    	   Classifier model = classifier.loadClassifier("/models/J48.model");
    	   classifier.filterEmails(model, "mails_unlabaled_test");

       }

  }
}