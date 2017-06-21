import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class main {

	private static String inputFile = "allDreams.txt";
	private static String fileContent;
	private static HashMap<String, List<String>> dictKeys = new HashMap<String, List<String>>();
	
	public static void main(String[] args)
	{
		List<String> working = new ArrayList<String>();
		ArrayList<Process> procs = new ArrayList<Process>(); 
		ArrayList<Process> dummy = new ArrayList<Process>();

		long startTime = System.currentTimeMillis();
		
		try {
			fileContent = readFile("input/machines.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		String[] wordsArray = fileContent.split("\\s+");

		for (String word : wordsArray){

			ProcessBuilder pb = new ProcessBuilder("ssh", 
					"blupiac@" + word,
					"hostname");

			List<String> result = runProcess(pb, 5, dummy, true);

			if(result.isEmpty() || !result.get(0).toLowerCase().equals(word.toLowerCase()))
			{
				System.err.println("Machine " + word + " not responding.");
				continue;
			}
			else
			{
				working.add(result.get(0));
			}

		}

		int i = 0;
		createSplits(working.size(), "input/" + inputFile, ' ', "output/S");

		PrintWriter out;
		try {
			out = new PrintWriter("output/machineDict.txt");

			for (String word : working){

				ProcessBuilder pb = new ProcessBuilder("ssh", "blupiac@" + word,
						"cd /tmp ; rm -rf blupiac ; mkdir -p blupiac ;" +
						"cd /tmp/blupiac ; mkdir -p splits ; mkdir -p maps ; mkdir -p dicts ; mkdir -p output");

				runProcess(pb, 5, dummy, true);			

				pb = new ProcessBuilder("scp", 
						"-pr",
						"input/SLAVE.jar",
						"blupiac@" + word + ":/tmp/blupiac/");

				runProcess(pb, 5, dummy, true);

				pb = new ProcessBuilder("scp", 
						"-pr",
						"output/S" + i +".spl",
						"blupiac@" + word + ":/tmp/blupiac/splits/");

				runProcess(pb, 5, dummy, true);
				
				pb = new ProcessBuilder("ssh", 
						"blupiac@" + word,
						"cd /tmp/blupiac ; java -jar SLAVE.jar 0 splits/S" + i +".spl");

				List<String> keys = new ArrayList<String>();
				keys = runProcess(pb, 15, procs, false);
				updateDictKeys(keys, "UM"+i);

				out.println("UM" + i + " " + word);

				i++;
			}

			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		for (Process proc : procs){
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("MAP phase done.");
		
		long mapTime   = System.currentTimeMillis();
		System.out.println("MAP time: " + (mapTime - startTime) + "ms");
		
		//******************************************************************
		//********************** MAP PHASE ENDED ***************************
		//******************************************************************
		
		PrintWriter dictKeysWriter;
		ArrayList<Process> procsShuffleReduce = new ArrayList<Process>();
		try {
			
			dictKeysWriter = new PrintWriter("output/keyDict.txt");
			printDictKeys(dictKeys, dictKeysWriter);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		createSplits(working.size(), "output/keyDict.txt", '\n', "output/keyDict");
		
		i = 0;
		for (String word : working){
			
			// Passing keys to machine shuffling
			ProcessBuilder pb = new ProcessBuilder("scp", 
					"-pr",
					"output/keyDict" + i +".spl",
					"blupiac@" + word + ":/tmp/blupiac/dicts/keyDict.txt");
			
			runProcess(pb, 5, dummy, true);
			
			pb = new ProcessBuilder("scp", 
					"-pr",
					"output/machineDict.txt",
					"blupiac@" + word + ":/tmp/blupiac/dicts/");
			
			runProcess(pb, 5, dummy, true);
			
			// putting maps in first machine
			for (int j = 0; j < working.size(); j++){
				if(!word.equals(working.get(j)))
				{
					pb = new ProcessBuilder("scp", 
							"-pr",
							"blupiac@" + working.get(j) + ":/tmp/blupiac/maps/UM" + j + ".txt",
							"blupiac@" + word + ":/tmp/blupiac/maps/");
					
					runProcess(pb, 5, dummy, true);
				}
			}
			
			// String with all umx for the shuffle command
			String umx = "";
			for (int j = 0; j < working.size(); j++){
				String thisUM = "maps/UM" + j + ".txt ";
				umx += thisUM;
			}
			
			pb = new ProcessBuilder("ssh", 
					"blupiac@" + word,
					"cd /tmp/blupiac ; java -jar SLAVE.jar 1 " + umx);
			
			runProcess(pb, 15, procsShuffleReduce, true);
			i++;
		}
		
		for (Process proc : procsShuffleReduce){
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("SHUFFLE and REDUCE phases done.");
		
		long redTime   = System.currentTimeMillis();
		System.out.println("SHUFFLE and REDUCE time: " + (redTime - mapTime) + "ms");
		
		//******************************************************************
		//**************** SHUFFLE REDUCE PHASE ENDED **********************
		//******************************************************************
		
		i = 0;
		for (String word : working)
		{
			ProcessBuilder pb = new ProcessBuilder("scp", 
					"-pr",
					"blupiac@" + word + ":/tmp/blupiac/output/RM.txt",
					"output/RM" + i + ".txt");
			
			runProcess(pb, 5, dummy, true);
			
			i++;
		}
		
		try {
			PrintWriter RESULTout = new PrintWriter("output/RESULT.txt");
			
			for(i = 0 ; i < working.size() ; i++)
			{
				RESULTout.println(readFile("output/RM" + i + ".txt"));
			}
			
			RESULTout.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms");

	}
	
	private static void updateDictKeys(List<String> keys, String value)
	{
		for (String key : keys) {
		    
			if(dictKeys.containsKey(key))
			{
				dictKeys.get(key).add(value);
			}
			else
			{
				List<String> machines = new ArrayList<String>();
				machines.add(value);
				dictKeys.put(key, machines);
			}
			
		}
	}
	
	private static void printDictKeys(HashMap<String, List<String> > dictKeys, PrintWriter pw)
	{
		for (Entry<String, List<String> > e : dictKeys.entrySet()) {
		    pw.println(e.getKey() + " " + e.getValue());			
		}
	}
	

	private static String readFile(String path) 
			throws IOException 
	{
		return new String(Files.readAllBytes(Paths.get(path)));
	}

	private static List<String> runProcess(ProcessBuilder p, long TIMEOUT, ArrayList<Process> procs, boolean verbose)
	{
		List<String> result = new ArrayList<String>();

		BlockingQueue<String> standardBQ = new ArrayBlockingQueue<String>(1024);
		BlockingQueue<String> errorBQ = new ArrayBlockingQueue<String>(1024);

		try {

			Process process = p.start();

			procs.add(process);
			
			InputStream is = process.getInputStream();
			StreamReader standardReader = new StreamReader(standardBQ, is);

			InputStream es = process.getErrorStream();
			StreamReader errorReader = new StreamReader(errorBQ, es);

			new Thread(standardReader).start();
			new Thread(errorReader).start();

			try {

				String stdOut, errOut;

				while( (stdOut = standardBQ.poll(TIMEOUT, TimeUnit.SECONDS)) != null &&
						stdOut != "EOF")
				{
					if(verbose)	System.out.println(stdOut);
					result.add(stdOut);
				}

				// timeout 1 sec because it already waited for standard output
				while( (errOut = errorBQ.poll(1, TimeUnit.SECONDS)) != null &&
						errOut != "EOF")
				{
					System.err.println(errOut);
				}

				if(stdOut == null || errOut == null)
				{
					System.err.println("Timeout reached.");
					result.add("Timeout reached on: " + p.toString());
					return result;
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}


		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;

	}

	private static void createSplits(int fragments, String inputPath, char separator, String target)
	{
		String fileContent = null;

		try {
			fileContent = readFile(inputPath);
		} catch (IOException e) {
			e.printStackTrace();
		}

		int[] sep = splitString(fragments, fileContent, separator);
				
		for(int i = 0; i < fragments; i++)
		{
			try {

				PrintWriter out = new PrintWriter(target + i + ".spl");
				out.println(fileContent.substring(sep[i], sep[i+1]));
				out.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	private static int[] splitString(int fragments, String s, char separator)
	{
		int size = s.length();

		int[] result = new int[fragments + 1];
		result[0] = 0;

		for(int i = 1; i < fragments; i++)
		{
			result[i] = i * size / fragments;
			while(s.charAt(result[i]) != separator)
				result[i]--;
		}

		result[fragments] = size;

		return result;
	}

}
