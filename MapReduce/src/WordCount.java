import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;



public class WordCount {

	private String fileContent;
	private HashMap<String, Integer> count = new HashMap<String, Integer>();
	
	
	public WordCount(String path) {
		try {
			fileContent = readFile(path);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		countWords();
	}
	
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
			
		}
		else
		{
			for (String name: count.keySet()){
		        System.out.println(name + " : " + count.get(name));
			} 
		}
	}
}