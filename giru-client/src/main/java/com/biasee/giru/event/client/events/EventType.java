package com.biasee.giru.event.client.events;

public enum EventType {

    ERROR("ERROR"),
    BOOT("BOOT"),
    ;

    public final String type;

    EventType(String type) {
        this.type = type;
    }
}