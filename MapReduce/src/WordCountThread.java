import java.util.ArrayList;
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
	
	public WordCountThread() {

	}

	public WordCountThread(Runnable arg0) {
		super(arg0);
	}

	public WordCountThread(String arg0) {
		super(arg0);
	}
	
	private void loadContent(String path, String stopPath)
	{
		
	}

}
