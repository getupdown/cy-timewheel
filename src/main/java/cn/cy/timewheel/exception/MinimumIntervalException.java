/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package cn.cy.timewheel.exception;

public class MinimumIntervalException extends RuntimeException {

    public MinimumIntervalException(long should, long real) {
        super("BlockingQueueTimer Minimum Interval should not less than " + should
                + ", now is " + real);
    }
}
