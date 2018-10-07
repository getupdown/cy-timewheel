package cn.cy.timewheel.core;

public class EventDescriptor {
	private ScheduledEvent event;

	private long msLater;

	public EventDescriptor(ScheduledEvent event, long msLater) {
		this.event = event;
		this.msLater = msLater;
	}

	public ScheduledEvent getEvent() {
		return event;
	}

	public long getMsLater() {
		return msLater;
	}
}
