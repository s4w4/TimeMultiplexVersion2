import station.ClockManager;
import station.Logger;
import station.MessageManager;
import station.Receiver;
import station.Sender;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class App2 {
    private static final int PORT = 16000;
    private static final int TTL_IN_SEC = 1;
    private static final String INTERFACE_NAME = "eth0";
    private static final String MULTICASTADDRESS = "225.10.1.2";

    public static void main(String[] args){
        try {
            System.out.println("-------------Start---------------");

            MulticastSocket multicastSocket = new MulticastSocket(PORT);
            multicastSocket.setTimeToLive(TTL_IN_SEC);
            multicastSocket.setNetworkInterface(NetworkInterface.getByName(INTERFACE_NAME));
            multicastSocket.joinGroup(InetAddress.getByName(MULTICASTADDRESS));

            System.out.println("-------------Senden---------------");
//            Sender sender = new Sender(multicastSocket,1000,MULTICASTADDRESS,PORT);
//            sender.start();
            
            System.out.println("-------------Empfangen---------------");
            Logger logger = new Logger();
			ClockManager clockManager = new ClockManager(0);
			MessageManager messageManager = new MessageManager(logger, clockManager);
			Receiver receiver = new Receiver(multicastSocket, messageManager );
            receiver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
