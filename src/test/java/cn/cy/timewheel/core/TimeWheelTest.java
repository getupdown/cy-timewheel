/*
 * Copyright (C) 2018 Baidu, Inc. All Rights Reserved.
 */
package cn.cy.timewheel.core;

import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

public class TimeWheelTest {

    private int threadNum = 10;

    private TimeWheel timeWheel;

    @Before
    public void bd() {
        timeWheel = TimeWheel.build();
    }

    @Test
    public void testEmpty() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        timeWheel.start();

        countDownLatch.await();
    }


    @Test
    public void testSingleTask() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        timeWheel.start();

        timeWheel.addEvent(new TestEvent(), 1500);

        countDownLatch.await();
    }

    public void simulateTasks1() {

    }

    /**
     * 这里测试一下, 在极限情况下, 多少任务的时候, 才会出现无法加入到同一个slot中的情况
     */
    @Test
    public void testSingleLimit() throws InterruptedException {
        // 单位时间内的并发任务量
        int missionNum = 1000;

        CountDownLatch countDownLatch = new CountDownLatch(missionNum);

        timeWheel.start();

        for (int i = 0; i < missionNum; i ++) {
            new Thread(() -> {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                    for (int x = 0; x < 10; x ++) {
                        timeWheel.addEvent(new TestEvent(), 100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        CountDownLatch countDownLatch1 = new CountDownLatch(1);
        countDownLatch1.await();
    }
}