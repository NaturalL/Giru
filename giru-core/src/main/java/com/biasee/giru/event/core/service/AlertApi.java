package com.biasee.giru.event.core.service;

import com.biasee.giru.event.core.config.EventConfig;
import com.biasee.giru.event.core.util.DLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class AlertApi {
    private static final DLog alertLog = DLog.getDLog("alert", AlertApi.class);

    @Autowired
    EventConfig eventConfig;

    /**
     * @param appName 应用名称
     * @param title 告警标题(短)
     * @param detail 告警详情(长)
     * @param critical 是否严重告警
     */
    public void alert(String appName, String title, String detail, boolean critical) {
        alertLog.warn("appName:{} title:{} critical:{} detail:{} ", appName, title, critical, detail);
        //TODO 发送告警,建议普通告警IM + 邮件,严重告警:IM + 邮件 + 短信
    }
}
