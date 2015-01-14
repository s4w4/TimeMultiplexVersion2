package station;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MessageManager {

	private Logger logger;
	private ClockManager clockManager;
	/**
	 * Slot an dem zuletzt im aktuellen Frame eine Nachricht empfangen wurde
	 * 
	 * !!! 0 = Es wurden noch keine Nachrichten im aktuellen Frame empfangen
	 */
	private byte lastReceivedSlot = 0;
	private Message lastMessage;
	private List<Byte> freeSlots;
	private List<Message> allReceivedMessage;
	private Random random;
	private Message ownMessage;
	private byte reservedSlot = 0;

	public MessageManager(Logger logger, ClockManager clockManager) {
		this.logger = logger;
		this.clockManager = clockManager;
		this.allReceivedMessage = new ArrayList<Message>();
		this.freeSlots = resetFreeSlots(clockManager.getSlotCount());
		this.random = new Random();
	}

	/**
	 * erstellt eine Liste mit freien Slots
	 * 
	 * @param slotCount
	 * @return
	 */
	private List<Byte> resetFreeSlots(int slotCount) {
		List<Byte> tempSlots = new ArrayList<Byte>();
		for (int i = 1; i <= slotCount; i++) {
			tempSlots.add((byte) i);
		}
		return tempSlots;
	}

	/**
	 * 
	 * @param currentMessage
	 */
	public void receivedMessage(Message currentMessage) {
		allReceivedMessage.add(currentMessage);
		currentMessage.setSendingSlot(this.clockManager.getCurrentSlot());
//		System.out.println("************* RECEIVED "+allReceivedMessage.size());		
		if (lastMessage != null && isKollision(currentMessage)) {
			handleKollision(currentMessage);
		} else {
			freeSlots.remove((Byte) currentMessage.getReservedSlot());
			this.clockManager.calcCorrection(currentMessage);
		}

		this.lastMessage = currentMessage;
	}

	/**
	 * Kollisionbehandlung
	 * 
	 * @param currentMessage
	 */
	private void handleKollision(Message currentMessage) {
		System.out.println("HANDLE KOLLISION "
				+ currentMessage.getSendingSlot() + " : "
				+ lastMessage.getSendingSlot());
		int index = allReceivedMessage.size();
		allReceivedMessage.get(index - 1).setKollision(true);
		allReceivedMessage.get(index - 2).setKollision(true);

		if (!lastMessage.isKollision()) {
			this.freeSlots.add(lastMessage.getReservedSlot());
			this.clockManager.rollbackLastCorrection(lastMessage);
		}
		lastMessage.setKollision(true);
		currentMessage.setKollision(true);

		if (this.ownMessage != null
				&& currentMessage.getSendingSlot() == this.ownMessage
						.getSendingSlot()) {
			this.ownMessage.setKollision(true);
		}

	}

	/**
	 * prüft ob aktuelle Nachricht kollidiert
	 * 
	 * @param currentMessage
	 * @return
	 */
	private boolean isKollision(Message currentMessage) {
		boolean kollision = lastMessage.getSendingSlot() == currentMessage
				.getSendingSlot();
		return kollision;
	}

	public void resetFrame() {
		System.out.println("==================================================================================");
		this.freeSlots = resetFreeSlots(clockManager.getSlotCount());
		logger.printMessages(allReceivedMessage);
		this.allReceivedMessage = new ArrayList<Message>();
		this.ownMessage = null;
		this.lastMessage = null;
	}

	public boolean isOwnKollision() {
		if (this.ownMessage == null) {
			return false;
		}
		return this.ownMessage.isKollision();
	}

	/**
	 * @param ownMessage
	 *            the ownMessage to set
	 */
	public void setOwnMessage(Message ownMessage) {
		this.ownMessage = ownMessage;
	}

	/**
	 * prüft ob es noch es noch freie Slots gibt
	 * 
	 * @return
	 */
	public boolean isFreeSlotNextFrame() {
		return freeSlots.size() > 0;
	}

	/**
	 * Waehlt einen freien zufaelligen Slot aus
	 * 
	 * @return
	 */
	public byte getFreeSlot() {
		if (this.reservedSlot > 0) {
			System.out.println("RESERVED SLOT : " + reservedSlot);
			return reservedSlot;
		} else {
			byte cS = calcNewSlot();
			System.out.println("CALC SLOT: " + cS);
			return cS;
		}
	}

	public byte calcNewSlot() {
		return freeSlots.get(random.nextInt(freeSlots.size()));
	}

	/**
	 * @return the reservedSlot
	 */
	public byte getReservedSlot() {
		return reservedSlot;
	}

	/**
	 * @param reservedSlot
	 *            the reservedSlot to set
	 */
	public void setReservedSlot(byte reservedSlot) {
		this.reservedSlot = reservedSlot;
	}

}
