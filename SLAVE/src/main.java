import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class main implements Runnable{

	private String fileContent;
	static ArrayList<String> path = new ArrayList<String>();
	static int mode;
	private static HashMap<String, Integer> count = new HashMap<String, Integer>();
	private ArrayList<String> words;

	public void run() {
		
		if(mode == 0)
		{
			try {
				fileContent = readFile(path.get(0));
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			countWordsParallel();
			writeOutput();
		}
	}

	// http://stackoverflow.com/questions/326390/how-do-i-create-a-java-string-from-the-contents-of-a-file
	private static String readFile(String path) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}

	private static void writeOutput() 
	{
		try {

			PrintWriter out = new PrintWriter("maps/UM" + path.get(0).charAt(path.get(0).length()-5) + ".txt");
			
			Iterator<Entry<String, Integer>> it = count.entrySet().iterator();
		    while (it.hasNext()) {
		    	Entry<String, Integer> pair = (Entry<String, Integer>)it.next();
		    	System.out.println(pair.getKey());
		    	out.println(pair.getKey() + " " + pair.getValue());
		        it.remove(); // avoids a ConcurrentModificationException
		    }
		    
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
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

	public static void main(String args[]){

		main obj = new main();
		Thread tobj =new Thread(obj);
		
		if(args[0].equals("0"))
		{
			mode = 0;
		}
		else if(args[0].equals("1"))
		{
			mode = 1;
		}
		else
		{
			System.err.println("Mode entered in command argument not recognized.");
		}
		
		int i = 1;
		while(i < args.length)
		{
			path.add(args[i]);
			i++;
		}
		
		tobj.start();
	}

}
