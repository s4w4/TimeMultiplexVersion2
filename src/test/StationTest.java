package test;

import static org.junit.Assert.*;

import org.junit.Test;

import station.Station;

@SuppressWarnings("unused")
public class StationTest {

	@Test
	public void testStart() {
		String interfaceName = "eth0";
		String mcastAddress = "225.10.1.2";
		int receivePort = 16000;
		char stationClass = 'A';
		long utcOffsetInMS = 0;
		new Station(interfaceName, mcastAddress, receivePort, stationClass,
				utcOffsetInMS);
		// station.start();
	}

	@Test
	public void testAblauf() {

	}

}
