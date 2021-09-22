package com.biasee.giru.event.web.controller;

import com.biasee.giru.event.core.service.EventProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EventClientController {
    @Autowired
    private EventProcessService eventProcessService;

    @PostMapping(value = "/event/report/{type}", consumes = MediaType.TEXT_PLAIN_VALUE)
    public void report(@PathVariable String type, @RequestBody String event) {
        eventProcessService.process(event, type);
    }

}
