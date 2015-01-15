package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import station.ClockManager;
import station.Logger;
import station.Message;
import station.MessageManager1;

public class MessageManagerTest {

	private MessageManager1 messageManager;
	private ClockManager clockManager; 

	public MessageManagerTest() {
		this.clockManager = new ClockManager(0);
		this.messageManager = new MessageManager1(new Logger("test"),
				clockManager);
	}

	@Test
	public void testReceivedMessage() {
		List<Message> messages = new ArrayList<>();
		for (int i = 0; i < 1; i++) {
			long currentTime = System.currentTimeMillis();
			byte reserveredSlot = (byte) (i+1);
			Message message = createMessage(clockManager.getCorrectedTimeInMS(), currentTime, clockManager.getCorrectionInMS(), reserveredSlot);
			messageManager.receivedMessage(message);
//			messageManager.setOwnMessage(message);
//			messageManager.setReservedSlot(reserveredSlot);		
		}
		long freeSlot = messageManager.getFreeSlot();
		System.out.println(freeSlot+" ::: "+messageManager.getReservedSlot());
	}

	
	
	
	
	
	
	
	private Message createMessage(long sendTime, long receivedTime, long correction, byte reserveredSlot) {
		char[] data = new char[24];
		for (int i = 0; i < data.length; i++) {
			data[i] = 'a';
		}

		Message message = new Message('A');
		message.setData(data);
		message.setReservedSlot(reserveredSlot);
		message.setSendTime(sendTime);
		message.setStationTimeInMS(receivedTime);
		message.setCurrentCorrection(correction);
		return message;
	}

}
