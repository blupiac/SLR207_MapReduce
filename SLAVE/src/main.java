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

	// Mode 0
	
	private String fileContent;
	static ArrayList<String> paths = new ArrayList<String>();
	static int mode;
	private static HashMap<String, Integer> count = new HashMap<String, Integer>();
	private ArrayList<String> words;
	
	// Mode 1
	
	// UM1 : c199-99 not needed?
	private static HashMap<String, String> dictMachines = new HashMap<String, String>();
	// key : [UM1, UM2, ...]
	private static HashMap<String, ArrayList<String>> dictKeys = new HashMap<String, ArrayList<String>>();
	private static HashMap<String, HashMap<String, Integer>> umxContent = new HashMap<String, HashMap<String, Integer>>();

	public void run() {
		
		if(mode == 0)
		{
			try {
				fileContent = readFile(paths.get(0));
			} catch (IOException e) {
				e.printStackTrace();
			}
	
			countWordsParallel();
			writeOutput();
		}
		else
		{
			dictMachines = readMachines("/tmp/blupiac/dicts/machineDict.txt");
			dictKeys = readKeys("/tmp/blupiac/dicts/keyDict.txt");
			
			try {
				for(String path : paths)
				{
					String pathContent = readFile(path);
					String[] wordsArray = pathContent.split("\\r?\\n");
					ArrayList<String> words = new ArrayList<String>(Arrays.asList(wordsArray));
					HashMap<String, Integer> occur = new HashMap<String, Integer>();
					for (String word: words) {
						String[] wordArray = word.split("\\s+");
						occur.put(wordArray[0], Integer.parseInt(wordArray[1]));
					}
					
					umxContent.put("UM" + path.charAt(path.length() - 5), occur);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			joinUM();
		}
	}

	private static void joinUM()
	{
		try {
			PrintWriter SMout = new PrintWriter("output/SM.txt");
			PrintWriter RMout = new PrintWriter("output/RM.txt");
			
			Iterator<Entry<String, ArrayList<String>>> it = dictKeys.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, ArrayList<String>> pair = it.next();
				int num = 0;
				
				for(String umx : pair.getValue())
				{					
					if(umxContent.containsKey(umx))
					{
						HashMap<String, Integer> content = umxContent.get(umx);
						SMout.println(pair.getKey() + " " + content.get(pair.getKey()));
						num += content.get(pair.getKey());
					}
					else
					{
						System.err.println("Unknown UMx: " + umx);
					}
				}
				
				RMout.println(pair.getKey() + " " + num);
		        it.remove(); // avoids a ConcurrentModificationException
		    }
			
			SMout.close();
			RMout.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static HashMap<String, String> readMachines(String path)
	{
		HashMap<String, String> result = new HashMap<String, String>();
		
		String machineFile = "";
		
		try {
			machineFile = readFile(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] machinesArray = machineFile.split("\\r?\\n");
		ArrayList<String> machinesList = new ArrayList<String>(Arrays.asList(machinesArray));		

		for (String word: machinesList) {
			String[] words = word.split("\\s+");
			result.put(words[0], words[1]);
		}
		
		return result;
	}
	
	private static HashMap<String, ArrayList<String>> readKeys(String path)
	{
		HashMap<String, ArrayList<String>> result = new HashMap<String, ArrayList<String>>();
		
		String keyFile = "";
		
		try {
			keyFile = readFile(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] keyArray = keyFile.split("\\r?\\n");
		ArrayList<String> keyList = new ArrayList<String>(Arrays.asList(keyArray));		

		for (String word: keyList) {
			String[] words = word.split("\\s+");
			
			ArrayList<String> umx = new ArrayList<String>();
			for(int i = 1 ; i < words.length ; i++)
			{
				umx.add(words[i].replace(",", "").replace("[", "").replace("]", ""));
			}
			
			result.put(words[0], umx);
		}
		
		return result;
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

			PrintWriter out = new PrintWriter("maps/UM" + paths.get(0).charAt(paths.get(0).length()-5) + ".txt");
			
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
		
		if(args.length < 2)
		{
			System.err.println("This program needs at least 2 arguments:");
			System.err.println("1) Mode, which can be 1 or 0");
			System.err.println("2) paths needed");
		}
		else if(args[0].equals("0"))
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
			paths.add(args[i]);
			i++;
		}
		
		tobj.start();
	}
}
