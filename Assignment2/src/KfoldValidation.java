import java.util.List;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.ArrayList;


public class KfoldValidation {
	private String[] strings;
	private Double[] error;

	
	public KfoldValidation(String[] strings){
		this.strings = strings;
	}
	
	public void validate(int k){
		error = new Double[k];
		for (int i =0; i<k; i++ ){
			 int ab=strings.length/k;
			 String[] forTest=new String[ab];
			 String[] forLearn=new String[k-ab];
			//random indicies
		
		 List<Integer> testIndicies = new ArrayList<Integer>();
		 Random random = new Random();
		 for (int a=0; a<ab;a++){
		 int index = random.nextInt(strings.length);
		 //System.out.println("Index test "+index);
		 testIndicies.add(index);
		 forTest[a]=strings[index];
		 }
		 //System.out.println("Indexes "+testIndicies);
		 int a=0;
		  for (int b=0; b<strings.length; b++)
		  {if(!testIndicies.contains(b)){
			  forLearn[a]=strings[b];
			  a++;
			 // System.out.println(b);
		  }
		  }
		// System.out.println(forTest[0]);
		// System.out.println(forTest.length);
		// System.out.println(forLearn.length);
		 //subset

		
		HiddenMarkovModel tagger = new HiddenMarkovModel();
		
		tagger.findTags(forLearn);
		tagger.trainModel(forLearn);
		//System.out.println(forLearn.length);
		//System.out.println(forTest.length);
		
		System.out.println("Finished Model training for fold "+(i+1));
		
		//Array of labeled strings
	    String[] result= tagger.viterbi(forTest);

	
	    for (String t :result){
	    	System.out.println(t);
	    }
	    //System.out.println(result.length);
	   
        error[i]=calculateErrors(result, forTest);
		System.out.println("Error rate: "+error[i]);
		 }
		int n=0;
		double meanError=0;
		while(n<k)
		{
		meanError=meanError+error[n];
		n++;
		}
		meanError = meanError/k;
		System.out.println("Mean Error over "+k+"-fold cross validation is "+meanError);
		
		
	}

	public double calculateErrors(String[] predictedData, String[] realData){
		
	    
		double incorrect=0;
		int total=0;
             for (int i = 0; i<predictedData.length; i++){
            	     String[] pred=  convertTextToArray(predictedData[i]);
            	     String[] is=  convertTextToArray(realData[i]);
            	     
            	    System.out.println(pred.length);
            	    System.out.println(is.length);
            	     
            	     for (int a = 0; a<pred.length; a++){ 
            	    	if(pred[a].equals(is[a])){
            	    	
            	    	} else {
            	    		incorrect++;
            	    	}
            	    	 total++;
            	    	 //System.out.println(a); 
            	     }

             }
            
		
		
		return incorrect/total;
		
	}
	public String[] convertTextToArray(String s){
		ArrayList<String> strings = new ArrayList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(s, " \t\n\r\f", false);
		while (stringTokenizer.hasMoreElements()) {
			strings.add((String) stringTokenizer.nextElement());
		}
		return strings.toArray(new String[strings.size()]);
	}
	
	
	

}
