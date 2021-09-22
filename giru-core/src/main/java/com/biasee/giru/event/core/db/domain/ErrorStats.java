package com.biasee.giru.event.core.db.domain;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.Data;


@Data
public class ErrorStats implements Cloneable {
    private String appId;
    private AtomicInteger total = new AtomicInteger();
    private String errorMsg;

    private String ip;

    private String stackTrace;

    private String errorFeature;
    private String errorName;

    private String logger;

    private Set<String> alertTags = new HashSet<>();

    private long lastFireTime;

    private String[] featureElements;

    public void merge(ErrorStats errorStats) {
        this.total.getAndAdd(errorStats.getTotal().get());
    }

    public synchronized boolean tryAlert(String tag) {
        return alertTags.add(tag);
    }

    public ErrorStats copy() {
        ErrorStats c = null;
        try {
            c = (ErrorStats) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        c.setAlertTags(new HashSet<>(alertTags));
        c.setTotal(new AtomicInteger(this.getTotal().get()));
        return c;
    }
}
