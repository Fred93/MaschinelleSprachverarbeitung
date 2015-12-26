
 
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
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpamClassifier {
	static FastVector records;
	static Instances trainingSet;
	static Attribute emailMessage;
	
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
                 rec.setValue((Attribute) records.elementAt(1), emailSubject + content);
               
                //rec.setValue((Attribute) records.elementAt(2), content);
                 trainingSet.add(rec);
         }
 }
	 
	 private String extractEmailSubject(String file){
		 String subject ="";
		 String regEx = Pattern.quote("Subject:") + "(.*?)(Date:|MiME-Version:|Sender:|MIME-Version:|X-Info:|Message-Id:|Content-Type:)";
		 Pattern pattern = Pattern.compile(regEx, Pattern.DOTALL);
		 Matcher matcher = pattern.matcher(file);
		 if (matcher.find())
		 {
		     System.out.println("Subject : "+matcher.group(1));
		    subject=matcher.group(1);
		 }
		 return subject;
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
			 System.out.println(matcherCT.group(1));
			 if(matcherCT.group(1).contains("text/plain")|| matcherCT.group(1).contains("multipart/mixed")){
				 //content alles nach der ersten leeren Zeile
				 String contentRegEx ="(?m)^\\s*$(.*?)\\z";
				 Pattern patternContent = Pattern.compile(contentRegEx, Pattern.DOTALL);
				 Matcher matcherContent = patternContent.matcher(file);
				 if (matcherContent.find())
				 {
				    //System.out.println(matcherContent.group(1));
					//Lösche alle html tags, linebreaks,leere Zeilen, zahlen
					 content=matcherContent.group(1).replaceAll("(<(.|\n)*?>)|([0-9])|[^\\w\\s]|((?m)^\\s*$)", "");
						
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
				 content=matcherContent.group(1).replaceAll("(<(.|\n)*?>)|([0-9])|[^\\w\\s]|((?m)^\\s*$)", "");
				
				 
				 System.out.println("Ohne Sonderzeichen : "+content);
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
				 content=matcherContent.group(1).replaceAll("(<((.|\n)*?)>)|([0-9])|=|((?m)^\\s*$)|\n", "");
				 System.out.println(content);
			 
		 }}else{
			 //content alles nach der ersten leeren Zeile
			 String contentRegEx ="(?m)^\\s*$(.*?)\\z";
			 Pattern patternContent = Pattern.compile(contentRegEx, Pattern.DOTALL);
			 Matcher matcherContent = patternContent.matcher(file);
			 if (matcherContent.find())
			 {
			    //System.out.println(matcherContent.group(1));
				//Lösche alle html tags, linebreaks,leere Zeilen, zahlen
		      content=matcherContent.group(1).replaceAll("(<((.|\n)*?)>)|([0-9])|=|((?m)^\\s*$)|\n", "");
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

         for (int loop = 0; loop < files.length; loop++) {
                 if (files[loop].isDirectory()) {
                         listFiles(files[loop].getAbsolutePath(), fileNames);
                 } else
                         fileNames.add(files[loop].getAbsolutePath());
         }
         //Collections.shuffle(fileNames);
         return fileNames;
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
     trainingSet.setClass(eClass);
     classifier.readTrainingDataset("mails_test_subset");
     classifier.writeArf(trainingSet, "test.arff");
     
    
	/*
	BufferedReader breader = null;
	breader = new BufferedReader(new FileReader("mails_naive.arff"));
	trainingSet=new Instances (breader);
	*/

	    
	  
      
     
       
       StringToWordVector filter = new StringToWordVector();
       filter.setLowerCaseTokens(true);
       //filter.setTFTransform(true);
       //filter.setIDFTransform(true);
       //filter.setOutputWordCounts(true);
       //trainingSet.setClass(eClass);
       filter.setInputFormat(trainingSet);
       Instances dataFiltered = Filter.useFilter(trainingSet, filter);
       //write to file
       dataFiltered.setClass(eClass);
   	   System.out.println("Class Attribute: " + dataFiltered.classAttribute());  
   	   
   	 //write instance dataFiltered into a arff file  
       classifier.writeArf(dataFiltered, "mails_filtered.arff");
       System.out.println("Attributes "+dataFiltered.numAttributes()); 
   	     
   	      
       //System.out.println(" Filtered data: " + dataFiltered); 
       
      
	    
	   
	  
       /*
         
        // Another Cross validation
      double precision =0;
	  double recall = 0;
	  double fmeasure =0;
	  double error = 0;
	  
	  int size =  dataFiltered.numInstances() /10;
	  System.out.println("Number of Instances " +size);
	  System.out.println("Number of Attributes " +dataFiltered.numAttributes());
	  System.out.println("Class Attribute " +dataFiltered.classAttribute());
	  
	  int beginn =0;
	  int end = size-1;
	  
	  for ( int i=1; i<=10; i++){
		  System.out.println("Iteration "+i);
		  Instances training = new Instances(dataFiltered);
		  Instances testing = new Instances(dataFiltered, beginn, (end-beginn));
		  for (int j=0; j<(end-beginn); j++){
			  //System.out.println("Length training: "+ training.numInstances() +" "+training.instance(beginn));
			  training.delete(beginn);
			 
		  }
		  System.out.println("Length training "+ training.numInstances());
		  System.out.println("Length testing "+ testing.numInstances());
		  
		  NaiveBayes tree = new NaiveBayes();
		  tree.buildClassifier(training);
		  Evaluation eveluation = new Evaluation(training);
		  eveluation.evaluateModel(tree, testing);
		  
		  System.out.println("P: "+eveluation.precision(0));
		  System.out.println("R: "+ eveluation.recall(0));
		  System.out.println("F: " + eveluation.fMeasure(0));
		  System.out.println("E: " + eveluation.errorRate());
		  
		  
	        precision += eveluation.precision(0);
		    recall += eveluation.recall(0);
		    fmeasure += eveluation.recall(0);
		    error += eveluation.errorRate();
		    
		    beginn = end +1;
		    end+=size;
		    if (i==9){
		    	end=dataFiltered.numInstances();
		    }
		  
	  }
	  System.out.println("Precision: "+precision/10.0);
	  System.out.println("Recall: "+ recall/10.0);
	  System.out.println("fMeasure: " + fmeasure/10.0);
	  System.out.println("Error: " + error/10.0);

	  */
	  
	  
	    int seed  = 10;
	    int folds = 10;
	    // randomize data
	    Random rand = new Random(seed);
	    Instances randData = new Instances(dataFiltered);
	    
	    randData.randomize(rand);
	    classifier.writeArf(randData, "mails_random.arff");
	       
	    
	    if (randData.classAttribute().isNominal()){
	      randData.stratify(folds);
	     
	    }
	    // perform cross-validation
	    Evaluation eval = new Evaluation(randData);
	    for (int n = 0; n < folds; n++) {
	      Instances train = randData.trainCV(folds, n);
	      //classifier.writeArf(train, "train"+n);
	      Instances test = randData.testCV(folds, n);
	      // the above code is used by the StratifiedRemoveFolds filter, the
	      // code below by the Explorer/Experimenter:
	      // Instances train = randData.trainCV(folds, n, rand);

	      // build and evaluate classifier
	      Classifier cModel = (Classifier)new NaiveBayes();
	      cModel.buildClassifier(train);
	      eval.evaluateModel(cModel, test);
	      
	      System.out.println("Fold "+n);
	      System.out.println("P:"+eval.precision(0));
	      System.out.println("E:"+eval.errorRate());
	      System.out.println("R:"+eval.recall(0));
	      System.out.println("F:"+eval.fMeasure(0));
	      
	      System.out.println("Train length "+train.numInstances());
	      System.out.println("Test length "+test.numInstances());
	      System.out.println(" ");
	      
	    }

	    // output evaluation
	    System.out.println();
	    System.out.println("=== Setup ===");
	    //System.out.println("Classifier: " + Utils.toCommandLine(cls));
	    System.out.println("Dataset: " + dataFiltered.relationName());
	    System.out.println("Folds: " + folds);
	    System.out.println("Seed: " + seed);
	    System.out.println();
	    System.out.println(eval.toSummaryString("=== " + folds + "-fold Cross-validation ===", false));
  
  }
}