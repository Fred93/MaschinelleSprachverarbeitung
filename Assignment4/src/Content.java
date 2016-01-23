import java.util.ArrayList;

public class Content {
	public ArrayList<String> words;
	public ArrayList<String> tags;
	
	public Content(ArrayList<String> words, ArrayList<String> tags){
		this.words = words;
		this.tags = tags;
	}

	public ArrayList<String> getWords() {
		return words;
	}

	public void setWords(ArrayList<String> words) {
		this.words = words;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}
}
