
public class main {
	
	public static void main(String[] args)
	{
		WordCount wc = new WordCount("/cal/homes/blupiac/workspace/MapReduce/input/santPub.txt",
										"/cal/homes/blupiac/workspace/MapReduce/input/ignore1.txt");
		wc.showResult(true, 50);
	}
}
