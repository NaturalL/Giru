package com.biasee.giru.event.client.events;

import java.util.HashMap;
import java.util.Map;


public class Event {
    private String appId;
    private String ip;

    private String name;
    private String type;

    private Map<String, String> labels = new HashMap<>();
    private long time = System.currentTimeMillis();

    public Event() {
    }

    public Event(String name, String type) {
        this.name = name;
        this.type = type;
    }


    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getLabels() {
        return labels;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public void label(String key, Object value) {
        if (value != null) {
            labels.put(key, String.valueOf(value));
        } else {
            labels.put(key, null);
        }
    }

    public String label(String key) {
        return labels.get(key);
    }

}
