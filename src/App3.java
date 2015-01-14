import station.Station;

public class App3 {
	private static String INTERFACE_NAME_DEFAULT = "eth0";
	private static String MCASTADDRESS_DEFAULT = "225.10.1.2";
	private static int RECEIVE_PORT_DEFAULT = 16000;
	private static char STATION_CLASS_DEFAULT = 'A';
	private static long UTC_OFFSET_MS_DEFAULT = 1;

	public static void main(String[] args) {
		Station s1 = new Station(INTERFACE_NAME_DEFAULT, MCASTADDRESS_DEFAULT, RECEIVE_PORT_DEFAULT, STATION_CLASS_DEFAULT, UTC_OFFSET_MS_DEFAULT);
		s1.start();
		
	}
}
