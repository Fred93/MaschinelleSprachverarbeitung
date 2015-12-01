package utils;

import java.io.FileWriter;
import java.io.IOException;

public class CSVWriter {
	public static void writeArrayAsCsv(int[][]array, String filename){
		try {
			
			System.out.println("Write csv...");
			FileWriter writer = new FileWriter("filename");			
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
	
	public static void writeArrayAsCsv(double[][]array, String filename){
		try {
			
			System.out.println("Write csv...");
			FileWriter writer = new FileWriter("filename");			
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
