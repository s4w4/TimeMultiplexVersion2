package station;

import java.nio.ByteBuffer; 
import java.util.Arrays;

import static station.PackageOrder.*;

public class Message {

	private final int BYTE_LENGTH = 34;
	// Stationklass
	private char stationClass;
	// Nutzdaten
	private char[] data;
	// reservierte Slot im naechsten Frame
	private byte reservedSlot;
	// Zeitstemple, wann Packet abgeschickt wurde
	private long sendTime;

	private long receivedTimeInMS = 0;
	private long correctedTimeAtThisTime = 0;

	private byte[] messageInByteArray = new byte[BYTE_LENGTH];;

	// private byte sendingSlot;
	private boolean kollision = false;
	private boolean ownMessage = false;
	private boolean oldFrame = false;

	public Message(char stationClass) {
		super();
		setStationClass(stationClass);
	}

	public Message(byte[] messageInByteArray) {
		this.messageInByteArray = messageInByteArray;

		stationClass = (char) this.messageInByteArray[STATION_CLASS.from()];

		data = new char[DATA.length()];
		for (int i = DATA.from(); i < DATA.length(); i++) {
			data[i] = (char) messageInByteArray[i];
		}

		reservedSlot = (byte) this.messageInByteArray[RESERVED_SLOT.from()];

		sendTime = ByteBuffer
				.wrap(this.messageInByteArray, SEND_TIME.from(),
						SEND_TIME.length()).asLongBuffer().get();
	}

	/**
	 * @return the kollision
	 */
	public boolean isKollision() {
		return kollision;
	}

	/**
	 * @param kollision
	 *            the kollision to set
	 */
	public void setKollision(boolean kollision) {
		this.kollision = kollision;
	}

	public byte[] toByteArray() {
		return messageInByteArray;
	}

	public char getStationClass() {
		return stationClass;
	}

	public void setStationClass(char stationClass) {
		messageInByteArray[STATION_CLASS.from()] = (byte) stationClass;
		this.stationClass = stationClass;
	}

	public char[] getData() {
		return data;
	}

	public void setData(char[] data) {
		for (int i = 0; i < DATA.length(); i++) {
			messageInByteArray[DATA.from() + i] = (byte) data[i];
		}
		this.data = data;
	}

	public byte getReservedSlot() {
		return reservedSlot;
	}

	public void setReservedSlot(byte reservedSlot) {
		messageInByteArray[RESERVED_SLOT.from()] = reservedSlot;
		this.reservedSlot = reservedSlot;
	}

	public long getSendTime() {
		return sendTime;
	}

	public void setSendTime(long sendTime) {
		ByteBuffer.wrap(messageInByteArray, SEND_TIME.from(),
				SEND_TIME.length()).putLong(sendTime);
		this.sendTime = sendTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString() r = reserved slot s = sending slot
	 */
	@Override
	public String toString() {

		String dataString = "";
		for (int i = 0; i <= 10; i++)
			dataString += data[i] + "";
		return "Message [ '" + dataString + "', " + stationClass + "," + " r:"
				+ reservedSlot + ", s:"
				+ (byte) (((getReceivedTimeInMS() % 1000) / 40) + 1) + ", "
				+ "TX:" + sendTime + "]";
	}

	/**
	 * @return the receivedTimeInMS
	 */
	public long getReceivedTimeInMS() {
		return receivedTimeInMS + correctedTimeAtThisTime;
	}

	/**
	 * @param receivedTimeInMS
	 *            the receivedTimeInMS to set
	 */
	public void setReceivedTimeInMS(long receivedTimeInMS) {
		this.receivedTimeInMS = receivedTimeInMS;
	}

	public void setOwnMessage(boolean b) {
		this.ownMessage = b;
	}

	public boolean isOwnMessage() {
		return ownMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + reservedSlot;
		result = prime * result + (int) (sendTime ^ (sendTime >>> 32));
		result = prime * result + stationClass;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		if (reservedSlot != other.reservedSlot)
			return false;
		if (sendTime != other.sendTime)
			return false;
		if (stationClass != other.stationClass)
			return false;
		return true;
	}

	public boolean isOldFrame() {
		return oldFrame;
	}

	public void setOldFrame(boolean oldFrame) {
		this.oldFrame = oldFrame;
	}

	public long getCorrectedTimeAtThisTime() {
		return correctedTimeAtThisTime;
	}

	public void setCurrentCorrection(long correctedTimeAtThisTime) {
		this.correctedTimeAtThisTime = correctedTimeAtThisTime;
	}

}
