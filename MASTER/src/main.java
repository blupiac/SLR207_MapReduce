import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

// http://www.xyzws.com/javafaq/how-to-run-external-programs-by-using-java-processbuilder-class/189

public class main {

	public static void main(String[] args)
	{
		
		String[] command = {"java", "-jar", "/tmp/blupiac/timeSLAVE.jar"};
		ProcessBuilder pb = new ProcessBuilder(command);
		
		BlockingQueue<String> standardBQ = new ArrayBlockingQueue<String>(1024);
		BlockingQueue<String> errorBQ = new ArrayBlockingQueue<String>(1024);

		try {
			
			Process process = pb.start();
		
			InputStream is = process.getInputStream();
			StreamReader standardReader = new StreamReader(standardBQ, is);
	        
	        InputStream es = process.getErrorStream();
	        StreamReader errorReader = new StreamReader(errorBQ, es);
	        
	        new Thread(standardReader).start();
	        new Thread(errorReader).start();
	        
	        try {
	        	
	        	String stdOut, errOut;
	        	long TIMEOUT = 5;
	        	
				while( (stdOut = standardBQ.poll(TIMEOUT, TimeUnit.SECONDS)) != null &&
						stdOut != "EOF")
				{
					System.out.println(stdOut);
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
					System.exit(1);
				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	        
	        
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
}
