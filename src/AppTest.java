import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

import station.Station;

public class AppTest {
	private static String INTERFACE_NAME_DEFAULT = "eth0";
	private static String MCASTADDRESS_DEFAULT = "225.10.1.2";
	private static int RECEIVE_PORT_DEFAULT = 16000;
	private static String STATION_CLASS_DEFAULT = "A";
	
	public static void main(String[] args) {
		byte[] bytes = new byte[10];
		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (i+1);
		}
		ByteBuffer buf = ByteBuffer.wrap(bytes,0,4);
	    
	    System.out.println(Arrays.toString(buf.array()));
	    System.out.println(buf.get(0));
	    
	    
 
		
		
	}
}
