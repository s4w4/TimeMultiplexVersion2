package station;

import java.util.List;

public class ClockManager {

	private final char CLASS_A = 'A';

	/**
	 * die Länge eines Slots in Millisekunden
	 */
	private final long SLOT_TIME_IN_MS = 40;

	/**
	 * der Korrekturwert in Millisekunden
	 */
	private long correctionInMS;

	/**
	 * Anzahl Slots in einem Frame
	 */
	private int slotCount = (int) (1000 / SLOT_TIME_IN_MS);

	/**
	 * Korrekturwert des letzten Slots
	 */
	private long correctionLastSlotInMS = 0;

	/**
	 * Anzahl aller A Station im aktuellen Frame
	 */
	private int countStations = 0;

	private long frameStartDistance;

	private boolean newFrame = false;

	public ClockManager(long utcOffsetInMS) {
		this.correctionInMS = utcOffsetInMS;

	}

	/**
	 * 
	 * @return
	 */
	public boolean isEOF() {
//		System.out.println(newFrame + " --- " + getCorrectedTimeInMS() + " ==== " + correctionInMS) ;
		return newFrame;
	}

	/**
	 * liefert aktuellen Slot
	 * 
	 * Slot beginnt bei 1
	 * @param currentMessage 
	 */
	public byte getCurrentSlot() {
		return (byte) (((getCorrectedTimeInMS() % 1000) / SLOT_TIME_IN_MS) + 1);
	}
	
	public byte getCorrectedSendingSlot(Message message) {
		return (byte) (((message.getCorrectedTimeInMS() % 1000) / SLOT_TIME_IN_MS) + 1);
	}

	/**
	 * liefert die akutelle Systemzeit plus den korregierten Wert in
	 * Millisekunden
	 * 
	 * @return
	 */
	public long getCorrectedTimeInMS() {
		return System.currentTimeMillis() + correctionInMS;
	}

	public int getSlotCount() {
		return this.slotCount;
	}

	/**
	 * Berechnet die Zeit wie lange es noch bis zum ende des Frames dauert in
	 * Millisekunden
	 * 
	 * @return
	 */
	public long calcToNextFrameInMS() {
		this.frameStartDistance = getCorrectedTimeInMS() - getCurrentFrame() * 1000;
		return 1000 - (getCorrectedTimeInMS() % 1000);
	}
	

	/**
	 * Synchronisiert Korregierte Stationszeit
	 * @param allReceivedMessage 
	 */
	public void sync(List<Message> allReceivedMessage) {
		
		long timeDiffSum = 0; 
		for (Message m : allReceivedMessage){
			if ((m.getStationClass() == CLASS_A) && !m.isKollision()) {
				long sendtime = m.getSendTime(); 
				long receivedTime = m.getCorrectedTimeInMS();
				timeDiffSum += m.getCorrectedTimeInMS()-m.getSendTime();
				this.countStations++; 				
			}
		}
		
		correctionInMS += timeDiffSum / (countStations == 0 ? 1 : countStations); 
		
		if (frameStartDistance - correctionInMS < 0 ){
			newFrame = false; 
		}else{
			newFrame = true; 
		}
		
	}

	public boolean isStartFrame() {
		return this.getCurrentSlot() == 1;
	}

	/**
	 * Reseted den ClockManager
	 */
	public void resetFrame() {
		this.countStations = 0;
		this.correctionLastSlotInMS = 0;
		this.newFrame = false; 
		frameStartDistance = 0; 
	}

	public long calcTimeUntilSlotInMS(byte slot) {
		return ((slot - 1) * SLOT_TIME_IN_MS );// + SLOT_TIME_IN_MS/2;
	}

	public long getCurrentFrame() {
		return getCorrectedTimeInMS() / 1000;
	}
	public long getFrame(long time) {
		return time / 1000;
	}
	/**
	 * @return the correctionInMS
	 */
	public long getCorrectionInMS() {
		return correctionInMS;
	}

	/**
	 * @param correctionInMS the correctionInMS to set
	 */
	public void setCorrectionInMS(long correctionInMS) {
		this.correctionInMS = correctionInMS;
	}
	

}
