package station;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

public class MessageManager1 {

	private Logger logger;
	private ClockManager clockManager;
	private List<Byte> freeSlots;
	private List<Message> allReceivedMessage;
	private Random random;
	private Message ownMessage;
	private byte reservedSlot = 0;

	public MessageManager1(Logger logger, ClockManager clockManager) {
		this.logger = logger;
		this.clockManager = clockManager;
		this.allReceivedMessage = new ArrayList<Message>();
		this.freeSlots = resetFreeSlots(clockManager.getSlotCount());
		this.random = new Random();
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
		currentMessage.setStationTimeInMS(System.currentTimeMillis());
		currentMessage.setCurrentCorrection(clockManager.getCorrectionInMS());
		checkOwnMessage(currentMessage);
		allReceivedMessage.add(currentMessage);
	}
	private void checkOwnMessage(Message m){
		if (this.ownMessage != null)
			if(this.clockManager.getCorrectedSendingSlot(m) == this.clockManager.getCorrectedSendingSlot(ownMessage)) {
				m.setOwnMessage(true);
			}else{
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
			for (int i = 0; i < allReceivedMessage.size()-1; i++){
				Message m1 = allReceivedMessage.get(i);
				Message m2 = allReceivedMessage.get(i+1);
				if (this.clockManager.getCorrectedSendingSlot(m1) == this.clockManager.getCorrectedSendingSlot(m2)){
					m1.setKollision(true);
					m2.setKollision(true);
				}
			}
	}
	
	public void syncMessagesReceivedTime(){
		handleKollision();
		clockManager.sync(allReceivedMessage);
		for (Message m : allReceivedMessage){
			m.setCurrentCorrection(clockManager.getCorrectionInMS());
			if (clockManager.getCurrentFrame() > clockManager.getFrame(m.getCorrectedTimeInMS())){
				m.setOldFrame(true);
			} else {
				m.setOldFrame(false);
			}
			checkOwnMessage(m);
		}
		handleKollision();
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
	 * prüft ob es noch es noch freie Slots gibt
	 * 
	 * @return
	 */
	public boolean isFreeSlotNextFrame() {
		return freeSlots.size() > 0;
	}

	/**
	 * Waehlt einen freien zufaelligen Slot aus 
	 * oder gibt reservierte Slot zurück, wenn es existiert
	 * 
	 * @return
	 */
	public byte getFreeSlot() {
		if (this.reservedSlot > 0) {
			return reservedSlot;
		} else {
			byte cS = calcNewSlot();
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
