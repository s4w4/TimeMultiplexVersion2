package station;


import java.io.IOException;
import java.net.DatagramPacket;
import java.net.MulticastSocket;

public class Receiver extends Thread{
    private static final int BYTE_LENGTH = 34;
    private DatagramPacket datagramPacket;
    private byte[] byteArray;
    private MulticastSocket multicastSocket;
	private MessageManager messageManager;

    public Receiver(MulticastSocket multicastSocket, MessageManager messageManager) {
        this.multicastSocket = multicastSocket;
        this.messageManager = messageManager;
        this.byteArray = new byte[BYTE_LENGTH];
        this.datagramPacket = new DatagramPacket(byteArray, BYTE_LENGTH);
    }

    @Override
    public void run() {
        while (true){
            try { 
            	//auf Nachricht warten
                multicastSocket.receive(datagramPacket);   
                this.messageManager.receivedMessage(new Message(byteArray));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
