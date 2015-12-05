import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Helper {
	
	private static PrintWriter writer;


	public Helper(){
		
	}
	
	
	public static File[] readFiles(String directory){
		File dir = new File(directory);
		File[] files = dir.listFiles();
		return files;
	}
	
	
	public static String[] convertFilesToStrings(File[] files){
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


	public static void writeResultstoFiles(String result, String directory, String fileName) throws IOException {
		// TODO Auto-generated method stub

			writer = new PrintWriter(directory+"/"+"ORG_"+fileName);
			writer.print(result);
			
		
		
		
	}
	
	
	
	

}
