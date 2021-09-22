package com.biasee.giru.event.core.tools.metrics;

import java.util.concurrent.atomic.AtomicLong;


public class MeterItem {
    final AtomicLong lastCount = new AtomicLong(0);
    volatile long updateTime;
    volatile double eventPerSecond;

    public long getLastCount() {
        return lastCount.get();
    }

    public long getUpdateTime() {
        return updateTime;
    }

    //秒速
    public double getSpeed() {
        return eventPerSecond;
    }
}
