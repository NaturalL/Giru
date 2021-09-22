package com.biasee.giru.event.core.tools.metrics;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


public class RealTimeMetricRegistry {

    private final static ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(2);

    public synchronized RealTimeMeter meter(int... seconds) {
        RealTimeMeter realTimeMeter = new RealTimeMeter();
        for (int second : seconds) {
            if (second <= 0) {
                throw new InvalidParameterException("统计间隔必须大于等于1秒");
            }
            if (realTimeMeter.itemMap.containsKey(second)) {
                continue;
            }
            MeterItem meterItem = new MeterItem();

            realTimeMeter.itemMap.put(second, meterItem);

            UpdateTask updateTask = new UpdateTask(realTimeMeter, meterItem);
            ScheduledFuture<?> scheduledFuture = scheduler
                    .scheduleAtFixedRate(updateTask, 1, second, TimeUnit.SECONDS);
            updateTask.setScheduledFuture(scheduledFuture);
        }
        return realTimeMeter;
    }

    class UpdateTask implements Runnable {
        WeakReference<RealTimeMeter> weakReference;
        final MeterItem meterItem;
        private volatile ScheduledFuture<?> scheduledFuture;

        public UpdateTask(RealTimeMeter realTimeMeter, MeterItem meterItem) {
            this.weakReference = new WeakReference(realTimeMeter);
            this.meterItem = meterItem;
        }

        public void setScheduledFuture(ScheduledFuture<?> scheduledFuture) {
            this.scheduledFuture = scheduledFuture;
        }

        @Override
        public void run() {
            try {
                RealTimeMeter realTimeMeter = weakReference.get();
                if (realTimeMeter == null) {
                    scheduledFuture.cancel(false);
                } else {
                    realTimeMeter.recalculate(meterItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
