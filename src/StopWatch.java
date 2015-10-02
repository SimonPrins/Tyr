import java.util.Date;


public class StopWatch 
{
	long lStartTime = 0;
	
	public void start()
	{
		lStartTime = new Date().getTime(); // start time
	}
	
	public long time()
	{
		long lEndTime = new Date().getTime(); // end time
 
		long difference = lEndTime - lStartTime; // check different
 
		lStartTime = lEndTime;
		
		return difference;
	}
}
