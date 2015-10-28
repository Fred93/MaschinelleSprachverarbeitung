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
			s = s.toLowerCase();
			if(tokens.containsKey(s)){
				tokens.put(s, tokens.get(s) +1);
			}else{
				tokens.put(s, 1);
			}
		}
		//System.out.println(tokens);
		Map<String, Integer> sortedMap = sortByComparator(tokens);
		//System.out.println(sortedMap);
		parser.passTokens(tokens, type);

	}
	
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap) {

		// Convert Map to List
		LinkedList<Map.Entry<String, Integer>> list = new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
                                           Map.Entry<String, Integer> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});

		// Convert sorted map back to a Map
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Integer> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}

}
