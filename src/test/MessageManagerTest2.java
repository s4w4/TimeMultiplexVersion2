package test;

import static org.junit.Assert.*;

import org.junit.Test;

import station.ClockManager;
import station.Logger;
import station.Message;
import station.MessageManager;

public class MessageManagerTest2 {

	private ClockManager clockManager;
	private MessageManager messageManager;

	public MessageManagerTest2() {
		this.clockManager = new ClockManager(0);
		Logger logger = new Logger("test");
		this.messageManager = new MessageManager(logger, clockManager);
	}

	@Test
	public void testReceivedMessageOwnMessageTrue() {
		byte reserveredSlot = (byte) (1);
		Message m1 = createMessage(reserveredSlot);
		messageManager.setOwnMessage(m1);
		messageManager.receivedMessage(m1);
		Message m2 = messageManager.getOwnMessage();
		assertEquals(m1, m2);
	}

	@Test
	public void testReceivedMessageOwnMessageFalse() {
		byte reserveredSlot = (byte) (1);
		Message m1 = createMessage(reserveredSlot);
		messageManager.receivedMessage(m1);
		Message m2 = messageManager.getOwnMessage();
		assertNotEquals(m1, m2);
	}

	@Test
	public void testReceivedMessageAllReceivedMessagesNotEmpty() {
		byte reserveredSlot = (byte) (1);
		Message m1 = createMessage(reserveredSlot);
		messageManager.receivedMessage(m1);
		assertEquals(m1, messageManager.getAllReceivedMessage().get(0));
	}

	@Test
	public void testReceivedMessageAllReceivedMessagesEmpty() {
		assertEquals(0, messageManager.getAllReceivedMessage().size());
	}

