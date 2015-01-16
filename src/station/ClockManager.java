package station;

import java.util.List;

public class ClockManager {

	private final char CLASS_A = 'A';
	private final long TIME_TOLERANZ_IN_MS = 2;

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
	 * Anzahl aller A Station im aktuellen Frame
	 */
	private int countStations = 0;

	private long frameStartDistance;

	private long lastCorrectionInMs = 0;

	// private long currentFrame = 0;

	public ClockManager(long utcOffsetInMS) {
		this.correctionInMS = utcOffsetInMS;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isEOF() {
		return getCurrentSlot() == 1;// frameStartDistance +
										// (correctionInMS%1000) >= 0;
	}

	/**
	 * liefert aktuellen Slot
	 * 
	 * Slot beginnt bei 1
	 * 
	 * @param currentMessage
	 */
	public byte getCurrentSlot() {
		return (byte) (((getCorrectedTimeInMS() % 1000) / SLOT_TIME_IN_MS) + 1);
	}

	public byte getCurrentSendingSlot(Message currentMessage) {
		return (byte) (((currentMessage.getReceivedTimeInMS() % 1000) / SLOT_TIME_IN_MS) + 1);
	}

	/**
	 * liefert die akutelle Systemzeit plus den korregierten Wert in
	 * Millisekunden
	 * 
	 * @return
	 */
	public long getCorrectedTimeInMS() {
		return currentTimeMillis() + correctionInMS;
	}

	public long currentTimeMillis() {
		return System.currentTimeMillis();
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
		this.frameStartDistance = getCorrectedTimeInMS() - getCurrentFrame()
				* 1000;
		return 1000 - (getCorrectedTimeInMS() % 1000);
	}

	/**
	 * Synchronisiert Korregierte Stationszeit
	 * 
	 * @param allReceivedMessage
	 */
	public void sync(List<Message> allReceivedMessage) {
		countStations = 0;
		long timeDiffSum = 0;
		for (Message m : allReceivedMessage) {
			if ((m.getStationClass() == CLASS_A) && !m.isKollision()) {
				long sendtime = m.getSendTime();
				long receivedTime = m.getReceivedTimeInMS();
				timeDiffSum += sendtime - receivedTime;
				countStations++;
			}
		}
		if (countStations == 0) {
			lastCorrectionInMs = timeDiffSum;
		} else {
			lastCorrectionInMs = timeDiffSum / countStations;
		}
		if (Math.abs(lastCorrectionInMs) > TIME_TOLERANZ_IN_MS)
			correctionInMS += lastCorrectionInMs;
	}

	public boolean isStartFrame() {
		return this.getCurrentSlot() == 1;
	}

	/**
	 * Reseted den ClockManager
	 */
	public void resetFrame() {
		this.countStations = 0;
		frameStartDistance = 0;
	}

	public long calcTimeUntilSlotInMS(byte slot) { 
		long t1 = (slot-1);
		long t2 = (t1*SLOT_TIME_IN_MS);
		long t3 = (getCurrentFrame()*1000);
		long t4  = getCorrectedTimeInMS()-t3;
		long temp = t2-t4 ;

//		System.out.println(">>>>> Slot: " + slot + " CS: " + getCurrentSlot()+" Temp: " + temp+" ::::::: t1: "+t1+" t2: "+t2+" t3: "+t3+" t4: "+t4);
 
		if (this.getCurrentSlot() == 1 && slot == 1) {
			return 0;
		} else {
			return temp <= 0 ? 0 : temp+1;//+(SLOT_TIME_IN_MS/2);
		}
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
	 * @param correctionInMS
	 *            the correctionInMS to set
	 */
	public void setCorrectionInMS(long correctionInMS) {
		this.correctionInMS = correctionInMS;
	}

}
