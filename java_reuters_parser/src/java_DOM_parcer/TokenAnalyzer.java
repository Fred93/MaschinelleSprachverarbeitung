package java_DOM_parcer;

import java.util.HashMap;

import java.util.StringTokenizer;

public class TokenAnalyzer implements Runnable {
	private StringTokenizer st;
	
	private TaskManager manager;
	private int type;
	
	public TokenAnalyzer(TaskManager manager, StringTokenizer st, int type) {
		this.st = st;
		this.manager = manager;
		this.type = type;
	}

	@Override
	public void run() {
		HashMap<String, Integer> tokens = new HashMap<>();
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
		manager.addTokenMap(tokens, type);

	}
}
