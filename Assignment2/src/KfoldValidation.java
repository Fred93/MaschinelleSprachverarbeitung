import java.util.List;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class KfoldValidation {
	private String[] strings;
	private int[][] subsetIndices;
	private String dataset;

	
	
	public KfoldValidation(String[] dataset, int k){
		dataset = strings;
		validate(k);
		
		
	}
	
	public void validate(int k){
		
		
		 subsetIndices = new int[k][];
		 for (int i =0; i<k; i++ ){
			 String[] forLearn=new String[i+1];
			 String[] forTest=new String[k-i+1];
			int indexLearn=(i+1)*(strings.length/k);
			
		forLearn=Arrays.copyOfRange(strings, 0, indexLearn);
		forTest=Arrays.copyOfRange(strings, i, strings.length);
		HiddenMarkovModel tagger = new HiddenMarkovModel();
		
		tagger.findTags(forLearn);
		tagger.trainModel(forLearn);
		System.out.println("Finished Model training");
		
	    tagger.viterbi(forTest);
		

		 }
		
	}
	
	
	
	
	

}
