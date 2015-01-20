package station;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException; 

public class Sender extends Thread {

	private static final int BYTE_LENGTH = 34;
	private MulticastSocket multicastSocket;
	private byte sendingSlot;
	private DatagramPacket datagramPacket;
	private MessageManager messageManager;
	private ClockManager clockManager;
	private DataManager dataManager;
	private char stationClass;

	public Sender(DataManager dataManager, MessageManager messageManager,
			ClockManager clockManager, MulticastSocket multicastSocket,
			byte sendingSlot, String multicastaddress, int port,
			char stationClass) {
		try {
			this.dataManager = dataManager;
			this.sendingSlot = sendingSlot;
			this.messageManager = messageManager;
			this.clockManager = clockManager;
			this.multicastSocket = multicastSocket;
			this.stationClass = stationClass;
			this.datagramPacket = new DatagramPacket(new byte[BYTE_LENGTH],
					BYTE_LENGTH, InetAddress.getByName(multicastaddress), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		long waitTime = this.clockManager.calcTimeUntilSlotInMS(sendingSlot);
		try {
			if(waitTime < 0)
				System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<0");
			if(waitTime>1000)
				System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>1000");
			Thread.sleep(waitTime);

			// Wenn neue Nachricht vorhanden ist
//			if (dataManager.hasNextData()) {
				// hole Nachricht vom DataManager
				char[] data = dataManager.getData();
				// hole reservierten Slot vom MessageManager
				byte reserveredSlot = messageManager.calcNewSlot();
				// erstelle das Paket
				Message message = new Message(this.stationClass);
				message.setData(data);
				message.setReservedSlot(reserveredSlot);
				message.setSendTime(clockManager.getCorrectedTimeInMS());
				message.setReceivedTimeInMS(clockManager.currentTimeMillis());
				message.setCurrentCorrection(clockManager.getCorrectionInMS());
				datagramPacket.setData(message.toByteArray());

				// zeit prüfen
				byte currentSlot = clockManager.getCurrentSlot();

				if (currentSlot == sendingSlot) {
					// sende Paket ab
					multicastSocket.send(datagramPacket);
					messageManager.setOwnMessage(message);
					messageManager.setReservedSlot(reserveredSlot); 
				} else{
					messageManager.setReservedSlot((byte) 0);
				}
//			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace(); 
		}
	}
}
