// https://stackoverflow.com/questions/11510409/how-to-monitor-external-process-ran-by-processbuilder

public class CompleteChecker implements Runnable {
	private final Process _proc;
	private volatile boolean _complete;

	public CompleteChecker(Process proc) {
		_proc = proc;
		_complete = false;
	}

	public boolean isComplete() { return _complete; }

	public void run() {
		try {
			_proc.waitFor();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		_complete = true;
	}

	public static CompleteChecker create(Process proc) {
		CompleteChecker compCheck = new CompleteChecker(proc);
		Thread t = new Thread(compCheck);
		t.start();
		return compCheck;
	}
}
