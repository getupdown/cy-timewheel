package cn.cy.timewheel.core;

import org.junit.Before;
import org.junit.Test;

public class BlockingQueueTimerTest {

	private BlockingQueueTimer blockingQueueTimer;

	@Before
	public void init() {
		blockingQueueTimer = new BlockingQueueTimer(100);
	}

	@Test
	public void testNormal() {
		long start = System.currentTimeMillis();
		for (int i = 0; i < 200; i++) {
			System.out.println("time now is " + System.currentTimeMillis() / 1000);
			blockingQueueTimer.once();
		}
		long end = System.currentTimeMillis();

		System.out.println((end - start) / 1000);
	}
}