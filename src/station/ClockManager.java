package station;

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
	 * Summe aller Korrekturwerte im aktuellen Frame
	 */
	private long sumCorrectionInMS = 0;

	/**
	 * Korrekturwert des letzten Slots
	 */
	private long correctionLastSlotInMS = 0;

	/**
	 * Anzahl aller A Station im aktuellen Frame
	 */
	private int countStations = 0;

	/**
	 * Korrekturwert des aktuellen Frames
	 */
	private long correctionLastFrameInMS = 0;

	private boolean flagClockBack = false;
	private boolean flagNewPackage = false;

	public ClockManager(long utcOffsetInMS) {
		this.correctionInMS = utcOffsetInMS;

	}

	/**
	 * 
	 * @return
	 */
	public boolean isEOF() {
		System.out.println("\t" + correctionLastFrameInMS);
		return correctionLastFrameInMS >= 0;
	}

	/**
	 * liefert aktuellen Slot
	 * 
	 * Slot beginnt bei 1
	 */
	public byte getCurrentSlot() {
		return (byte) (((getCorrectedTimeInMS() % 1000) / SLOT_TIME_IN_MS) + 1);
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
	 * Berechnet neue Korrektur
	 * 
	 * @param message
	 */
	public void calcCorrection(Message message) {
		if (message.getStationClass() == CLASS_A) {
			this.correctionLastSlotInMS = message.getSendTime()
					- getCorrectedTimeInMS();
			this.sumCorrectionInMS += correctionLastSlotInMS;
			this.countStations++;
			if (flagClockBack) {
				flagNewPackage = true;
			}
		}

	}

	/**
	 * Bei Kollision wird die letzte Korrektur zurückgesetzt
	 * 
	 * @param message
	 */
	public void rollbackLastCorrection(Message message) {
		if (message.getStationClass() == CLASS_A) {
			this.countStations--;
			this.sumCorrectionInMS -= correctionLastSlotInMS;
			this.correctionLastSlotInMS = 0;
			if (flagClockBack) {
				flagNewPackage = false;
			}
		}
	}

	/**
	 * Berechnet die Zeit wie lange es noch bis zum ende des Frames dauert in
	 * Millisekunden
	 * 
	 * @return
	 */
	public long calcToNextFrameInMS() {
		return 1000 - (getCorrectedTimeInMS() % 1000);
	}

	/**
	 * Synchronisiert Korregierte Stationszeit
	 */
	public void sync() {
		if (flagClockBack && !flagNewPackage) {
			this.correctionLastFrameInMS = 0;
		} else {
			this.correctionLastFrameInMS = this.sumCorrectionInMS
					/ (this.countStations == 0 ? 1 : this.countStations);
			this.flagClockBack = correctionLastFrameInMS < 0;
			this.correctionInMS += correctionLastFrameInMS;
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
		this.sumCorrectionInMS = 0;
		this.correctionLastFrameInMS = 0;
		this.correctionLastSlotInMS = 0;
		this.flagClockBack = false;
		this.flagNewPackage = false;
	}

	public long calcTimeUntilSlotInMS(byte slot) {
		return ((slot - 1) * SLOT_TIME_IN_MS );// + SLOT_TIME_IN_MS/2;
	}

}
