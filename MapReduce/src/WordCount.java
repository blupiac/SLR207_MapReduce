import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class WordCount {

	private String fileContent;
	private HashMap<String, Integer> count = new HashMap<String, Integer>();
	private HashSet<String> stopwords;
	private ArrayList<String> words;

	// http://www.java67.com/2015/01/how-to-sort-hashmap-in-java-based-on.html
	LinkedHashMap<String, Integer> sorted;
	Set<Entry<String, Integer>> mappings;
	
	public WordCount(String path, String stopPath) {
		
		String stopwordContent = null;
		
		try {
			fileContent = readFile(path);
			stopwordContent = readFile(stopPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] stopwordsArray = stopwordContent.split("\\s+");
		stopwords = new HashSet<String>(Arrays.asList(stopwordsArray));
		
	}
	
	Comparator<Entry<String, Integer>> valueComparator = new Comparator<Entry<String,Integer>>()
	{ 
		public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2)
		{
			int v1 = e1.getValue();
			int v2 = e2.getValue();
			String k1 = e1.getKey();
			String k2 = e2.getKey();
			
			if (v1 < v2)					return 1;
			else if (v1 > v2)				return -1;
			else return k1.compareTo(k2);	// v1 == v2
		}
	};

	// http://stackoverflow.com/questions/326390/how-do-i-create-a-java-string-from-the-contents-of-a-file
	private static String readFile(String path) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}
	
	public int[] splitString(int fragments)
	{
		int size = fileContent.length();
		
		int[] result = new int[fragments + 1];
		result[0] = 0;
		
		for(int i = 1; i < fragments; i++)
		{
			result[i] = i * size / fragments;
			while(fileContent.charAt(result[i]) != ' ')
				result[i]--;
		}
		
		result[fragments] = size;
		
		return result;
	}
	
	public void filterWords()
	{
		
		// enleve tout avant apostrophes
		//fileContent = fileContent.replaceAll(".*'", "");
		// http://stackoverflow.com/questions/1611979/remove-all-non-word-characters-from-a-string-in-java-leaving-accented-charact
		//fileContent = fileContent.replaceAll("[^\\p{L}\\p{Nd}]+", "");
		
		// enleve nombres et caracteres speciaux
		fileContent = fileContent.replaceAll("^[\\s\\.\\d]+", "").toLowerCase();
		
		String[] wordsArray = fileContent.split("\\s+");
		words = new ArrayList<String>(Arrays.asList(wordsArray));
		
		for (int i = 0; i < words.size(); i ++) 
		{
			words.set(i, words.get(i).replaceAll(".*'", ""));
			// http://stackoverflow.com/questions/1611979/remove-all-non-word-characters-from-a-string-in-java-leaving-accented-charact
			words.set(i, words.get(i).replaceAll("[^\\p{L}\\p{Nd}]+", ""));
			
			if(stopwords.contains(words.get(i)) || words.get(i).length() < 2)
			{
				words.remove(i);
			}
		}
		
	}

	public void countWords()
	{
		String[] wordsArray = fileContent.split("\\s+");
		words = new ArrayList<String>(Arrays.asList(wordsArray));		
		
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
	
	private HashMap<String, Integer> countWords(String s)
	{
		String[] wordsArray = s.split("\\s+");
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		words = new ArrayList<String>(Arrays.asList(wordsArray));		
		
		for (String word: words) {
			if(result.containsKey(word))
			{
				result.put(word, result.get(word) + 1);
			}
			else
			{
				result.put(word, 1);
			}
		}
		
		return result;
	}
	
	public void countWordsParallel()
	{
		int procs = Runtime.getRuntime().availableProcessors() - 1;
		
		final int[] i = new int[procs];
		for(int j = 0; j < procs; j ++) {i[j] = j;}
		
		final int[] idx = splitString(procs);
		
		List<Callable<HashMap<String, Integer> > > tasks = 
				new ArrayList<Callable<HashMap<String, Integer> > >();
		
		for (final int j : i) {
            Callable<HashMap<String, Integer> > c = new Callable<HashMap<String, Integer> >() {
                @Override
                public HashMap<String, Integer> call() throws Exception {
                    return countWords(fileContent.substring(idx[j], idx[j+1]));
                }
            };
            tasks.add(c);
        }
		
		final ExecutorService pool = Executors.newFixedThreadPool(procs);
		
		try {
			
            List<Future<HashMap<String, Integer> > > results = pool.invokeAll(tasks);
            
            for (Future<HashMap<String, Integer> > partialCount : results) {
            	
            	for (Map.Entry<String, Integer> entry : partialCount.get().entrySet())
            	{
            		
            		if(count.containsKey(entry.getKey()))
        			{
        				count.put(entry.getKey(), count.get(entry.getKey()) + entry.getValue());
        			}
        			else
        			{
        				count.put(entry.getKey(), entry.getValue());
        			}
            	}
            	
            }
        
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} finally {
            pool.shutdown();
        }		
		
		
		
		
		
	}
	
	public void countWordsWithFilter()
	{
		String[] words = fileContent.split("\\s+");

		for(int i = 0; i < words.length; i++)
		{
			String word = words[i];
			// enleve tout avant apostrophes
			word = word.replaceAll(".*'", "");
			// http://stackoverflow.com/questions/1611979/remove-all-non-word-characters-from-a-string-in-java-leaving-accented-charact
			word = word.replaceAll("[^\\p{L}\\p{Nd}]+", "").toLowerCase();
			// enleve nombres et caracteres speciaux
			word = word.replaceAll("^[\\s\\.\\d]+", "");
			
			if(word.matches(".*\\d+.*") || stopwords.contains(word) || word.length() < 2)
			{
				continue;
			}
			
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
	
	public void sortResult()
	{
		// Sort method needs a List, so let's first convert Set to List in Java 
		List<Entry<String, Integer>> listOfEntries = new ArrayList<Entry<String, Integer>>(count.entrySet());
		
		// sorting HashMap by values using comparator 
		Collections.sort(listOfEntries, valueComparator);
		sorted = new LinkedHashMap<String, Integer>(listOfEntries.size());
		
		// copying entries from List to Map 
		for(Entry<String, Integer> entry : listOfEntries)
		{
			sorted.put(entry.getKey(), entry.getValue());
		}
	}
	
	
	// ordered: if results need to be ordered
	// numShown: number of entries to be shown, 0 shows all entries
	public void showResult(boolean ordered, int numShown)
	{
		HashMap<String, Integer> target;
		int i = 1;
		
		if(ordered)
		{
			target = sorted;
		}
		else
		{
			target = count;
		}
		
		for (String name: target.keySet())
		{
			System.out.println(name + " : " + count.get(name));
			i++;
			
			if(i == numShown)
			{
				break;
			}
		}
	}
}