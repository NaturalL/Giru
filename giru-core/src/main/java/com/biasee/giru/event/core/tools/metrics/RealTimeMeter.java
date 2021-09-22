package com.biasee.giru.event.core.tools.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * com.codahale.metrics 里的是滑动平均,这里是直接平均
 *
 * @author zhengjie  2019/3/6.
 */
public class RealTimeMeter {
    static final MeterItem INVALID = new MeterItem();

    Map<Integer, MeterItem> itemMap = new HashMap<>();
    final AtomicLong currentCount = new AtomicLong(0);

    /**
     * 新的事件发生时调用
     */
    public void mark() {
        currentCount.incrementAndGet();
    }

    public void mark(long amount) {
        currentCount.addAndGet(amount);
    }

    //重新计算速度
    void recalculate(MeterItem meterItem) {
        long periodCount = currentCount.get() - meterItem.lastCount.getAndSet(currentCount.get());
        long now = System.currentTimeMillis();
        if (periodCount > 0) {
            meterItem.eventPerSecond = periodCount * 1000 / (now - meterItem.updateTime);
        } else {
            meterItem.eventPerSecond = 0;
        }
        meterItem.updateTime = now;
    }

    public long getCount() {
        return currentCount.get();
    }

    public double getSpeed(int seconds) {
        return getMeterItem(seconds).getSpeed();
    }

    public long getIntervelCount(int seconds) {
        return getCount() - getLastCount(seconds);
    }

    public long getLastCount(int seconds) {
        return getMeterItem(seconds).getLastCount();
    }


    public MeterItem getMeterItem(int seconds) {
        MeterItem meterItem = itemMap.get(seconds);
        return meterItem != null ? meterItem : INVALID;
    }

}


