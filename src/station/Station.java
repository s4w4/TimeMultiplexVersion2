package station;


import java.io.IOException;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;

public class Station extends Thread {

	/**
	 * Time To Life in sekunden
	 */
	private final int TTL_IN_SEC = 1;

	/**
	 * Wartezeit des Senders in millisekunden
	 */
	private final long SENDER_WAIT_TIME_IN_MILLISEC = 1000;

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
	 * Sender
	 */
	private Sender sender;

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

	private Logger loggerReceiver;
	private Logger loggerSender;

	private MessageManager messageManager;

	private DataManager dataManager;

	private long utcOffsetInMS;

	private Logger loggerStation;

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
			/****************************************************************
			 * Initialphase
			 ****************************************************************/
//			System.out.println("============== INITIAL PHASE ==============");
			// Create Logger (Datensenke)
			this.loggerReceiver = new Logger("LogReceiver");
			this.loggerSender = new Logger("LogSender");
			this.loggerStation = new Logger("LogStation");

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
//			System.out.println("============== ABLAUF PHASE ==============");
			this.dataSourceListener.start();
			this.receiver.start();

			
			startPhase();
			listeningPhase();

			do {
				System.out.println("################################################## hauptweil start");

				if (this.clockManager.isStartFrame()) {
					System.out.println("################################################## ");
					System.out.println("################################################## start frame " + clockManager.getCurrentFrame() +" CT = "+ clockManager.currentTimeMillis());
					System.out.println("################################################## ");
					loggerStation.print(clockManager.getCurrentFrame()+": start " + clockManager.getCurrentFrame()  +" CT = "+ clockManager.currentTimeMillis());

					if (this.messageManager.isOwnKollision()
							|| !this.messageManager.isFreeSlotNextFrame()) {
						loggerStation.print(clockManager.getCurrentFrame()+"################################################## : OwnKollision OR NoFreeSlotsInNextFrame");
						resetFrame();
					} else {
//						if (!this.messageManager.isOwnMessageSended()){
							loggerStation.print(clockManager.getCurrentFrame()+"################################################## : keine OwnKollision OR FreeSlotsInNextFrame");

							sendingPhase();
//						}
					}

				} else {
					//Zurücksetzen
					loggerStation.print(clockManager.getCurrentFrame()+"################################################## : hauptwhlie else");
					messageManager.setReservedSlot((byte) 0);
					resetFrame();
					startPhase();
				}
				listeningPhase();
				loggerStation.print(clockManager.getCurrentFrame()+"################################################## : hauptwhlie finish ");


			} while (!finish);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally { 
			loggerStation.print(clockManager.getCurrentFrame()+"################################################## : Station Beendet");
		}
	}

	private void sendingPhase() {
		byte freeSlot = this.messageManager.getFreeSlot();
		loggerStation.print(clockManager.getCurrentFrame()+": [[[[[[[[[[[[[[[[SendingPhase]]]]]]]]]]]]]]]] ("+freeSlot+")");
		this.resetFrame();
		Sender sender = new Sender(dataManager, messageManager, clockManager,
				multicastSocket, freeSlot, mcastAddress, receivePort,
				stationClass, loggerSender);
		sender.start();
	}

	private void listeningPhase() throws InterruptedException {
		loggerStation.print(clockManager.getCurrentFrame()+": [[[[[[[[[[[[[[[[ListeningPhase]]]]]]]]]]]]]]]] start");
		do {
			long timeToNextFrame = this.clockManager.calcToNextFrameInMS(); 
			loggerStation.print(clockManager.getCurrentFrame()+": [[[[[[[[[[[[[[[[ListeningPhase]]]]]]]]]]]]]]]] while sleep="+timeToNextFrame);
			Thread.sleep(timeToNextFrame);
			messageManager.syncMessagesReceivedTime();
		} while (!this.clockManager.isEOF());
	}

	private void startPhase() throws InterruptedException {
		loggerStation.print(clockManager.getCurrentFrame()+": [[[[[[[[[[[[[[[[StartPhase]]]]]]]]]]]]]]]] ");
		do {
			loggerStation.print(clockManager.getCurrentFrame()+": [[[[[[[[[[[[[[[[StartPhase]]]]]]]]]]]]]]]] 1");		
			Thread.sleep(this.clockManager.calcToNextFrameInMS());
			messageManager.syncMessagesReceivedTime();
			if (this.clockManager.isEOF()) {
				loggerStation.print(clockManager.getCurrentFrame()+": [[[[[[[[[[[[[[[[StartPhase]]]]]]]]]]]]]]]] 2");		
				resetFrame();
			}
		} while (!this.clockManager.isEOF()
				&& !this.clockManager.isStartFrame());
	}

	/**
	 * Setzt Startwert auf Anfangswert
	 */
	private void resetFrame() {
		loggerStation.print(clockManager.getCurrentFrame()+": ResetFrame");
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
		String paramInterfaceName = args[0];
		String paramMcastAddress = args[1];
		int paramReceivePort = Integer.parseInt(args[2]);
		char paramStationClass = args[3].charAt(0);
		long paramUtcOffsetInMS = Long.parseLong(args[4]);

		new Station(paramInterfaceName, paramMcastAddress, paramReceivePort,
				paramStationClass, paramUtcOffsetInMS).start();
	}

}
