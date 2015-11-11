import java.util.HashMap;

import java.util.StringTokenizer;

public class TokenAnalyzer implements Runnable {
	private StringTokenizer st;
	
	private XMLParser manager;
	
	public TokenAnalyzer(XMLParser manager, StringTokenizer st) {
		this.st = st;
		this.manager = manager;
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
		manager.addTokenMap(tokens);

	}
}
