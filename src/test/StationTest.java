package test;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import station.ClockManager;
import station.Message;
import station.MessageManager;
import station.Station;

@SuppressWarnings("unused")
public class StationTest {

	private Station station; 

	public StationTest() {
		String interfaceName = "eth0";
		String mcastAddress = "225.10.1.2";
		int receivePort = 16000;
		char stationClass = 'A';
		long utcOffsetInMS = 0;
		this.station = new Station(interfaceName, mcastAddress, receivePort, stationClass, utcOffsetInMS);
		 
	}

	@Test
	public void testAblauf() throws InterruptedException, IOException {
		 
	}
	private Message createMessage(ClockManager clockManager, byte reserveredSlot) {

		char[] data = new char[24];
		for (int i = 0; i < data.length; i++) {
			data[i] = 'a';
		}

		Message message = new Message('A');
		message.setData(data);
		message.setReservedSlot(reserveredSlot);
		message.setSendTime(clockManager.getCorrectedTimeInMS());
		message.setReceivedTimeInMS(System.currentTimeMillis());
		message.setCurrentCorrection(clockManager.getCorrectionInMS());
		return message;
	}
}
