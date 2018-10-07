package cn.cy.timewheel.core;

import java.time.LocalDateTime;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

public class TestEvent implements ScheduledEvent {

	private LocalDateTime startTime;

	private LocalDateTime endTime;

	private static Logger logger = LoggerContext.getContext().getLogger("TestEvent");

	@Override
	public void startTimingCallback() {
		startTime = LocalDateTime.now();
	}

	@Override
	public void timeoutCallback() {
		endTime = LocalDateTime.now();
		//		logger.debug("task completed! start from:{}, end at: {} ", startTime.toString(),
		//				endTime.toString());
		System.out.println("task completed! start from " + startTime.toString() + ", end at " + endTime.toString());
	}
}
