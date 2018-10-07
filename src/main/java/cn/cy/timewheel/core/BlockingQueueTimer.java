package cn.cy.timewheel.core;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import cn.cy.timewheel.exception.MinimumIntervalException;

/**
 * 使用一个空阻塞队列来模拟定时器
 * 允许支持的最小单位是100ms
 */
public class BlockingQueueTimer implements TickTimer {

	// 最小单位, 默认为100ms
	private static long MINIMUM_INTERVAL = 100;

	// 单位是ms
	private long interval;

	// 使用阻塞队列来模拟
	private BlockingQueue blockingQueue;

	public BlockingQueueTimer(long interval) {

		if (interval < MINIMUM_INTERVAL) {
			throw new MinimumIntervalException(MINIMUM_INTERVAL, interval);
		}

		this.interval = interval;
		blockingQueue = new LinkedBlockingQueue();
	}

	@Override
	public void once() {
		try {
			// 阻塞取空队列队首，依次来模拟一次计时
			blockingQueue.poll(interval, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public long getInterval() {
		return interval;
	}
}
