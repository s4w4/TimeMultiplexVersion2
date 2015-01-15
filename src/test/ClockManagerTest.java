package test;

import static org.junit.Assert.*;

import org.junit.Test;

import station.ClockManager;
import station.Logger;
import station.MessageManager1;

public class ClockManagerTest {

	private ClockManager clockManager;

	public ClockManagerTest() {
		this.clockManager = new ClockManager(0);
	}
	
	@Test
	public void testCalcToNextFrameInMS() {
		long currentTime = System.currentTimeMillis();
		long currentFrame = currentTime / 1000;

		long resTime = clockManager.calcToNextFrameInMS();
		long resSum = currentTime + resTime;
		long resFrame = resSum / 1000;
		long resFrameStart = resSum % 1000;

		assertEquals(currentFrame, resFrame - 1);
		assertEquals(0, resFrameStart);
	}

	@Test
	public void testIsEOF() {
		try {
			MessageManager1 mm = new MessageManager1(new Logger("test"), clockManager);
			long resTime = clockManager.calcToNextFrameInMS(); 
			long frameOld = clockManager.getFrame(System.currentTimeMillis());
			Thread.sleep(resTime);
			long frameNew = clockManager.getFrame(System.currentTimeMillis());
			mm.syncMessagesReceivedTime(); 
			assertTrue(clockManager.isEOF());
			assertEquals(frameNew, frameOld+1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	
}
