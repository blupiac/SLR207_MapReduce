import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


// http://stackoverflow.com/questions/2016083/what-is-the-easiest-way-to-parallelize-a-task-in-java
public class WordCountThread extends Thread {

	private HashMap<String, Integer> count = new HashMap<String, Integer>();
	private HashSet<String> stopwords;
	private ArrayList<String> words;
	
	public WordCountThread(HashSet<String> stopwords, ArrayList<String> words) {
		this.words = words;
		this.stopwords = stopwords;
	}

	public WordCountThread(Runnable arg0, HashSet<String> stopwords, ArrayList<String> words) {
		super(arg0);
		this.words = words;
		this.stopwords = stopwords;
	}

	public WordCountThread(String arg0, HashSet<String> stopwords, ArrayList<String> words) {
		super(arg0);
		this.words = words;
		this.stopwords = stopwords;
	}

	public void countWords()
	{			
		for (String word: words) {
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
