import java.util.List;
import java.util.Random;
import java.util.ArrayList;


public class KfoldValidation {
	private String[] strings;
	private Double[] error;

	
	public KfoldValidation(String[] strings){
		this.strings = strings;
	}
	
	//returns best Markov-Model
	public HiddenMarkovModel validate(int k){
		HiddenMarkovModel bestModel =null;
		
		error = new Double[k];
		System.out.println(strings.length);
		 int setSize=strings.length/k;
		System.out.println(setSize);
		
		 Random random = new Random();
		 
		 
		 int[][] testIndicies = new int [k][setSize];
		 
		 List<Integer>IndicieOfStrings=new ArrayList<Integer>();
		 //List of possible Indexes
		 for (int a=0; a<strings.length;a++){
			 IndicieOfStrings.add(a);
		 }

		 
		for (int i =0; i<k; i++ ){
			
			//set testindeizies for current fold
			 for (int bb=0; bb<setSize; bb++){
				 int indexOfIndex = random.nextInt(IndicieOfStrings.size());
				 testIndicies[i][bb] = IndicieOfStrings.get(indexOfIndex);
				 //lösche bentzte Indicies
				 IndicieOfStrings.remove(indexOfIndex);

				 }
			
			
			 String[] forTest=new String[setSize];
			 String[] forLearn=new String[strings.length-setSize];
			 
			 //Liste der benutzten Indizies für Testset
			 List<Integer>listOfTestIndicies=new ArrayList<Integer>();
			 
			 
			 for (int a=0; a<testIndicies[i].length;a++){
				 forTest[a]=strings[testIndicies[i][a]];
				 listOfTestIndicies.add(testIndicies[i][a]);
			 }
			 
			 
			 int a=0;
			  for (int b=0; b<strings.length; b++)
			  {if(!listOfTestIndicies.contains(b)){
				  forLearn[a]=strings[b];
				  a++;
			  }
			  }
		 System.out.println("Zum Lernen Länge" + forLearn.length);
		 //subset

		
		HiddenMarkovModel tagger = new HiddenMarkovModel();
		
		
		System.out.println("Suche tags");
		tagger.findTags(forLearn);
		System.out.println("Lerne");
		tagger.trainModel(forLearn);
		System.out.println("Finished Model training for fold "+(i+1));
		
		//Array of labeled strings
	    String[] result= tagger.viterbi(forTest);

	
	    for (String t :result){
	    	System.out.println(t);
	    }
        error[i]=calculateErrors(result, forTest);
        tagger.kValerrorRate=error[i];
        if (i==0){
			bestModel=tagger;
		}
        
        //find best Model
        if(i>0 && tagger.kValerrorRate<bestModel.kValerrorRate){
        	bestModel=tagger;
        }
        
		System.out.println("Error rate: "+error[i]);
   }

		//calculate mean error
		int n=0;
		double meanError=0;
		while(n<k)
		{
		meanError=meanError+error[n];
		n++;
		}
		meanError = meanError/k;
		//print results
		System.out.println("Feinisched valdation");
		System.out.println("Mean Error over "+k+"-fold cross validation is "+meanError);
		System.out.println("Best model with error "+bestModel.kValerrorRate);
		return bestModel;
		
	}

	public double calculateErrors(String[] predictedData, String[] realData){

		double incorrect=0;
		int total=0;
             for (int i = 0; i<predictedData.length; i++){
            	     String[] pred=  Helper.convertTextToArray(predictedData[i]);
            	     String[] is=  Helper.convertTextToArray(realData[i]);
            	     for (int a = 0; a<pred.length; a++){ 
            	    	if(pred[a].equals(is[a])){
            	    	
            	    	} else {
            	    		incorrect++;
            	    	}
            	    	 total++;
            	     }
             }
		return incorrect/total;
	}

}
