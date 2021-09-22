package com.biasee.giru.event.core.db.domain;

import com.biasee.giru.event.client.events.ErrorEvent;
import com.biasee.giru.event.core.util.EventUtils;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;


@Data
public class ErrorApp implements Cloneable {

    private String appId;
    private AtomicInteger errors = new AtomicInteger(0);
    private long lastDeployTime;

    private ConcurrentHashMap<String, ErrorStats> stats = new ConcurrentHashMap<>();

    private long updateTime;

    public void reset() {
        stats.clear();
        errors.set(0);
    }

    public void merge(ErrorApp errorApp) {
        errors.getAndAdd(errorApp.getErrors().get());
        if (updateTime == 0) {
            updateTime = System.currentTimeMillis();
        }
        if (errorApp.updateTime > 0) {
            updateTime = Math.min(updateTime, errorApp.updateTime);
        }
        for (Entry<String, ErrorStats> entry : errorApp.stats.entrySet()) {
            ErrorStats errorStats = getErrorStats(entry.getKey());
            if (errorStats == null) {
                stats.put(entry.getKey(), entry.getValue().copy());
            } else {
                errorStats.merge(entry.getValue());
            }
        }
    }

    public ErrorStats getErrorStats(String feature) {
        return stats.get(feature);
    }

    public synchronized int updateStats(ErrorEvent errorEvent) {
        updateTime = System.currentTimeMillis();
        errors.incrementAndGet();

        String feature = EventUtils.getExceptionFeature(errorEvent);
        ErrorStats errorStats = getErrorStats(feature);
        if (errorStats == null) {
            errorStats = new ErrorStats();
            errorStats.setAppId(errorEvent.getAppId());
            errorStats.setErrorFeature(feature);
            errorStats.setFeatureElements(EventUtils.getFeatureElements(errorEvent));
            stats.put(feature, errorStats);
        }
        errorStats.setErrorName(EventUtils.getException(errorEvent));
        errorStats.setLogger(errorEvent.label("logger"));
        errorStats.setErrorMsg(StringUtils.abbreviate(errorEvent.getError(), 2000));
        errorStats.setIp(errorEvent.getIp());
        errorStats.setStackTrace(errorEvent.takeStackTrace());
        errorStats.setLastFireTime(errorEvent.getTime());

        return errorStats.getTotal().incrementAndGet();
    }

    public ErrorApp copy() {
        ErrorApp c = null;
        try {
            c = (ErrorApp) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        c.setStats(new ConcurrentHashMap<>(this.stats));
        c.setErrors(new AtomicInteger(this.errors.get()));
        return c;
    }
}
