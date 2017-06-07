import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;

// https://stackoverflow.com/questions/29339933/read-and-write-files-in-java-using-separate-threads

public class StreamReader implements Runnable{

	protected BlockingQueue<String> blockingQueue = null;
	protected InputStream is;

	public StreamReader(BlockingQueue<String> blockingQueue, InputStream is){
		this.blockingQueue = blockingQueue; 
		this.is = is;
	}

	@Override
	public void run() {
		BufferedReader br = null;
		try {
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			String buffer =null;
			while((buffer=br.readLine())!=null){
				blockingQueue.put(buffer);
			}
			blockingQueue.put("EOF");  //When end of file has been reached

		} catch (FileNotFoundException e) {

			e.printStackTrace();
		} catch (IOException e) {

			e.printStackTrace();
		} catch(InterruptedException e){

		}finally{
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}



}