import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.StringTokenizer;
import utils.CSVWriter;

public class HiddenMarkovModel {
	private TransitionManager transitionManager;
	private EmissionManager emissionManager;
	private String[] assignedTags;
	private double[][] probabilityTabular;
	private double normalizationParameter;
	
	public HiddenMarkovModel(){
		transitionManager = new TransitionManager(0.1);
		emissionManager = new EmissionManager(0.1);
	}
	
	public File[] readFiles(String directory){
		File dir = new File(directory);
		File[] files = dir.listFiles();
		return files;
	}
	
	public void findTags(String[] strings){
		
		for (int i = 0; i < strings.length; i++) {
			System.out.println("Learning for "+i);
			String string = strings[i];
			StringTokenizer stringTokenizer = new StringTokenizer(string, " \t\n\r\f", false);
		    while (stringTokenizer.hasMoreElements()) {
				String token = (String) stringTokenizer.nextElement();
				String[] res = token.split("/");
				
				
				transitionManager.addTag(res[1]);
				//System.out.println("Sent to transition manager, tag of " +res[0]+ " is " + res[1]);
				
				emissionManager.addTerm(res[0]);
				//System.out.println("Sent to emission manager " + res[0]);
			}
		}
	    /*for (String tag: transitionManager.getTagSet()) {
			System.out.println(tag);
		}*/
	}
	
	public void trainModel(String[] strings){
		transitionManager.calculateTransitionProbalilities(strings);
		emissionManager.setTagSet(transitionManager.getTagSet());
		emissionManager.calculateEmissionProbalilities(strings);;
	}
	
	
	
	public String[] convertFilesToStrings(File[] files){
		String[] strings = new String[files.length];
	    FileInputStream fin;
		try {
			for (int i = 0; i < strings.length; i++) {
				File f = files[i];
				fin = new FileInputStream(f);
				byte[] buffer = new byte[(int) f.length()];
			    new DataInputStream(fin).readFully(buffer);
			    fin.close();
			    String s = new String(buffer, "UTF-8");
			    strings[i] = s;
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return strings;
	}
	
	public String[] convertTextToArray(String s){
		ArrayList<String> strings = new ArrayList<String>();
		StringTokenizer stringTokenizer = new StringTokenizer(s, " \t\n\r\f", false);
		while (stringTokenizer.hasMoreElements()) {
			strings.add((String) stringTokenizer.nextElement());
		}
		return strings.toArray(new String[strings.size()]);
	}
	
	public String prepareInput(String input) {
		String result="";
			StringTokenizer stringTokenizer = new StringTokenizer(input, " \t\n\r\f", false);
		    while (stringTokenizer.hasMoreElements()) {
				String token = (String) stringTokenizer.nextElement();
				String[] res = token.split("/");
				result = result+" "+res[0];
		    }
		return result;
		
	}
	
	
	public String[] viterbi(String[] data){
		String[] answer = new String [data.length];
		
		
		for (int a=0; a<data.length;a++){	
		String s= prepareInput(data[a]);
		String[] tokens = convertTextToArray(s);
		double length=tokens.length;
		
		normalizationParameter = (1/(length/2));
		
		System.out.println("Norm.Parameter " + normalizationParameter);
		
		String[] tags = new String[tokens.length];
		assignedTags = tags;
		probabilityTabular = new double[transitionManager.getTagSet().size()][tokens.length];
		viterbiStep(tokens, tokens.length-1);
		
		String rep="";
		for (int i=0; i<tokens.length; i++) {
			rep= rep+" "+tokens[i] +"/"+assignedTags[i];
	    }
		answer[a]=rep;
		
		
		CSVWriter.writeArrayAsCsv(probabilityTabular, "probabilityTabular.csv");
		for (String string : assignedTags) {
		//	System.out.println(string);
		}
		
		}
		return answer;
		
	}
	
	public void viterbiStep(String[] tokens, int i){
		
		
		String bestHit = "";
		if (i == -1){
			//return 1;
		}else{
			
		
			String token = tokens[i];
			viterbiStep(tokens, i-1);
			//System.out.println("Vitebi step for: " + tokens[i]);
			/*String previousTag;
			if (i == 0){
				previousTag = TransitionManager.START;
			}else{
				previousTag = assignedTags[i-1];
			}*/
			double maxPathProbability =1;	
			double maxProb = 1;
			for (String tag : transitionManager.getTagSet()) {
				//System.out.println("Tag" +tag);
				maxPathProbability = 1;
				bestHit = "NA";
				//find max transition probability to tag from previous path/tag
				for (String previousTag : transitionManager.getTagSet()) {
					double maxPrevious = 1;
					if (i > 0){
						maxPrevious = probabilityTabular[transitionManager.getTagIndex(previousTag)][i-1];
					}
					
					double transition = transitionManager.getTransitionProbability(previousTag, tag);
					double totalPathProbability = maxPrevious + Math.log(transition);
					
					
					//Look for maximal Path Prob.
					if (maxPathProbability==1)
					{
						maxPathProbability = totalPathProbability;
					}
					if (totalPathProbability > maxPathProbability){
						maxPathProbability = totalPathProbability;
						bestHit = tag;
					}
				}
				//emission - p das gegebenes Wort ist getagt mit jedem Tag
				
				double a = Math.log(emissionManager.getEmissionProbability(tag, token));
				double totalProbability = normalizationParameter*(maxPathProbability+ a);
			
				
				
				//System.out.println(totalProbability);
				//nehmen die maximale p des phades bis current tag
          if(maxProb==1){
        	  maxProb = totalProbability;
          }
				if (totalProbability > maxProb){
					maxProb = totalProbability;
					assignedTags[i] = bestHit;
					//System.out.println(bestHit);
				}
				probabilityTabular[transitionManager.getTagIndex(tag)][i] = totalProbability;	
			}
		}
	}

	public static void main(String[] args) {
		String directory = "brown_learn";
		HiddenMarkovModel tagger = new HiddenMarkovModel();
		File[] files = tagger.readFiles(directory);
		String[] strings = tagger.convertFilesToStrings(files);
		
		KfoldValidation kfoldVal = new KfoldValidation(Arrays.copyOfRange(strings, 0, 10));
		kfoldVal.validate(10);
		
		
		/*
		String text = "The/NA mayor's/NA present/NA term/NA of/NA office/NA expires/NA Jan./NA 1/NA ./NA";
		String[] input=new String[1];
		input[0]=text;
		
		/*tagger.findTags(Arrays.copyOfRange(strings, 0, 1));
		tagger.trainModel(Arrays.copyOfRange(strings, 0, 1));
		
		String[] ans= tagger.viterbi(Arrays.copyOfRange(strings, 0, 1));
		for(String st :ans){
			System.out.println(st);
		}
		*/
		
		/*tagger.findTags(strings);
		tagger.trainModel(strings);
		System.out.println("Finished Model training");
		
		String[] ans= tagger.viterbi(input);
		for(String st :ans){
			System.out.println(st);
		}
*/
		
		
		
	/*
		tagger.findTags(strings);
		tagger.trainModel(strings);
		System.out.println("Finished Model training");

		String text = "The/NA mayor's/NA present/NA term/NA of/NA office/NA expires/NA Jan./NA 1/NA ./NA";
		String[] input=new String[1];
		input[0]=tagger.prepareInput(text);
		String[] ans= tagger.viterbi(input);
		for(String st :ans){
			System.out.println(st);
		}
		*/
		
		
	}

}
 