	/**
	 * Keine Kollision alle Slots werden reserviert
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testSyncMessagesReceivedTimeHandleKollisionFalse()
			throws InterruptedException {
		for (int i = 0; i < 25; i++) {
			messageManager.receivedMessage(createMessage((byte) (i + 1)));
			Thread.sleep(40);
		}
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.syncMessagesReceivedTime();

		for (int i = 0; i < messageManager.getAllReceivedMessage().size(); i++) {
			assertEquals(false, messageManager.getAllReceivedMessage().get(i)
					.isKollision());
		}
	}

	/**
	 * 2 Packete in Slot 1 empfangen
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testSyncMessagesReceivedTimeHandleKollisionTrue1()
			throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.receivedMessage(createMessage((byte) (1)));
		messageManager.receivedMessage(createMessage((byte) (2)));
		for (int i = 2; i < 25; i++) {
			Thread.sleep(40);
			messageManager.receivedMessage(createMessage((byte) (i + 1)));
		}
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.syncMessagesReceivedTime();

		Message m1 = messageManager.getAllReceivedMessage().get(0);
		Message m2 = messageManager.getAllReceivedMessage().get(1); 

		// System.out.println("1: "+clockManager.getCorrectedSendingSlot(m1)+" Kollision= "+m1.isKollision());
		// System.out.println("2: "+clockManager.getCorrectedSendingSlot(m2)+" Kollision= "+m2.isKollision());
		// System.out.println("3: "+clockManager.getCorrectedSendingSlot(m3)+" Kollision= "+m3.isKollision());

		assertEquals(true, m1.isKollision());
		assertEquals(true, m2.isKollision());
		for (int i = 2; i < messageManager.getAllReceivedMessage().size(); i++) {
			assertEquals(false, messageManager.getAllReceivedMessage().get(i)
					.isKollision());
		}
	}

	/**
	 * 3 Pakete in Slot 1 empfangen
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testSyncMessagesReceivedTimeHandleTrue2()
			throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.receivedMessage(createMessage((byte) (1)));
		messageManager.receivedMessage(createMessage((byte) (2)));
		messageManager.receivedMessage(createMessage((byte) (3)));
		for (int i = 3; i < 25; i++) {
			Thread.sleep(40);
			messageManager.receivedMessage(createMessage((byte) (i + 1)));
		}
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.syncMessagesReceivedTime();
		assertEquals(true, messageManager.getAllReceivedMessage().get(0)
				.isKollision());
		assertEquals(true, messageManager.getAllReceivedMessage().get(1)
				.isKollision());
		assertEquals(true, messageManager.getAllReceivedMessage().get(2)
				.isKollision());
		for (int i = 3; i < messageManager.getAllReceivedMessage().size(); i++) {
			assertEquals(false, messageManager.getAllReceivedMessage().get(i)
					.isKollision());
		}
	}

	/**
	 * Zuerst in Slot 1 Paket 1 und 2 empfangen und in Slot 2 Paket 3 empfangen
	 * Nach zeitverschiebung in Slot 1 Paket 1 empfangen und in Slot 2 Paket 2
	 * und 3 empfangen
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testSyncMessagesReceivedTimeHandleKollisionVerrueckt()
			throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(20);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(20);
		messageManager.receivedMessage(createMessage((byte) (3)));
		Thread.sleep(this.clockManager.calcToNextFrameInMS());

		clockManager.setCorrectionInMS(20);
		messageManager.syncMessagesReceivedTime();

		Message m1 = messageManager.getAllReceivedMessage().get(0);
		Message m2 = messageManager.getAllReceivedMessage().get(1);
		Message m3 = messageManager.getAllReceivedMessage().get(2);

		// System.out.println("1: "+clockManager.getCorrectedSendingSlot(m1)+" Kollision= "+m1.isKollision());
		// System.out.println("2: "+clockManager.getCorrectedSendingSlot(m2)+" Kollision= "+m2.isKollision());
		// System.out.println("3: "+clockManager.getCorrectedSendingSlot(m3)+" Kollision= "+m3.isKollision());

		assertEquals(false, m1.isKollision());
		assertEquals(true, m2.isKollision());
		assertEquals(true, m3.isKollision());
	}

	/**
	 * in Slot 1 Paket 1 und 2 empfangen und in Slot 2 Paket 3 empfangen
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testSyncMessagesReceivedTimeHandleKollisionNichtVerrueckt()
			throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(20);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(20);
		messageManager.receivedMessage(createMessage((byte) (3)));
		Thread.sleep(this.clockManager.calcToNextFrameInMS());

		messageManager.syncMessagesReceivedTime();

		Message m1 = messageManager.getAllReceivedMessage().get(0);
		Message m2 = messageManager.getAllReceivedMessage().get(1);
		Message m3 = messageManager.getAllReceivedMessage().get(2);

		// System.out.println("1: "+clockManager.getCorrectedSendingSlot(m1)+" Kollision= "+m1.isKollision());
		// System.out.println("2: "+clockManager.getCorrectedSendingSlot(m2)+" Kollision= "+m2.isKollision());
		// System.out.println("3: "+clockManager.getCorrectedSendingSlot(m3)+" Kollision= "+m3.isKollision());

		assertEquals(true, m1.isKollision());
		assertEquals(true, m2.isKollision());
		assertEquals(false, m3.isKollision());
	}

	/**
	 * alte Pakete um 2 slots verschieben
	 * 
	 * @throws InterruptedException
	 */
	@Test
	public void testSyncMessagesReceivedTimeHandleKollisionSetCorrection1()
			throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		Message mOld1 = createMessage((byte) (1));
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		Message mOld2 = createMessage((byte) (2));
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(40);
		Message mOld3 = createMessage((byte) (3));
		messageManager.receivedMessage(createMessage((byte) (3)));

		clockManager.setCorrectionInMS(80);
		messageManager.syncMessagesReceivedTime();

		Message m1 = messageManager.getAllReceivedMessage().get(0);
		Message m2 = messageManager.getAllReceivedMessage().get(1);
		Message m3 = messageManager.getAllReceivedMessage().get(2);

