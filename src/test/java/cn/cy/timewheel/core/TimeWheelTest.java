package cn.cy.timewheel.core;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

import org.junit.Before;
import org.junit.Test;

public class TimeWheelTest {

    private int threadNum = 10;

    private volatile TimeWheel timeWheel;

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

    /**
     * 模拟突然有很多请求进来的情况
     */
    @Test
    public void testSingleHighParallel() throws InterruptedException {
        // 单位时间内的并发任务量
        int missionNum = 1000;

        CountDownLatch countDownLatch = new CountDownLatch(missionNum);

        timeWheel.start();

        for (int i = 0; i < missionNum; i++) {
            new Thread(() -> {
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                    for (int x = 0; x < 100; x++) {
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

    /**
     * 模拟长期的 平稳的请求
     */
    @Test
    public void testNormalUse() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        timeWheel.start();

        // 1000个线程平稳工作
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                Random random = new Random();

                while (true) {
                    int x = Math.abs(random.nextInt()) % 100;

                    if (x < 30) {
                        timeWheel.addEvent(new TestEvent(), 1000);
                    }

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        countDownLatch.await();
    }

    /**
     * 模拟长期的请求, 并且会跨过一整轮时间轮
     */
    @Test
    public void testOverflowNormalUse() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        timeWheel.start();

        // 1000个线程平稳工作
        for (int i = 0; i < 1000; i++) {
            new Thread(() -> {
                Random random = new Random();

                while (true) {
                    int x = Math.abs(random.nextInt()) % 100;

                    if (x < 30) {
                        timeWheel.addEvent(new TestEvent(), 5000);
                    }

                    try {
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }

        countDownLatch.await();

        countDownLatch.await();
    }
}