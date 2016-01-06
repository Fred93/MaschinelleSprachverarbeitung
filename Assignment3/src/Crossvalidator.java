import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.functions.VotedPerceptron;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.RandomTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class Crossvalidator {

	
	
	public static Classifier validate(int folds, Instances dataFiltered) throws Exception{
		StringToWordVector  filter = new StringToWordVector();
		     filter.setLowerCaseTokens(true);
		     filter.setTFTransform(true);
		     filter.setIDFTransform(true);
		     filter.setOutputWordCounts(true);
		     filter.setInputFormat(dataFiltered);
		     
		     FilteredClassifier filtClassifier=new FilteredClassifier();
		     filtClassifier.setFilter(filter);
		     filtClassifier.setClassifier((Classifier) new J48());
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
	    double previousfalsPos=0;
	    
	    // randomize data
	    Random rand = new Random(seed);
	    Instances randData = new Instances(dataFiltered);
	    
	    randData.randomize(rand);     
	    
	    if (randData.classAttribute().isNominal()){
	      randData.stratify(folds);
	     
	    }
	    // perform cross-validation
	    Evaluation eval = new Evaluation(randData);
	    for (int n = 0; n < folds; n++) {
	      Instances train = randData.trainCV(folds, n);
	      //classifier.writeArf(train, "train"+n);
	      Instances test = randData.testCV(folds, n);
	      
	      
	      //check stratification
	      int numspam=0;
	      int numham=0;
	     
	      
	      for (int a=0; a<test.numInstances(); a++){
	    	  
	      //System.out.println(test.instance(a).stringValue(0));
	      if (test.instance(a).stringValue(0)=="spam"){
	    	  numspam++;
	      }else numham++;
	      
	      }
	      double relation= numspam/numham;
	      System.out.println("Spam / Ham = "+ relation);
	      
	      
	      
	      // the above code is used by the StratifiedRemoveFolds filter, the
	      // code below by the Explorer/Experimenter:
	      // Instances train = randData.trainCV(folds, n, rand);

	      // build and evaluate classifier
	      filtClassifier.buildClassifier(train);
	      
	      //cModel = (Classifier) new RandomTree();
	      
	      eval.evaluateModel(filtClassifier, test);
	      
	      
	      System.out.println("Fold Number "+n);
	      System.out.println("P Fold:"+eval.precision(0));
	      System.out.println("E Fod:"+eval.errorRate());
	      System.out.println("R Fod:"+eval.recall(0));
	      System.out.println("fMeasure Fold:"+eval.fMeasure(0));
	      
	      System.out.println("TRUE POSITIVES "+eval.numTruePositives(0));
	      System.out.println("TRUE Negatives "+eval.numTrueNegatives(0));
	      System.out.println("Correct Total "+eval.correct());
	      
	      double falsePos= eval.numFalsePositives(0)-previousfalsPos;
	      previousfalsPos+=falsePos;
	      
	      System.out.println("Num False POSITIVES "+falsePos);
	      //System.out.println("Num False POSITIVES "+eval.numFalsePositives(0));
	      
	      
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

	    return filtClassifier;
	    
}
	
	

	
}
