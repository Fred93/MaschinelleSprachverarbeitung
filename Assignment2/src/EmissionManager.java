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

public class EmissionManager {
	private Set<String> tagSet = new HashSet<String>();
	private Set<String> termSet = new HashSet<String>();
	private double[][] emissionProbabilities;
	public final static String START = "start";
	private List listTagSet;
	private List listTermSet;
	private double lambda = 0.5;
	
	public EmissionManager(){
		tagSet.add(EmissionManager.START);
	}
	
	public void setTagSet(Set<String> set){
		tagSet = set;
	}
	
	public void addTerm(String s){
		termSet.add(s);
	}
	
	public double getEmissionProbability(String tag, String term){
		int pos1 = listTagSet.indexOf(tag);
		int pos2 = listTermSet.indexOf(term);
		return emissionProbabilities[pos1][pos2];
	}
	
	public void calculateEmissionProbalilities(String[] strings){
		//Initialize matrix
		emissionProbabilities = new double[tagSet.size()][termSet.size()];
		
		listTagSet = Arrays.asList(tagSet.toArray(new String[tagSet.size()]));
		listTermSet = Arrays.asList(termSet.toArray(new String[termSet.size()]));
		
		int[][] emissionCounter = countEmissions(strings);
		
		for (int i = 0; i < emissionCounter.length; i++) {
			int rowSum = getRowSum(emissionCounter[i]);
			for (int j = 0; j < emissionCounter[i].length; j++) {
				
				//Simple (bad) smoothing
				emissionProbabilities[i][j] = (emissionCounter[i][j]*1.0+lambda)/(rowSum+lambda*emissionCounter[i].length);
			}
		}
		
		writeArrayAsCsv(emissionProbabilities);
	}
	
	public int getRowSum(int[] row){
		int sum = 0;
		for (int i : row) {
			sum = sum + i;
		}
		return sum;
	}
	
	public int[][] countEmissions(String[] strings){
		//Emission Counter Matrix
		int[][] emissionCounter = new int[tagSet.size()][termSet.size()];
		
		
		//Iterate over all Strings to count emissions
		for (int i = 0; i < 1; i++) {
			String text = strings[i];
			StringTokenizer stringTokenizer = new StringTokenizer(text, " \t\n\r\f", false);
		    while (stringTokenizer.hasMoreElements()) {
				String token = (String) stringTokenizer.nextElement();
				String[] tupel = token.split("/");
				String term = tupel[0];
				String tag = tupel[1];
				emissionCounter[listTagSet.indexOf(tag)][listTermSet.indexOf(term)]++;
			}
		}
		writeArrayAsCsv(emissionCounter);
		return emissionCounter;
	}
	
	public void writeArrayAsCsv(int[][]array){
		try {
			
			System.out.println("Write csv...");
			FileWriter writer = new FileWriter("emissionCounter.csv");			
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
			FileWriter writer = new FileWriter("emissionProbabilities.csv");			
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
