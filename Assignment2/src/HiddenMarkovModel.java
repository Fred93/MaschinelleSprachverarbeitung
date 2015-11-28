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

public class HiddenMarkovModel {
	private TransitionManager transitionManager;
	private EmissionManager emissionManager;
	
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
				System.out.println(token);
				String[] res = token.split("/");
				System.out.println(res[1]);
				transitionManager.addTag(res[1]);
				emissionManager.addTerm(res[0]);
			}
		    System.out.println("Finished File");
		}
	    System.out.println("Amount tags: " + transitionManager.getTagSet().size() +" \n\n\n");
	    for (String tag: transitionManager.getTagSet()) {
			System.out.println(tag);
		}
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
	
	public String[] convertTextToArrayList(String s){
		ArrayList<String> strings = new ArrayList<>();
		StringTokenizer stringTokenizer = new StringTokenizer(s, " \t\n\r\f", false);
		while (stringTokenizer.hasMoreElements()) {
			strings.add((String) stringTokenizer.nextElement());
		}
		return (String[]) strings.toArray();
	}
	
	public void viterbi(String s){
		String[] tokens = convertTextToArrayList(s);
		String[] tags = new String[tokens.length];
		viterbiStep(tokens, tags, 0);
	}
	
	public double viterbiStep(String[] tokens, String[] tags, int i){
		/*if (index == 0){
			
		}else{
			String token = tokens[i];
			if (i == 0){
				String previousTag = TransitionManager.START;
			}else{
				String prevousTag = tags[i-1];
			}
			
		}*/
		return 0;
	}

	public static void main(String[] args) {
		String directory = "brown_learn";
		HiddenMarkovModel tagger = new HiddenMarkovModel();
		File[] files = tagger.readFiles(directory);
		String[] strings = tagger.convertFilesToStrings(files);
		tagger.findTags(strings);
		tagger.trainModel(strings);
		
	}

}
