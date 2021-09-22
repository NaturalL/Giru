package com.biasee.giru.event.core.util;

import java.util.concurrent.TimeUnit;


public class SimpleLimiter {

    //毫秒
    private long lastAcquireTimestamp = 0L;
    //毫秒范围
    private final long rangeMillis;

    private SimpleLimiter(long range) {
        this.rangeMillis = range;
    }

    public static SimpleLimiter atMostOnceEvery(long time, TimeUnit timeUnit) {
        if (time < 0) {
            throw new IllegalArgumentException("范围不能小于 0");
        }
        SimpleLimiter simpleLimiter = new SimpleLimiter(timeUnit.toMillis(time));
        return simpleLimiter;
    }

    public synchronized boolean tryAcquire() {
        long now = System.currentTimeMillis();
        if (now - lastAcquireTimestamp >= rangeMillis) {
            lastAcquireTimestamp = now;
            return true;
        } else {
            return false;
        }
    }

}
