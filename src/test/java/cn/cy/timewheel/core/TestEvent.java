/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package cn.cy.timewheel.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        logger.debug("task completed! start from:{}, end at: {} ", startTime.format(DateTimeFormatter.ISO_TIME),
                endTime.format(DateTimeFormatter.ISO_TIME));
    }
}
