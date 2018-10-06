/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package cn.cy.timewheel.core;

/**
 * 超时接口, 所有使用{@link TimeWheel}的事件都需要实现这个接口
 */
public interface ScheduledEvent {

	default void startTimingCallback() {
	}

	void timeoutCallback();
}
