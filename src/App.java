import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;

import station.ClockManager;
import station.Logger;
import station.MessageManager;
import station.MessageManager1;
import station.Receiver;
import station.Station;
/**
 * App
 */
public class App {
	public static void main(String[] args) throws IOException {
		int receivePort = 16000;
		String interfaceName = "eth0";
		int TTL_IN_SEC = 1;
		String mcastAddress = "225.10.1.2";
		long utcOffsetInMS = 2;
		
		MulticastSocket multicastSocket = new MulticastSocket(receivePort);
		multicastSocket.setTimeToLive(TTL_IN_SEC);
		multicastSocket.setNetworkInterface(NetworkInterface
				.getByName(interfaceName));
		multicastSocket.joinGroup(InetAddress.getByName(mcastAddress));

		Logger logger = new Logger("Log");
		ClockManager clockManager = new ClockManager(utcOffsetInMS);
		MessageManager  messageManager = new MessageManager(logger , clockManager);
		
		Receiver r = new Receiver(multicastSocket, messageManager);
		r.start();
	}
}
