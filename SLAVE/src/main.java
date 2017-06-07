
public class main implements Runnable{
	public void run()
	{
		try {
			Thread.sleep(10000);
			System.out.printf("%d", 2+3);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]){  
		main obj = new main();  
		Thread tobj =new Thread(obj);  
		tobj.start();  
	} 
}
