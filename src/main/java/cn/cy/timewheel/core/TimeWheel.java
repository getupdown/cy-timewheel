package cn.cy.timewheel.core;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

/**
 * 时间轮核心逻辑
 * <p>
 * 这里并未使用分层时间轮，而是复用同一个时间轮
 * 在每个槽上有轮数来标识，现在是走到的第几圈
 * 支持的最小单位是秒
 */
public class TimeWheel {

    private TickTimer tickTimer;

    private static Logger logger = LoggerContext.getContext().getLogger(TimeWheel.class.getName());

    // 一圈的槽数
    private final int slotNum;

    private static final int DEFAULT_SLOT_NUM = 200;

    // 一个槽所代表的时间,单位是ms
    private final int milliSecondsPerSlot;

    private static final int DEFAULT_TIME_PER_SLOT = 100;

    // 现在走到的指针
    private volatile int point;

    // 轮数, 每走过一圈, 轮数自增
    private volatile long round;

    private Executor executor;

    private ArrayList<Slot<ScheduledEvent>> slotList;

    // 任务开始时间
    private LocalDateTime startTime;

    // 任务开始计数
    private long startCnt;

    // 任务收集队列
    private volatile ConcurrentLinkedQueue<EventDescriptor> collectQueue;

    // 单次任务收集的最大值
    private int SINGLE_ROUND_COLLECTION_MAXIMUM = 1000;

    private TimeWheel(int slotNum, int milliSecondsPerSlot) {
        this.slotNum = slotNum;
        this.milliSecondsPerSlot = milliSecondsPerSlot;
        tickTimer = new BlockingQueueTimer(milliSecondsPerSlot);
        startCnt = 0;

        slotList = new ArrayList<>();
        for (int i = 0; i < slotNum; i++) {
            slotList.add(Slot.buildEmptySlot(i));
        }

        executor = Executors.newFixedThreadPool(20);
        collectQueue = new ConcurrentLinkedQueue<>();
    }

    private void tick() {
        // 计时一次
        // logger.debug("once");
        tickTimer.once();

        logger.warn("timing ");
        Slot nowSlot = slotList.get(point);
        final long tarRound = round;
        final int nowPoint = point;

        executor.execute(() -> {
            nowSlot.pollEvent(tarRound);
        });

        point++;
        if (point >= slotNum) {
            point %= slotNum;
            // long都溢出了, 这程序得跑到人类灭亡把...
            round++;
        }
    }

    private void collect() {
        // 由于任务被加进公共队列到真正被取出来加入时间轮，这中间有误差
        // 在这里做一个补偿机制
        long baseMillis = System.currentTimeMillis();
        for (int i = 0; i < SINGLE_ROUND_COLLECTION_MAXIMUM; i++) {
            EventDescriptor descriptor = collectQueue.poll();
            if (descriptor == null) {
                break;
            }

            addEvent0(descriptor.getEvent(), descriptor.getMsLater(), descriptor.getAddedTime(), baseMillis);
        }
    }

    public void start() {

        startCnt++;
        startTime = LocalDateTime.now();

        // 计时线程
        new Thread(() -> {
            while (true) {
                // 收集和计时交替进行
                tick();
                collect();
            }
        }).start();
    }

    public static TimeWheel build() {
        return new TimeWheel(DEFAULT_SLOT_NUM, DEFAULT_TIME_PER_SLOT);
    }

    /**
     * 在millisLater毫秒之后进行任务
     *
     * @param event
     * @param millisLater
     */
    private void addEvent0(ScheduledEvent event, long millisLater, long missionStartMillis, long baseMillis) {

        long deltaSlotIndex = (millisLater - (baseMillis - missionStartMillis)) / milliSecondsPerSlot;

        if (deltaSlotIndex == 0) {
            deltaSlotIndex++;
        }

        int nextIndex = (point + (int) deltaSlotIndex);
        long tarRound = round;

        //        System.out.println(LocalDateTime.now() + " start is " + startTime.toString() + " now " + point + "
        // nextIndex "
        //                + nextIndex);

        if (nextIndex >= slotNum) {
            nextIndex -= slotNum;
            tarRound++;
        }
        Slot<ScheduledEvent> tarSlot = slotList.get(nextIndex);

        tarSlot.addEvent(tarRound, event);
    }

    /**
     * 对外暴露的添加时间方法
     * 把事件包装成 {@link EventDescriptor} 后, 加入队列, 等待消费
     *
     * @param event
     * @param millisLater
     */
    public void addEvent(ScheduledEvent event, long millisLater) {
        EventDescriptor eventDescriptor = new EventDescriptor(event, millisLater);
        collectQueue.add(eventDescriptor);
    }

    /**
     * 槽位的类定义
     * <p>
     * 不使用分层策略, 而是复用这一层.
     * 每个槽会维护一个 Map<round, List<Event>> 的数据结构
     */
    public static class Slot<Event extends ScheduledEvent> {

        // 现在这一槽位所处于的轮数
        private volatile int nowRound;

        // 这个slot所在的下标
        private final int index;

        // Map<round, List<Event>>
        private volatile HashMap<Long, List<Event>> eventMap;

        private Slot(int nowRound,
                     HashMap<Long, List<Event>> eventMap,
                     int index) {
            this.nowRound = nowRound;
            this.eventMap = eventMap;
            this.index = index;
        }

        @SuppressWarnings("unchecked")
        public static Slot buildEmptySlot(int index) {
            return new Slot(0, new HashMap<>(), index);
        }

        public void addEvent(long tarRound, Event event) {

            // 更新任务
            List<Event> eventList = eventMap.getOrDefault(tarRound, null);
            if (eventList == null) {
                eventList = new ArrayList<>();
                eventMap.put(tarRound, eventList);
            }

            eventList.add(event);

            event.startTimingCallback();
        }

        // 循环指定round的任务, 进行回调
        public void pollEvent(long tarRound) {
            List<Event> eventList = eventMap.getOrDefault(tarRound, null);

            if (eventList == null) {
                return;
            }

            for (Event event : eventList) {
                event.timeoutCallback();
            }

            // remove the element, help gc
            eventMap.remove(tarRound);
        }
    }
}
