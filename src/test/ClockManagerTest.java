package test;

import static org.junit.Assert.*;

import org.junit.Test;

import station.ClockManager;
import station.Logger;
import station.Message;
import station.MessageManager;

public class ClockManagerTest {

	private ClockManager clockManager; 
	private Logger logger;

	public ClockManagerTest() {
		this.clockManager = new ClockManager(0);
		this.logger = new Logger("test"); 
	}

	/**
	 * test: getSlotCount
	 */
	@Test
	public void testGetSlotCount() {
		assertEquals("Anzahl Slots sind nicht 25", 25,
				clockManager.getSlotCount());
	}

	/**
	 * test: getCorrectedTimeInMS
	 */
	@Test
	public void testGetCorrectedTimeInMS() {
		long expected = System.currentTimeMillis();
		assertEquals(expected, clockManager.getCorrectedTimeInMS());
	}

	/**
	 * test: getCurrentFrame
	 */
	@Test
	public void testGetCurrentFrame() {
		long expected = System.currentTimeMillis() / 1000;
		assertEquals(expected, clockManager.getCurrentFrame());
	}

	@Test
	public void testSync() throws InterruptedException {

		ClockManager clockManagerOS50 = new ClockManager(50);
		MessageManager mm = new MessageManager(logger, clockManagerOS50);
		Thread.sleep(clockManagerOS50.calcToNextFrameInMS());
		mm.receivedMessage(createMessage((byte) (1), 'A'));
		Thread.sleep(40);
		mm.receivedMessage(createMessage((byte) (2), 'B'));
//		Thread.sleep(40);
//		mm.receivedMessage(createMessage((byte) (3), 'B'));
		Thread.sleep(clockManagerOS50.calcToNextFrameInMS());

		
		System.out.println(clockManagerOS50.getCorrectionInMS());
		mm.syncMessagesReceivedTime();
		System.out.println(clockManagerOS50.getCorrectionInMS());

		mm.getAllReceivedMessage().get(0).setCurrentCorrection(20);
		mm.getAllReceivedMessage().get(1).setCurrentCorrection(100);
//		mm.getAllReceivedMessage().get(2).setCurrentCorrection(0);

//		clockManagerOS50.setCorrectionInMS(20);
		System.out.println(clockManagerOS50.getCorrectionInMS());
		mm.syncMessagesReceivedTime();
		System.out.println(clockManagerOS50.getCorrectionInMS());

//		clockManagerOS50.sync(mm.getAllReceivedMessage());

	}

	private Message createMessage(byte reserveredSlot, char station) {

		char[] data = new char[24];
		for (int i = 0; i < data.length; i++) {
			data[i] = 'a';
		}

		Message message = new Message(station);
		message.setData(data);
		message.setReservedSlot(reserveredSlot);
		message.setSendTime(clockManager.getCorrectedTimeInMS());
		message.setReceivedTimeInMS(System.currentTimeMillis());
		message.setCurrentCorrection(clockManager.getCorrectionInMS());
		return message;
	}
}
