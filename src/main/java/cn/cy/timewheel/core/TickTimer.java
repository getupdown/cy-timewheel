package cn.cy.timewheel.core;

/**
 * tickTimer接口
 * 定义时间轮上的一格时间的模拟
 */
public interface TickTimer {

	/**
	 * 进行一次计时
	 */
	void once();

	/**
	 * 获取一次计时的间隔
	 */
	long getInterval();
}
