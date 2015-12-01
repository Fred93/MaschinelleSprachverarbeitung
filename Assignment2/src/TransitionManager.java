import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import utils.CSVWriter;

public class TransitionManager {
	private Set<String> tagSet = new HashSet<String>();
	private double[][] transitionProbabilities;
	public final static String START = "start";
	private List<String> listTagSet;
	private double lambda = 0;
	
	public TransitionManager(double lambda){
		this.lambda = lambda;
		tagSet.add(TransitionManager.START);
	}
	
	public void addTag(String s){
		tagSet.add(s);
	}
	
	public Set<String> getTagSet(){
		return tagSet;
	}
	
	public int getTagIndex(String tag){
		return listTagSet.indexOf(tag);
	}
	
	public double getTransitionProbability(String fromTag, String toTag){
		return transitionProbabilities[listTagSet.indexOf(fromTag)][listTagSet.indexOf(toTag)];
	}
	
	public void calculateTransitionProbalilities(String[] strings){
		//Initialize matrix
		transitionProbabilities = new double[tagSet.size()][tagSet.size()];
		
		listTagSet = Arrays.asList(tagSet.toArray(new String[tagSet.size()]));
		
		int[][] transitionCouter = countTransitions(strings);
		
		for (int i = 0; i < transitionCouter.length; i++) {
			int rowSum = getRowSum(transitionCouter[i]);
			for (int j = 0; j < transitionCouter.length; j++) {
				//Simple  smoothing
				transitionProbabilities[i][j] = (transitionCouter[i][j]*1.0+lambda)/(rowSum+lambda*tagSet.size());
			}
		}
		CSVWriter.writeArrayAsCsv(transitionProbabilities, "emissionProbabilities.csv");
	}
	
	public int getRowSum(int[] row){
		int sum = 0;
		for (int i : row) {
			sum = sum + i;
		}
		return sum;
	}
	
	public int[][] countTransitions(String[] strings){
		//Transition Counter Matrix
		int[][] transitionCounter = new int[tagSet.size()][tagSet.size()];
		
		//Iterate over all Strings to count transitions
		String previousTag = TransitionManager.START;
		for (int i = 0; i < 1; i++) {
			String text = strings[i];
			StringTokenizer stringTokenizer = new StringTokenizer(text, " \t\n\r\f", false);
		    while (stringTokenizer.hasMoreElements()) {
				String token = (String) stringTokenizer.nextElement();
				String tag = token.split("/")[1];
				transitionCounter[listTagSet.indexOf(previousTag)][listTagSet.indexOf(tag)]++;
				previousTag = tag;
			}
		}
		System.out.println(transitionCounter);
		CSVWriter.writeArrayAsCsv(transitionCounter, "transitionCounter.csv");
		return transitionCounter;
	}
}
