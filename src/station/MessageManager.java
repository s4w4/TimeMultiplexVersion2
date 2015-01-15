package station;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MessageManager {
	private Logger logger;
	private ClockManager clockManager;
	private Message ownMessage;
	private List<Message> allReceivedMessage;
	private List<Byte> freeSlots; 

	public MessageManager(Logger logger, ClockManager clockManager) {
		this.logger = logger;
		this.clockManager = clockManager;
		this.allReceivedMessage = new ArrayList<Message>();
		this.freeSlots = resetFreeSlots(clockManager.getSlotCount()); 
	}

	public void receivedMessage(Message message) {
		message.setStationTimeInMS(System.currentTimeMillis());
		message.setCurrentCorrection(clockManager.getCorrectionInMS());
		checkOwnMessage(message);
		allReceivedMessage.add(message);
	}

	private void checkOwnMessage(Message message) {
		if (this.ownMessage != null)
			if (this.clockManager.getCorrectedSendingSlot(message) == this.clockManager
					.getCorrectedSendingSlot(ownMessage)) {
				message.setOwnMessage(true);
			} else {
				message.setOwnMessage(false);
			}
	}

	public void syncMessagesReceivedTime() {
		handleKollision();
		clockManager.sync(allReceivedMessage);
		long currentFrame = clockManager.getCurrentFrame();
		for (Message m : allReceivedMessage) {
			m.setCurrentCorrection(clockManager.getCorrectionInMS()); 
			if (currentFrame > clockManager.getFrame(m
					.getCorrectedTimeInMS())) {
				m.setOldFrame(true);
			} else {
				m.setOldFrame(false);
			}
			checkOwnMessage(m);
		}
		handleKollision();
	}

	private void handleKollision() {
		if (allReceivedMessage.size() > 1) {
			for (int i = 0; i < allReceivedMessage.size() - 1; i++) {
				Message m1 = allReceivedMessage.get(i);
				Message m2 = allReceivedMessage.get(i + 1);
				if (this.clockManager.getCorrectedSendingSlot(m1) == this.clockManager
						.getCorrectedSendingSlot(m2)) {
					m1.setKollision(true);
					m2.setKollision(true);
				} else {
					m1.setKollision(false);
					m2.setKollision(false);
					if (i > 0 && this.clockManager.getCorrectedSendingSlot(allReceivedMessage.get(i-1)) == this.clockManager
							.getCorrectedSendingSlot(m1)) {
						allReceivedMessage.get(i-1).setKollision(true);
						m1.setKollision(true);
					}
				}
			}
		} 

	}

	public void resetFrame() {
		this.freeSlots = resetFreeSlots(clockManager.getSlotCount());
		resetAllMessageFromOldFrame(); 					
		this.ownMessage = null;
	}
	
	private void resetAllMessageFromOldFrame() {
		ArrayList<Message> messagesToPrint = new ArrayList<Message>();
		ArrayList<Message> tmpMessage = new ArrayList<Message>(allReceivedMessage);
		for (Message m : tmpMessage){
			if (m.isOldFrame()){
				messagesToPrint.add(m);
				allReceivedMessage.remove(m);
				freeSlots.remove((Byte) m.getReservedSlot()); 
			}
		}
		logger.printMessages(messagesToPrint, clockManager.getCurrentFrame()-1, clockManager.getCorrectionInMS());
	}

	private List<Byte> resetFreeSlots(int slotCount) {
		List<Byte> tempSlots = new ArrayList<Byte>();
		for (int i = 1; i <= slotCount; i++) {
			tempSlots.add((byte) i);
		}
		return tempSlots;
	}

	public boolean isOwnKollision() {
		boolean isOwnMessageKollision = false; 
		for (Message m : allReceivedMessage)
			if (m.isOwnMessage() && m.isKollision())
				isOwnMessageKollision = true; 
		return isOwnMessageKollision;
	}

	public boolean isFreeSlotNextFrame() {
		return freeSlots.size() > 0;
	}

	public boolean isOwnMessageSended() {
		boolean OwnMessageSended = false; 
		for (Message m : allReceivedMessage)
			if (m.isOwnMessage())
				OwnMessageSended = true; 
		return OwnMessageSended;
	}

	/****************************************************************************
	 * GETTER UND SETTER
	 ****************************************************************************/
	/**
	 * @return the ownMessage
	 */
	public Message getOwnMessage() {
		return ownMessage;
	}

	/**
	 * @param ownMessage
	 *            the ownMessage to set
	 */
	public void setOwnMessage(Message ownMessage) {
		this.ownMessage = ownMessage;
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
	 * @return the freeSlots
	 */
	public List<Byte> getFreeSlots() {
		return freeSlots;
	}

	/**
	 * @param freeSlots the freeSlots to set
	 */
	public void setFreeSlots(List<Byte> freeSlots) {
		this.freeSlots = freeSlots;
	}


}
