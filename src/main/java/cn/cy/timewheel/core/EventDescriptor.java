package cn.cy.timewheel.core;

public class EventDescriptor {

    /**
     * 等待定时的任务对象
     */
    private ScheduledEvent event;

    /**
     * 多少毫秒后执行
     */
    private long msLater;

    /**
     * 记录这个事件被加入公共队列的时间
     * 用于 {@link TimeWheel#addEvent(ScheduledEvent, long)}中的补偿机制
     */
    private long addedTime;

    public EventDescriptor(ScheduledEvent event, long msLater) {
        this.event = event;
        this.msLater = msLater;
        this.addedTime = System.currentTimeMillis();
    }

    public ScheduledEvent getEvent() {
        return event;
    }

    public long getMsLater() {
        return msLater;
    }

    public long getAddedTime() {
        return addedTime;
    }
}
