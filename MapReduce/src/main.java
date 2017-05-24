
public class main {
	
	public static void main(String[] args)
	{
		long startTime = System.currentTimeMillis();
		
		WordCount wc = new WordCount( "input/web.wet",
										"input/ignore1.txt");
		
		long loadTime = System.currentTimeMillis();
		System.out.println("Load time: " + (loadTime - startTime) + "ms");
		/*
		wc.filterWords();
		
		long filtertTime = System.currentTimeMillis();
		System.out.println("Filter time: " + (filtertTime  - loadTime) + "ms");*/
		
		//wc.countWordsWithFilter();
		
		wc.countWordsParallel();
		
		long countTime = System.currentTimeMillis();
		System.out.println("Count time: " + (countTime  - loadTime) + "ms");
		
		wc.sortResult();
		
		long sortTime   = System.currentTimeMillis();
		System.out.println("Sort time: " + (sortTime - countTime) + "ms");
		
		//wc.showResult(true, 50);
		
		long endTime   = System.currentTimeMillis();
		System.out.println("Total time: " + (endTime - startTime) + "ms");
		
		//System.out.println(Runtime.getRuntime().availableProcessors());
	}
}
