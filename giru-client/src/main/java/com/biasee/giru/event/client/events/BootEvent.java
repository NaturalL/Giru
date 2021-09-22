package com.biasee.giru.event.client.events;


public class BootEvent extends Event {

    public BootEvent() {
        this(null);
    }

    public BootEvent(String name) {
        super(name, EventType.BOOT.type);
    }
}