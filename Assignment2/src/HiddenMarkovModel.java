import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.StringTokenizer;

import com.sun.org.apache.xerces.internal.util.SynchronizedSymbolTable;

public class HiddenMarkovModel {
	private TransitionManager transitionManager;
	private EmissionManager emissionManager;
	private String[] assignedTags;
	
	public HiddenMarkovModel(){
		transitionManager = new TransitionManager();
		emissionManager = new EmissionManager();
	}
	
	public File[] readFiles(String directory){
		File dir = new File(directory);
		File[] files = dir.listFiles();
		return files;
	}
	
	public void findTags(String[] strings){
		for (int i = 0; i < 1; i++) {
			String string = strings[i];
			StringTokenizer stringTokenizer = new StringTokenizer(string, " \t\n\r\f", false);
		    while (stringTokenizer.hasMoreElements()) {
				String token = (String) stringTokenizer.nextElement();
				String[] res = token.split("/");
				transitionManager.addTag(res[1]);
				emissionManager.addTerm(res[0]);
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
		ArrayList<String> strings = new ArrayList<>();
		StringTokenizer stringTokenizer = new StringTokenizer(s, " \t\n\r\f", false);
		while (stringTokenizer.hasMoreElements()) {
			strings.add((String) stringTokenizer.nextElement());
		}
		return strings.toArray(new String[strings.size()]);
	}
	
	public void viterbi(String s){
		String[] tokens = convertTextToArray(s);
		String[] tags = new String[tokens.length];
		assignedTags = tags;
		viterbiStep(tokens, tokens.length-1);
	}
	
	public double viterbiStep(String[] tokens, int i){
		
		double maxProbability = 0;
		String bestHit = "";
		if (i == -1){
			maxProbability = 1;
		}else{
			System.out.println("Vitebi step for: " + tokens[i]);
			String token = tokens[i];
			double maxPrevious = viterbiStep(tokens, i-1);
			String previousTag;
			if (i == 0){
				previousTag = TransitionManager.START;
			}else{
				previousTag = assignedTags[i-1];
			}
			
			for (String tag : transitionManager.getTagSet()) {
				double emissionProbability = emissionManager.getEmissionProbability(tag, token);
				double transitionProbabiliy = transitionManager.getTransitionProbability(previousTag, tag);
				if (emissionProbability>0){
				}
				if (token.startsWith("may") && tag.equals("nn$")){
					emissionManager.getEmissionProbability(tag, token);
				}
				double totalProbability = maxPrevious*emissionProbability*transitionProbabiliy;
				if (totalProbability > maxProbability){
					maxProbability = totalProbability;
					bestHit = tag;
				}
			}
			System.out.println("Best Hit: " + bestHit);
			assignedTags[i] = bestHit;
		}
		return maxProbability;
	}

	public static void main(String[] args) {
		String directory = "brown_learn";
		HiddenMarkovModel tagger = new HiddenMarkovModel();
		File[] files = tagger.readFiles(directory);
		String[] strings = tagger.convertFilesToStrings(files);
		tagger.findTags(strings);
		tagger.trainModel(strings);
		System.out.println("Finished Model training");
		String text = "The mayor's present term of office expires Jan. 1 .";
		tagger.viterbi(text);
		
	}

}
