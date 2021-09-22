package com.biasee.giru.event.core.tools;

import com.biasee.giru.event.core.tools.metrics.RealTimeMeter;
import com.biasee.giru.event.core.util.EventUtils;
import com.biasee.giru.event.core.util.DLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class EventMonitor {

    private final static DLog logger = DLog.getDLog("monitor", EventMonitor.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Scheduled(cron = "0/2 * * ? * *")
    public void scheduleTaskUsingCronExpression() {
        RealTimeMeter meter = EventUtils.eventMeter;

        logger.info("秒速度:{} 已处理:{} 最近:{}",
                rightPad(meter.getSpeed(1), 5),
                rightPad(meter.getCount(), 10), meter.getIntervelCount(60));
    }

    private String rightPad(Object value, int size) {
        return StringUtils.rightPad(String.valueOf(value), size);
    }

}
