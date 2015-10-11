package sparse;

public class Timer {
	long start, stop, total;
	
	public Timer() {
		 total =0;
		 start = -1;
	}
	
	public void start(){
		start = System.currentTimeMillis();
	}
	
	public long stop(){
		if(start > 0){
			stop = System.currentTimeMillis();
			total += stop - start;
			start = -1;
		}
		return total;
	}
	
	public long total(){
		return total;
	}
	 
	public void reset(){
		total = 0;
		start = -1;
	}
}