		// System.out.println("1: ("+clockManager.getCorrectedSendingSlot(mOld1)+" | "+clockManager.getCorrectedSendingSlot(m1)+")");
		// System.out.println("2: ("+clockManager.getCorrectedSendingSlot(mOld2)+" | "+clockManager.getCorrectedSendingSlot(m2)+")");
		// System.out.println("3: ("+clockManager.getCorrectedSendingSlot(mOld3)+" | "+clockManager.getCorrectedSendingSlot(m3)+")");

		assertEquals(clockManager.getCurrentSendingSlot(mOld1) + 2,
				clockManager.getCurrentSendingSlot(m1));
		assertEquals(clockManager.getCurrentSendingSlot(mOld2) + 2,
				clockManager.getCurrentSendingSlot(m2));
		assertEquals(clockManager.getCurrentSendingSlot(mOld3) + 2,
				clockManager.getCurrentSendingSlot(m3));
	}

	@Test
	public void testSyncMessagesReceivedTimeHandleSetCorrectionOldFrame_Slot_25_1()
			throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS() - 40);
		// System.out.println(System.currentTimeMillis());
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(2);
		messageManager.syncMessagesReceivedTime();
		Message m1 = messageManager.getAllReceivedMessage().get(0);
		Message m2 = messageManager.getAllReceivedMessage().get(1);
		// System.out.println("SLOT = "+clockManager.getCorrectedSendingSlot(m1)+" ; "+m1.isOldFrame());
		// System.out.println("SLOT = "+clockManager.getCorrectedSendingSlot(m2)+" ; "+m2.isOldFrame());

		assertTrue(m1.isOldFrame());
		assertFalse(m2.isOldFrame());
	}

	@Test
	public void testSyncMessagesReceivedTimeHandleSetCorrectionOldFrame_Slot_24_25()
			throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS() - 80);
		// System.out.println(System.currentTimeMillis());
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(40);
		messageManager.syncMessagesReceivedTime();
		Message m1 = messageManager.getAllReceivedMessage().get(0);
		Message m2 = messageManager.getAllReceivedMessage().get(1);
		// System.out.println("SLOT = "+clockManager.getCorrectedSendingSlot(m1)+" ; "+m1.isOldFrame());
		// System.out.println("SLOT = "+clockManager.getCorrectedSendingSlot(m2)+" ; "+m2.isOldFrame());

		assertTrue(m1.isOldFrame());
		assertTrue(m2.isOldFrame());
	}

	@Test
	public void testSyncMessagesReceivedTimeHandleSetCorrectionOldFrame_Slot_1_2()
			throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		// System.out.println(System.currentTimeMillis());
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(2);
		messageManager.syncMessagesReceivedTime();
		Message m1 = messageManager.getAllReceivedMessage().get(0);
		Message m2 = messageManager.getAllReceivedMessage().get(1);
		// System.out.println("SLOT = "+clockManager.getCorrectedSendingSlot(m1)+" ; "+m1.isOldFrame());
		// System.out.println("SLOT = "+clockManager.getCorrectedSendingSlot(m2)+" ; "+m2.isOldFrame());

		assertFalse(m1.isOldFrame());
		assertFalse(m2.isOldFrame());
	}

	@Test
	public void testresetFrame_Slot25_1() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS() - 40);
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(2);
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();
		// for (Message m : messageManager.getAllReceivedMessage()) {
		// System.out.println(clockManager.getCorrectedSendingSlot(m)+" "+m.isOldFrame());
		// }
		assertEquals(1, messageManager.getAllReceivedMessage().size());
	}

	@Test
	public void testresetFrame_Slot24_25() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS() - 80);
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(40);
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();
		// System.out.println("SIZE = 0 ? ::"+messageManager.getAllReceivedMessage().size());

		assertEquals(0, messageManager.getAllReceivedMessage().size());
	}

	@Test
	public void testresetFrame_Slot1_2() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(2);
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();
		// for (Message m : messageManager.getAllReceivedMessage()) {
		// System.out.println(clockManager.getCorrectedSendingSlot(m)+" "+m.isOldFrame());
		// }
		assertEquals(2, messageManager.getAllReceivedMessage().size());
	}

	@Test
	public void testIsOwnKollision() throws InterruptedException {

		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.receivedMessage(createMessage((byte) (1)));
		messageManager.receivedMessage(createMessage((byte) (2)));

		messageManager.syncMessagesReceivedTime();

		Message m1 = messageManager.getAllReceivedMessage().get(0);
//		Message m2 = messageManager.getAllReceivedMessage().get(1);
		m1.setOwnMessage(true);

		assertEquals(true, messageManager.isOwnKollision());
	}

	@Test
	public void testIsOwnMessageSended() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		Message ownMessage = createMessage((byte) (1));
		messageManager.setOwnMessage(ownMessage);
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		messageManager.receivedMessage(createMessage((byte) (2)));

