package station;

import java.util.Hashtable;

public class Timer {

	private static final Hashtable<String, Long> startTime = new Hashtable<String, Long>();
	
	public static void start(String id){
		startTime.put(id, new Long(System.currentTimeMillis()));
	}
	
	public static long stop(String id) {
		return System.currentTimeMillis()-((Long)startTime.remove(id)).longValue();
	}
}
