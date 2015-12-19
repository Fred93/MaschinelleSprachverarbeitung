
 
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

import java.util.Random;

public class TextDirectoryToArff {
 
 
  public static void main(String[] args) throws Exception {
 
/*
      TextDirectoryToArff tdta = new TextDirectoryToArff();
      try {
    Instances dataset = tdta.createDataset("mails_train");
    System.out.println("Dataset: "+dataset);
      } catch (Exception e) {
    System.err.println(e.getMessage());
    e.printStackTrace();
      }
      */
	  
	  
	// convert the directory into a dataset for weka
	  TextDirectoryLoader loader = new TextDirectoryLoader();
	    loader.setDirectory(new File("mails_train"));
	    Instances dataRaw = loader.getDataSet();
	    //Set class attribute
	    //dataRaw.setClassIndex(dataRaw.numAttributes() - 1);
	    //System.out.println(" Imported data: " + dataRaw);
	    
	  //write instance raw into a arff file  
	    BufferedWriter writer = new BufferedWriter(new FileWriter("mails_raw.arff"));
	    writer.write(dataRaw.toString());
	    writer.flush();
	    writer.close();

	    
	  /*
	  //reading existing the arff file
	  BufferedReader reader = new BufferedReader(
              new FileReader("mails.arff"));
       Instances data = new Instances(reader);
       reader.close();
       // setting class attribute
       data.setClassIndex(data.numAttributes() - 1);
       */
       
       // apply the StringToWordVector
       // (see the source code of setOptions(String[]) method of the filter
       // if you want to know which command-line option corresponds to which
       // bean property)
       StringToWordVector filter = new StringToWordVector();
       filter.setInputFormat(dataRaw);
       Instances dataFiltered = Filter.useFilter(dataRaw, filter);
   	      System.out.println(dataFiltered.classAttribute());  
       //System.out.println(" Filtered data: " + dataFiltered); 
       
       //write instance dataFiltered into a arff file  
	    BufferedWriter writerfiltered = new BufferedWriter(new FileWriter("mails_filtered.arff"));
	    writerfiltered.write(dataFiltered.toString());
	    writerfiltered.flush();
	    writerfiltered.close();
	    
	    // train J48 and output model
	    //J48 classifier = new J48();
	    //classifier.buildClassifier(dataFiltered);
	    //System.out.println(" Classifier model: " + classifier);
	    
	    //Data preprocessing should be apply here, e.g. apply AttributeSelection Filters
	    
	    //cross Validation
	    
	  /*  Random rand = new Random(1); 
	    Instances randData = new Instances(dataFiltered);  
	    randData.randomize(rand); // randomize data with number generator
	    
	    
	    for (int n = 0; n < 5; n++) {
	    	   Instances train = randData.trainCV(5, n);
	    	   Instances test = randData.testCV(5, n);
	    	 
	    	   // further processing, classification, etc.
	    	
	    	 }
	    
	    */
	  
	    
	    
	    int seed  = 10;
	    int folds = 5;

	    // randomize data
	    Random rand = new Random(seed);
	    Instances randData = new Instances(dataFiltered);
	    randData.randomize(rand);
	    if (randData.classAttribute().isNominal())
	      randData.stratify(folds);

	   

	    
	    
	    // perform cross-validation
	    Evaluation eval = new Evaluation(randData);
	    for (int n = 0; n < folds; n++) {
	      Instances train = randData.trainCV(folds, n);
	      Instances test = randData.testCV(folds, n);
	      // the above code is used by the StratifiedRemoveFolds filter, the
	      // code below by the Explorer/Experimenter:
	      // Instances train = randData.trainCV(folds, n, rand);

	      // build and evaluate classifier
	      Classifier cModel = (Classifier)new NaiveBayes();
	      cModel.buildClassifier(train);
	      eval.evaluateModel(cModel, test);
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