package station;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
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
	private byte[] messageInByteArray = new byte[BYTE_LENGTH];;
	
	private byte sendingSlot;
	private boolean kollision = false;
	
	public Message(char stationClass) {
		super();
		setStationClass(stationClass);
	}
	
	
	public Message(byte[] messageInByteArray){
		this.messageInByteArray = messageInByteArray;

		stationClass = (char) this.messageInByteArray[STATION_CLASS.from()]; 
		
		data = new char[DATA.length()];	
		for(int i=DATA.from(); i<DATA.length(); i++){
			data[i] = (char) messageInByteArray[i];	
		}
					
		reservedSlot = (byte) this.messageInByteArray[RESERVED_SLOT.from()]; 
					
		sendTime = ByteBuffer.wrap(this.messageInByteArray, SEND_TIME.from(), SEND_TIME.length()).asLongBuffer().get(); 
		
	}
	
	
	/**
	 * @return the sendingSlot
	 */
	public byte getSendingSlot() {
		return sendingSlot;
	}


	/**
	 * @param sendingSlot the sendingSlot to set
	 */
	public void setSendingSlot(byte sendingSlot) {
		this.sendingSlot = sendingSlot;
	}


	/**
	 * @return the kollision
	 */
	public boolean isKollision() {
		return kollision;
	}


	/**
	 * @param kollision the kollision to set
	 */
	public void setKollision(boolean kollision) {
		this.kollision = kollision;
	}


	public byte[] toByteArray(){
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
		  for (int i = 0; i < DATA.length(); i++){
	        	messageInByteArray[DATA.from()+i] = (byte) data[i];
//	        	System.out.print(messageInByteArray[i]);
		  }
//		  System.out.println();
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
        ByteBuffer.wrap(messageInByteArray, SEND_TIME.from(), SEND_TIME.length()).putLong(sendTime);
		this.sendTime = sendTime;
	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Message [\nstationClass=" + stationClass + ", \ndata="
				+ Arrays.toString(data) + ", \nreservedSlot=" + reservedSlot
				+ ", \nsendTime=" + sendTime + ", \nsendingSlot=" + sendingSlot
				+ ", \nkollision=" + kollision + "]";
	}
	

	
}
