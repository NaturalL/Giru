package com.biasee.giru.event.web.bootstrap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;


@Component
public class BootFailedListener implements ApplicationListener<ApplicationFailedEvent> {


    private final Logger logger = LoggerFactory.getLogger(BootFailedListener.class);

    @Override
    public void onApplicationEvent(ApplicationFailedEvent event) {

        logger.error("启动失败: " + event.getException().getMessage(), event.getException());

        System.exit(-1);
    }
}