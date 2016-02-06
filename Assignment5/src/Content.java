import java.util.ArrayList;

public class Content {
	public ArrayList<String> sententces;
	public ArrayList<String> tags;
	
	public Content(ArrayList<String> sententces){
		this.sententces = sententces;
		
	}

	public ArrayList<String> getSentences() {
		return sententces;
	}

	public void setWords(ArrayList<String> sententces) {
		this.sententces = sententces;
	}

	public ArrayList<String> getTags() {
		return tags;
	}

	public void setTags(ArrayList<String> tags) {
		this.tags = tags;
	}
}