//		Message m1 = messageManager.getAllReceivedMessage().get(0);
//		Message m2 = messageManager.getAllReceivedMessage().get(1);

//		System.out.println(messageManager.isOwnMessageSended());
		assertTrue(messageManager.isOwnMessageSended());
	}

	@Test
	public void testIsFreeSlotNextFrame1() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());

		messageManager.receivedMessage(createMessage((byte) (1)));

		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();

		// System.out.println(Arrays.toString(messageManager.getFreeSlots()
		// .toArray()));
		Byte[] expandArr1 = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
				16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };
		assertArrayEquals(expandArr1, messageManager.getFreeSlots().toArray());
	}

	@Test
	public void testIsFreeSlotNextFrame2() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		for (int i = 0; i < 25; i++) {
			messageManager.receivedMessage(createMessage((byte) (i + 1)));
			Thread.sleep(40);
		}
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();

		// System.out.println(Arrays.toString(messageManager.getFreeSlots()
		// .toArray()));

		assertFalse(messageManager.isFreeSlotNextFrame());
	}

	@Test
	public void testIsFreeSlotNextFrame3() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS() - 40);
		messageManager.receivedMessage(createMessage((byte) (1)));
		Thread.sleep(40);
		messageManager.receivedMessage(createMessage((byte) (2)));
		Thread.sleep(2);
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();

		// System.out.println(Arrays.toString(messageManager.getFreeSlots()
		// .toArray()));
		Byte[] expandArr1 = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
				16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };
		assertArrayEquals(expandArr1, messageManager.getFreeSlots().toArray());

		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();
		// System.out.println(Arrays.toString(messageManager.getFreeSlots()
		// .toArray()));
		Byte[] expandArr2 = { 1, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
				16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };
		assertArrayEquals(expandArr2, messageManager.getFreeSlots().toArray());

	}

	@Test
	public void testCalcNewSlot1() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		for (int i = 0; i < 24; i++) {
			messageManager.receivedMessage(createMessage((byte) (i + 1)));
			Thread.sleep(40);
		}
		Thread.sleep(40);
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();

		// System.out.println(Arrays.toString(messageManager.getFreeSlots()
		// .toArray()));
		// System.out.println(messageManager.calcNewSlot());
		// Byte[] expandArr1 = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
		// 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };

		assertEquals(25, messageManager.calcNewSlot());

	}

	@Test
	public void testGetFreeSlot1() throws InterruptedException {
		Thread.sleep(this.clockManager.calcToNextFrameInMS());
		for (int i = 0; i < 24; i++) {
			messageManager.receivedMessage(createMessage((byte) (i + 1)));
			Thread.sleep(40);
		}
		Thread.sleep(40);
		messageManager.syncMessagesReceivedTime();
		messageManager.resetFrame();

		// System.out.println(Arrays.toString(messageManager.getFreeSlots()
		// .toArray()));
		// System.out.println(messageManager.calcNewSlot());
		// Byte[] expandArr1 = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15,
		// 16, 17, 18, 19, 20, 21, 22, 23, 24, 25 };

		assertEquals(25, messageManager.getFreeSlot());
	}

	private Message createMessage(byte reserveredSlot) {

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
