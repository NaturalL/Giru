package com.biasee.giru.event.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class EventConfig {
    @Autowired
    private Profile profile;

    @Autowired
    private EventCoreConfig coreConfig;

    public EventCoreConfig core() {
        return coreConfig;
    }


}
