package cn.cy.timewheel.core;

/**
 * 超时接口, 所有使用{@link TimeWheel}的事件都需要实现这个接口
 */
public interface ScheduledEvent {

	default void startTimingCallback() {
	}

	void timeoutCallback();
}
