import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.text.html.HTML.Tag;

public class TransitionManager {
	private Set<String> tagSet = new HashSet<String>();
	private double[][] transitionProbabilities;
	public final static String START = "start";
	private List listTagSet;
	
	public TransitionManager(){
		tagSet.add(TransitionManager.START);
	}
	
	public void addTag(String s){
		tagSet.add(s);
	}
	
	public Set<String> getTagSet(){
		return tagSet;
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
				transitionProbabilities[i][j] = (transitionCouter[i][j]*1.0)/rowSum;
			}
		}
		
		writeArrayAsCsv(transitionProbabilities);
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
		writeArrayAsCsv(transitionCounter);
		return transitionCounter;
	}
	
	public void writeArrayAsCsv(int[][]array){
		try {
			
			System.out.println("Write csv...");
			FileWriter writer = new FileWriter("transitionCounter.csv");			
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length; j++) {
					writer.append(array[i][j] + "");
					if (j != array[i].length-1){
						writer.append(";");
					}
				}
				writer.append("\n");
			}
		    writer.flush();
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeArrayAsCsv(double[][]array){
		try {
			
			System.out.println("Write csv...");
			FileWriter writer = new FileWriter("transitionProbabilities.csv");			
			for (int i = 0; i < array.length; i++) {
				for (int j = 0; j < array[i].length; j++) {
					writer.append(array[i][j] + "");
					if (j != array[i].length-1){
						writer.append(";");
					}
				}
				writer.append("\n");
			}
		    writer.flush();
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
