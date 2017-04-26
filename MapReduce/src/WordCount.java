import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.regex.Pattern;



public class WordCount {

	private String fileContent;
	private HashMap<String, Integer> count = new HashMap<String, Integer>();

	// http://www.java67.com/2015/01/how-to-sort-hashmap-in-java-based-on.html
	LinkedHashMap<String, Integer> sorted;
	Set<Entry<String, Integer>> mappings;
	
	public WordCount(String path) {
		try {
			fileContent = readFile(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		countWords();
		
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
	
	Comparator<Entry<String, Integer>> valueComparator = new Comparator<Entry<String,Integer>>()
	{ 
		@Override
		public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2)
		{
			int v1 = e1.getValue();
			int v2 = e2.getValue();
			String k1 = e1.getKey();
			String k2 = e2.getKey();
			if (v1 < v2)					return 0;
			else if (v1 > v2)				return 1;
			else if(k1.compareTo(k2) < 0)	return 0;	// v1 == v2
			else							return 1;
		}
	};

	// http://stackoverflow.com/questions/326390/how-do-i-create-a-java-string-from-the-contents-of-a-file
	private static String readFile(String path) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}

	private void countWords()
	{
		String[] words = fileContent.split("\\s+");

		for(int i = 0; i < words.length; i++)
		{
			// http://stackoverflow.com/questions/1611979/remove-all-non-word-characters-from-a-string-in-java-leaving-accented-charact
			String word = words[i].replaceAll("[^\\p{L}\\p{Nd}]+", "").toLowerCase();
			word = word.replaceAll("^[\\s\\.\\d]+", "");
			
			if(count.containsKey(word) && !word.matches(".*\\d+.*"))
			{
				count.put(word, count.get(word) + 1);
			}
			else if(!word.matches(".*\\d+.*"))
			{
				count.put(word, 1);
			}
		}
	}
	
	// http://stackoverflow.com/questions/1453171/remove-diacritical-marks-
	// %C5%84-%C7%B9-%C5%88-%C3%B1-%E1%B9%85-%C5%86-%E1%B9%87-%E1%B9%8B-%E1%B9%89-%CC%88-%C9%B2-%C6%9E-%E1%B6%87-%C9%B3-%C8%B5-from-unicode-chars
/*	public static final Pattern DIACRITICS_AND_FRIENDS
	    = Pattern.compile("[\\p{InCombiningDiacriticalMarks}\\p{IsLm}\\p{IsSk}]+");
	
	private static String stripDiacritics(String str) {
	    str = Normalizer.normalize(str, Normalizer.Form.NFD);
	    str = DIACRITICS_AND_FRIENDS.matcher(str).replaceAll("");
	    return str;
	}
*/
	public void showResult(boolean ordered)
	{
		if(ordered)
		{
			for (String name: sorted.keySet()){
				System.out.println(name + " : " + count.get(name));
			}
		}
		else
		{
			for (String name: count.keySet()){
				System.out.println(name + " : " + count.get(name));
			} 
		}
	}
}