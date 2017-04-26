import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;



public class WordCount {

	private String fileContent;
	private HashMap<String, Integer> count = new HashMap<String, Integer>();

	// http://www.java67.com/2015/01/how-to-sort-hashmap-in-java-based-on.html
	TreeMap<String, Integer> sorted;
	Set<Entry<String, Integer>> mappings;
	
	public WordCount(String path) {
		try {
			fileContent = readFile(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		countWords();
		
		sorted = new TreeMap<>(count);
		mappings = sorted.entrySet();
	}
	
	Comparator<Entry<String, Integer>> valueComparator = new Comparator<Entry<String,Integer>>()
			{ 
				@Override
				public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2)
				{
					int v1 = e1.getValue();
					int v2 = e2.getValue();
					if (v1 < v2)	return 0;
					else			return 1;
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
			// http://stackoverflow.com/questions/21946042/remove-all-spaces-and-punctuation-anything-not-a-letter-from-a-string
			String word = words[i].replaceAll("[^A-Za-z]+", "").toLowerCase();
			
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

	public void showResult(boolean ordered)
	{
		if(ordered)
		{
			for(Entry<String, Integer> mapping : mappings)
			{
				System.out.println(mapping.getKey() + " : " + mapping.getValue());
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