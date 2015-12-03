import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class KfoldValidation {
	private String[] strings;
	private int[][] subsetIndices;
	private String dataset;

	
	
	public KfoldValidation(String[] strings){
		this.strings = strings;
	
		
		
	}
	
	public void validate(int k){
		
		
		 //subsetIndices = new int[k][];
		 for (int i =0; i<1; i++ ){
			 String[] forTest=new String[i+1];
			 String[] forLearn=new String[k-i+1];
			int indexLearn=(i+1)*(strings.length/k);
			
		forTest=Arrays.copyOfRange(strings, 0, indexLearn);
		forLearn=Arrays.copyOfRange(strings, indexLearn, strings.length);
		HiddenMarkovModel tagger = new HiddenMarkovModel();
		
		tagger.findTags(forLearn);
		tagger.trainModel(forLearn);
		System.out.println(forLearn.length);
		System.out.println(forTest.length);
		System.out.println("Finished Model training for fold "+indexLearn);
		
		//Array of labeled strings
	    String[] result= tagger.viterbi(Arrays.copyOfRange(forLearn, 0, 1));
	    for (String t :result){
	    	System.out.println(t);
	    }
		// double error= calculateErrors(result, forTest);
		//System.out.println("Error: "+error);

		 }
		
	}
	public double calculateErrors(String[] predictedData, String[] realData){
		int correct=0;
		int incorrect=0;
		int total=0;
             for (int i = 0; i<predictedData.length; i++){
            	     String[] pred=  convertTextToArray(predictedData[i]);
            	     String[] is=  convertTextToArray(realData[i]);
            	     for (int a = 0; a<pred.length; i++){ 
            	    	if(pred[a]==is[a]){
            	    		correct++;
            	    	} else {
            	    		incorrect++;
            	    	}
            	    	 total++;
            	    	 
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
