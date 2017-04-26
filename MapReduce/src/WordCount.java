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
			if(count.containsKey(words[i]))
			{
				count.put(words[i], count.get(words[i]) + 1);
			}
			else
			{
				count.put(words[i], 1);
			}
		}
	}
	
	public void showResult()
	{
		for (String name: count.keySet()){
	        System.out.println(name + " : " + count.get(name));
		} 
	}
}