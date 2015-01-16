package station;

import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.util.Random;

public class Station extends Thread {

	/**
	 * Time To Life in sekunden
	 */
	private final int TTL_IN_SEC = 1;

	/**
	 * Netzwerkinterfacename
	 */
	private String interfaceName;

	/**
	 * Multicast Adresse
	 */
	private String mcastAddress;

	/**
	 * Port auf den gelauscht wird
	 */
	private int receivePort;

	/**
	 * Stationklasse
	 */
	private char stationClass;

	/**
	 * DatenSourceReader
	 */
	private DataSourceListener dataSourceListener;

	/**
	 * Multicastsocket
	 */
	private MulticastSocket multicastSocket;

	/**
	 * Empfänger
	 */
	private Receiver receiver;

	/**
	 * Flag zeigt an ob Station beendet werden soll
	 */
	private boolean finish = false;

	/**
	 * Verwaltet die Uhren
	 */
	private ClockManager clockManager;

	/**
	 * Logger
	 */
	private Logger loggerReceiver;

	/**
	 * Message Manager Kollisionsbehandlung und Slotverwaltung
	 */
	private MessageManager messageManager;

	/**
	 * Data Manager Verwaltung der Nachrichten von der Datenquelle
	 */
	private DataManager dataManager;

	/**
	 * Offset
	 */
	private long utcOffsetInMS;

	public Station(String interfaceName, String mcastAddress, int receivePort,
			char stationClass, long utcOffsetInMS) {
		this.interfaceName = interfaceName;
		this.mcastAddress = mcastAddress;
		this.receivePort = receivePort;
		this.stationClass = stationClass;
		this.utcOffsetInMS = utcOffsetInMS;
	}

	@Override
	public void run() {
		try {
//			int rand = new Random().nextInt(Math.abs((int)System.currentTimeMillis()%1000));
//			System.out.println("YYY "+rand);
//			Thread.sleep(rand);
			/****************************************************************
			 * Initialphase
			 ****************************************************************/
			// System.out.println("============== INITIAL PHASE ==============");
			// Create Logger (Datensenke)
			this.loggerReceiver = new Logger("LogReceiver");

			// Create ClockManager
			this.clockManager = new ClockManager(utcOffsetInMS);
			// Create MessageManager
			this.messageManager = new MessageManager(loggerReceiver,
					clockManager);

			// Create MulticastSocket
			multicastSocket = new MulticastSocket(receivePort);
			multicastSocket.setTimeToLive(TTL_IN_SEC);
			multicastSocket.setNetworkInterface(NetworkInterface
					.getByName(interfaceName));
			multicastSocket.joinGroup(InetAddress.getByName(mcastAddress));
			// Create Receiver
			this.receiver = new Receiver(multicastSocket, messageManager);

			// Create DataManager
			this.dataManager = new DataManager();
			// Create DataSourceListener
			this.dataSourceListener = new DataSourceListener(dataManager);

			/****************************************************************
			 * Ablaufphase
			 ****************************************************************/
			// System.out.println("============== ABLAUF PHASE ==============");
			this.dataSourceListener.start();
			this.receiver.start();

			startPhase();
			listeningPhase();

			do {

				if (this.clockManager.isStartFrame()) {

					if (this.messageManager.isOwnKollision()
							|| !this.messageManager.isFreeSlotNextFrame()) {
						messageManager.setReservedSlot((byte)0);
						resetFrame();
					} else {
						
						sendingPhase();
					}

				} else {
					// Zurücksetzen
					messageManager.setReservedSlot((byte) 0);
					resetFrame();
					startPhase();
				}
				listeningPhase();

			} while (!finish);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sendingPhase() {
		
//		System.out.println(">>>>>>>> IS ALLOWED: "+messageManager.isAllowedSend());
		
		if (messageManager.isAllowedSend()) {
			byte freeSlot = this.messageManager.getFreeSlot();
			messageManager.setReservedSlot((byte)0);
			this.resetFrame();
			Sender sender = new Sender(dataManager, messageManager,
					clockManager, multicastSocket, freeSlot, mcastAddress,
					receivePort, stationClass);
			sender.start();
			
		}else {
			messageManager.setReservedSlot((byte)0);
			this.resetFrame();
		}
		messageManager.setAllowedSend(true);
	}

	private void listeningPhase() throws InterruptedException {
		do {
			long timeToNextFrame = this.clockManager.calcToNextFrameInMS();
			Thread.sleep(timeToNextFrame);
			messageManager.syncMessagesReceivedTime();
		} while (!this.clockManager.isEOF());
	}

	private void startPhase() throws InterruptedException {
		do {
			Thread.sleep(this.clockManager.calcToNextFrameInMS());
			messageManager.syncMessagesReceivedTime();
			if (this.clockManager.isEOF()) {
				resetFrame();
			}
		} while (!this.clockManager.isEOF()
				&& !this.clockManager.isStartFrame());
	}

	/**
	 * Setzt Startwert auf Anfangswert
	 */
	private void resetFrame() {
		this.messageManager.resetFrame();
		this.clockManager.resetFrame();
	}

	/**
	 * Beendet die Station
	 */
	public void exit() {
		this.finish = true;
	}

	public static void main(String[] args) {
		if (args.length >= 4) {
			String paramInterfaceName = args[0];
			String paramMcastAddress = args[1];
			int paramReceivePort = Integer.parseInt(args[2]);
			char paramStationClass = args[3].charAt(0);
			long paramUtcOffsetInMS = (args.length == 5) ? Long
					.parseLong(args[4]) : 0;
			new Station(paramInterfaceName, paramMcastAddress,
					paramReceivePort, paramStationClass, paramUtcOffsetInMS)
					.start();
		}
	}

}
