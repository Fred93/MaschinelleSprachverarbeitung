package java_DOM_parcer;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;

public class TokenAnalyzer implements Runnable {
	private StringTokenizer st;
	private HashMap<String, Integer> tokens = new HashMap<>();
	private XMLParser parser;
	private int type;
	
	public TokenAnalyzer(XMLParser parser, StringTokenizer st, int type) {
		this.st = st;
		this.parser = parser;
		this.type = type;
	}

	@Override
	public void run() {
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if(tokens.containsKey(s)){
				tokens.put(s, tokens.get(s) +1);
			}else{
				tokens.put(s, 1);
			}
		}
		//System.out.println(tokens);
		//System.out.println("      ");
		//Map<String, Integer> sortedMap = sortByComparator(tokens);
		//System.out.println(sortedMap);
		//System.out.println("      ");
		parser.passTokens(tokens, type);

	}
}
