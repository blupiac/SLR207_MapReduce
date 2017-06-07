import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;


public class main {

	private static String fileContent;

	public static void main(String[] args)
	{
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

			String result = runProcess(pb, 5);

			if(result == null || !result.toLowerCase().equals(word.toLowerCase()))
			{
				System.err.println("Machine " + result + " not responding.");
				continue;
			}

			pb = new ProcessBuilder("ssh", 
					"blupiac@" + word,
					"cd /tmp ; rm -rf blupiac");

			runProcess(pb, 5);
			
			pb = new ProcessBuilder("ssh", 
					"blupiac@" + word,
					"cd /tmp ; mkdir -p blupiac");

			runProcess(pb, 5);
			

			pb = new ProcessBuilder("scp", 
					"-pr",
					"input/SLAVE.jar",
					"blupiac@" + word + ":/tmp/blupiac/");

			runProcess(pb, 5);

		}



	}


	private static String readFile(String path) 
			throws IOException 
	{
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded);
	}

	private static String runProcess(ProcessBuilder p, long TIMEOUT)
	{
		String lastInput = null;

		BlockingQueue<String> standardBQ = new ArrayBlockingQueue<String>(1024);
		BlockingQueue<String> errorBQ = new ArrayBlockingQueue<String>(1024);

		try {

			Process process = p.start();

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
					System.out.println(stdOut);
					lastInput = stdOut;
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
					return "Timeout reached.";
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}


		} catch (IOException e) {
			e.printStackTrace();
		}

		return lastInput;

	}



}
