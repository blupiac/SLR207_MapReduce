import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;



public class WordCountThread implements Callable<HashMap<String, Integer> > {

	private HashMap<String, Integer> count = new HashMap<String, Integer>();
	private ArrayList<String> wordsList;
	private String words;
	
	public WordCountThread(String words) {
		this.words = words;
	}

	public HashMap<String, Integer> call() {
		countWords();
		return count;		
	}

	public void countWords()
	{
		String[] wordsArray = words.split("\\s+");
		wordsList = new ArrayList<String>(Arrays.asList(wordsArray));
		
		for (String word: wordsList) {
			if(count.containsKey(word))
			{
				count.put(word, count.get(word) + 1);
			}
			else
			{
				count.put(word, 1);
			}
		}
	}

}
