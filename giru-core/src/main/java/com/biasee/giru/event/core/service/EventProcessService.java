package com.biasee.giru.event.core.service;

import com.biasee.giru.event.client.events.BootEvent;
import com.biasee.giru.event.client.events.ErrorEvent;
import com.biasee.giru.event.client.events.EventType;
import com.biasee.giru.event.core.db.DataBase;
import com.biasee.giru.event.core.db.domain.ErrorApp;
import com.biasee.giru.event.core.util.EventUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class EventProcessService {

    private static final Logger logger = LoggerFactory.getLogger("event");


    @Autowired
    private DataBase dataBase;
    @Autowired
    private AnalyzeService analyzeService;


    public void process(String event, String type) {
        EventUtils.eventMeter.mark();
        aliyunLog(event);

        if (EventType.ERROR.type.equals(type)) {
            ErrorEvent errorEvent = EventUtils.fromJson(event, ErrorEvent.class);

            ErrorApp errorApp = dataBase.getErrorApp(errorEvent.getAppId());
            errorApp.updateStats(errorEvent);
            dataBase.updateErrorAppTime();
            analyzeService.analyze(errorEvent, errorApp);

        } else if (EventType.BOOT.type.equals(type)) {
            BootEvent bootEvent = EventUtils.fromJson(event, BootEvent.class);
            ErrorApp errorApp = dataBase.getErrorApp(bootEvent.getAppId());
            errorApp.setLastDeployTime(bootEvent.getTime());
            dataBase.updateErrorAppTime();
        }
    }

    private void aliyunLog(String event) {
        logger.info(event);
    }

}
