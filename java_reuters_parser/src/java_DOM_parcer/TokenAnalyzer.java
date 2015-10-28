package java_DOM_parcer;

import java.util.HashMap;
import java.util.StringTokenizer;

public class TokenAnalyzer implements Runnable {
	private StringTokenizer st;
	private HashMap<String, Integer> tokens = new HashMap<>();
	
	public TokenAnalyzer(StringTokenizer st) {
		this.st = st;
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
		System.out.println(tokens);

	}

}
