package station;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MessageManager {

	private Logger logger;
	private ClockManager clockManager;
	private List<Byte> freeSlotsCurrentFrame;
	private List<Byte> freeSlotsOldFrame = null;
	private List<Message> allReceivedMessage;
	private Random random;
	private Message ownMessage;
	private byte reservedSlot = 0;
	private boolean allowedSendNextFrame = true;

	public MessageManager(Logger logger, ClockManager clockManager) {
		this.logger = logger;
		this.clockManager = clockManager;
		this.allReceivedMessage = Collections
				.synchronizedList(new ArrayList<Message>());
		this.freeSlotsCurrentFrame = resetFreeSlots(clockManager.getSlotCount());
		this.freeSlotsOldFrame = new ArrayList<Byte>();
		this.random = new Random();
	}

	public void resetFrame() {
		this.freeSlotsOldFrame = resetFreeSlots(clockManager.getSlotCount());
		this.freeSlotsCurrentFrame = resetFreeSlots(clockManager.getSlotCount());
		resetAllMessageFromOldFrame();
		if (freeSlotsCurrentFrame != null) {
			logger.print("RESET: "
						+ "\n OldFrame "+Arrays.toString(freeSlotsOldFrame.toArray())
						+ "\n CurrentFrame "+ Arrays.toString(freeSlotsCurrentFrame.toArray()));
						
		}
		this.ownMessage = null;
	}

	private void resetAllMessageFromOldFrame() {
		ArrayList<Message> messagesToPrint = new ArrayList<Message>();
//		this.freeSlotsOldFrame = new ArrayList<Byte>();
		for (Message m : allReceivedMessage) {
			if (m.isOldFrame() && !m.isKollision()) {
				messagesToPrint.add(m);
				// this.freeSlotsOldFrame.add((Byte) m.getReservedSlot());
				freeSlotsOldFrame.remove((Byte) m.getReservedSlot());
			}else if (!m.isOldFrame()) {
				freeSlotsCurrentFrame.remove((Byte) m.getReservedSlot());
			}

		}
		logger.printMessages(messagesToPrint, clockManager.getCurrentFrame(),
				clockManager.getCorrectionInMS());
		allReceivedMessage.removeAll(messagesToPrint);
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
//		int oldSize = freeSlotsCurrentFrame.size();
		freeSlotsCurrentFrame.remove((Byte) currentMessage.getReservedSlot());

		currentMessage.setReceivedTimeInMS(clockManager.currentTimeMillis());
		currentMessage.setCurrentCorrection(clockManager.getCorrectionInMS());
		checkOwnMessage(currentMessage);
		allReceivedMessage.add(currentMessage);

		// // Es wurde nichts gelöscht Free Slots sind geblieben
		// if ((oldSize == freeSlots.size()) && (currentMessage.isOwnMessage()))
		// {
		// // (reservedSlot == currentMessage.getReservedSlot())) {
		// this.reservedSlot = 0;
		// this.allowedSendNextFrame = false;
		// } else {
		// // Egal
		// }
	}

	private void checkOwnMessage(Message m) {
		if (this.ownMessage != null)
			if (this.clockManager.getCurrentSendingSlot(m) == this.clockManager
					.getCurrentSendingSlot(ownMessage)) {
				m.setOwnMessage(true);
			} else {
				m.setOwnMessage(false);
			}
	}

	/**
	 * Kollisionbehandlung
	 * 
	 * @param currentMessage
	 */
	private void handleKollision() {
		if (allReceivedMessage.size() > 1)
			for (int i = 0; i < allReceivedMessage.size() - 1; i++) {
				Message m1 = allReceivedMessage.get(i);
				Message m2 = allReceivedMessage.get(i + 1);
				if (this.clockManager.getCurrentSendingSlot(m1) == this.clockManager
						.getCurrentSendingSlot(m2)) {
					m1.setKollision(true);
					m2.setKollision(true);
//					removeFreeSlot(m1);
//					removeFreeSlot(m2);
				} else {
					m1.setKollision(false);
					m2.setKollision(false);
//					addFreeSlot(m1);
//					addFreeSlot(m2);
					if (i > 0
							&& this.clockManager
									.getCurrentSendingSlot(allReceivedMessage
											.get(i - 1)) == this.clockManager
									.getCurrentSendingSlot(m1)) {
						allReceivedMessage.get(i - 1).setKollision(true);
						m1.setKollision(true);
//						removeFreeSlot(m1);
					}
				}
			}
	}

	private void addFreeSlot(Message m1) {
		if (!this.freeSlotsCurrentFrame.contains((Byte) m1.getReservedSlot()))
			this.freeSlotsCurrentFrame.add((Byte) m1.getReservedSlot());
	}

	private void removeFreeSlot(Message m1) {
		if (this.freeSlotsCurrentFrame.contains((Byte) m1.getReservedSlot()))
			this.freeSlotsCurrentFrame.remove((Byte) m1.getReservedSlot());
	}

	public void syncMessagesReceivedTime() {
		handleKollision();
		clockManager.sync(allReceivedMessage);
		for (Message m : allReceivedMessage) {
			m.setCurrentCorrection(clockManager.getCorrectionInMS());
			if (clockManager.getCurrentFrame() == clockManager.getFrame(m
					.getReceivedTimeInMS())) {
				m.setOldFrame(false);
			} else {
				m.setOldFrame(true);
			}
			checkOwnMessage(m);
		}
		handleKollision();
		for (Message m : allReceivedMessage) {
			if (!m.isOwnMessage() && !m.isKollision()
					&& m.getReservedSlot() == reservedSlot) {
				// reservedSlot = 0;
				setReservedSlot((byte) 0);
				allowedSendNextFrame = false;
			}
		}
	}

	/**
	 * prüft ob eigene Nachricht kollidiert
	 * 
	 * @param currentMessage
	 * @return
	 */
	public boolean isOwnKollision() {
		boolean isOwnMessageKollision = false;
		for (Message m : allReceivedMessage)
			if (m.isOwnMessage() && m.isKollision())
				isOwnMessageKollision = true;
		return isOwnMessageKollision;
	}

	public boolean isOwnMessageSended() {
		boolean OwnMessageSended = false;
		for (Message m : allReceivedMessage)
			if (m.isOwnMessage())
				OwnMessageSended = true;
		return OwnMessageSended;
	}

	/**
	 * @param ownMessage
	 *            the ownMessage to set
	 */
	public void setOwnMessage(Message ownMessage) {
		this.ownMessage = ownMessage;
	}

	/**
	 * @return the freeSlots
	 */
	public List<Byte> getFreeSlots() {
		return freeSlotsCurrentFrame;
	}

	/**
	 * @param freeSlots
	 *            the freeSlots to set
	 */
	public void setFreeSlots(List<Byte> freeSlots) {
		this.freeSlotsCurrentFrame = freeSlots;
	}

	/**
	 * @return the allReceivedMessage
	 */
	public List<Message> getAllReceivedMessage() {
		return allReceivedMessage;
	}

	/**
	 * @param allReceivedMessage
	 *            the allReceivedMessage to set
	 */
	public void setAllReceivedMessage(List<Message> allReceivedMessage) {
		this.allReceivedMessage = allReceivedMessage;
	}

	/**
	 * @return the ownMessage
	 */
	public Message getOwnMessage() {
		return ownMessage;
	}

	/**
	 * prüft ob es noch es noch freie Slots gibt
	 * 
	 * @return
	 */
	public boolean isFreeSlotNextFrame() {
		return freeSlotsCurrentFrame.size() > 0;
	}

	/**
	 * Waehlt einen freien zufaelligen Slot aus oder gibt reservierte Slot
	 * zurück, wenn es existiert
	 * 
	 * @return
	 */
	public byte getFreeSlot() {

		if (this.reservedSlot > 0) {
			if (freeSlotsOldFrame != null) {
				logger.print("Station GET Reserved Slot "
						+ "\n OldFrame "+Arrays.toString(freeSlotsOldFrame.toArray())
						+ "\n CurrentFrame "+ Arrays.toString(freeSlotsCurrentFrame.toArray())
						+ "\n ReservedSlot: " + reservedSlot);
			}

			return reservedSlot;
		} else {
			byte cS = calcNewSlotStation();

			return cS;
		}
	}

	public byte calcNewSlotStation() {
		byte res = 0;
		if (freeSlotsOldFrame.size() == 0)
			res = 0;
		else if (freeSlotsOldFrame.size() == 1) {
			res = freeSlotsOldFrame.get(0);
			freeSlotsOldFrame.remove((Byte) res);
		} else {
			res = freeSlotsOldFrame.get(random.nextInt(Math.abs((int) System
					.currentTimeMillis() % 1000)) % freeSlotsOldFrame.size());
			freeSlotsOldFrame.remove((Byte) res);
		}
		if (freeSlotsOldFrame != null) {
			logger.print("Station GET FREESLOTS "
					+ "\n OldFrame "+Arrays.toString(freeSlotsOldFrame.toArray())
					+ "\n CurrentFrame "+ Arrays.toString(freeSlotsCurrentFrame.toArray())
					+ "\n RandomSlot: " + res);
		}
		return res;
	}

	public byte calcNewSlotSender() {
		byte res = 0;
		if (freeSlotsCurrentFrame.size() == 0)
			res = 0;
		else if (freeSlotsCurrentFrame.size() == 1) {
			res = freeSlotsCurrentFrame.get(0);
			freeSlotsCurrentFrame.remove((Byte) res);
		} else {
			res = freeSlotsCurrentFrame.get(random.nextInt(Math
					.abs((int) System.currentTimeMillis() % 1000))
					% freeSlotsCurrentFrame.size());
			freeSlotsCurrentFrame.remove((Byte) res);
		}
		if (freeSlotsCurrentFrame != null) {
			logger.print("Sender GET FREESLOTS "
					+ "\n OldFrame "+Arrays.toString(freeSlotsOldFrame.toArray())
					+ "\n CurrentFrame "+ Arrays.toString(freeSlotsCurrentFrame.toArray())
					+ "\n RandomSlot: " + res);
		}
		// reservedSlot = res;
		setReservedSlot(res);
		return res;
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
	public void setReservedSlot(byte slot) {
//		if (slot == 0 && reservedSlot != 0) {
//			this.freeSlotsCurrentFrame.add((Byte) this.reservedSlot);
//		} else {
//			this.freeSlotsCurrentFrame.remove((Byte) slot);
//		}
		this.reservedSlot = slot;
	}

	public boolean isAllowedSend() {
		return this.allowedSendNextFrame;
	}

	public void setAllowedSend(boolean b) {
		this.allowedSendNextFrame = b;
	}

}
