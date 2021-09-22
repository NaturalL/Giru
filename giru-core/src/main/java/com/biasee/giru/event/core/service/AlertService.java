package com.biasee.giru.event.core.service;

import com.biasee.giru.event.client.events.ErrorEvent;
import com.biasee.giru.event.core.config.EventConfig;
import com.biasee.giru.event.core.config.vo.AppConfig;
import com.biasee.giru.event.core.db.DataBase;
import com.biasee.giru.event.core.util.EventUtils;
import com.biasee.giru.event.core.util.DLog;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;


@Service
public class AlertService {

    private static final DLog alertLog = DLog.getDLog("alert", AlertService.class);

    @Autowired
    private EventConfig config;
    @Autowired
    private SpringTemplateEngine templateEngine;
    @Autowired
    private AlertApi alertApi;
    @Autowired
    private DataBase dataBase;


    public void newErrorAlert(ErrorEvent errorEvent) {
        String exception = EventUtils.getException(errorEvent);
        String feature = EventUtils.getExceptionFeature(errorEvent);

        String title = DLog
                .format("新异常! 服务:{} 异常:{} {} 时间:{}",
                        errorEvent.getAppId(), exception,
                        StringUtils.abbreviate(errorEvent.getError(), 100),
                        EventUtils.formatDateTime(errorEvent.getTime()));

        if (isIgnore(errorEvent, exception, feature)) {
            alertLog.info("忽略告警: {}", title);
        } else {
            errorAlert(errorEvent, title, false);
        }
    }

    public void increaseErrorAlert(ErrorEvent errorEvent, int count, int power) {
        String exception = EventUtils.getException(errorEvent);
        String feature = EventUtils.getExceptionFeature(errorEvent);
        String title = DLog
                .format("突增异常{}! 服务:{} 数量:{} 异常:{} {} 时间:{}",
                        power > 0 ? (int) Math.pow(10, power) + "倍" : "",
                        errorEvent.getAppId(), count, EventUtils.getException(errorEvent),
                        StringUtils.abbreviate(errorEvent.getError(), 100),
                        EventUtils.formatDateTime(errorEvent.getTime()));

        if (isIgnore(errorEvent, exception, feature) && count < 50) {
            alertLog.info("忽略告警: {}", title);
        } else {
            boolean danger = false;
            if (power >= 1 && count > 1000) {
                danger = true;
            }
            errorAlert(errorEvent, title, danger);
        }
    }

    public void errorAlert(ErrorEvent errorEvent, String title, boolean danger) {
        String exception = EventUtils.getException(errorEvent);
        String feature = EventUtils.getExceptionFeature(errorEvent);

        String error = StringUtils.abbreviate(errorEvent.getError(), 1000);

        //超长截断
        int length = 4096 - 1500 - StringUtils.length(error);

        Map<String, Object> params = new HashMap<>();
        params.put("name", errorEvent.getAppId());
        params.put("ip", errorEvent.getIp());
        params.put("lastFireTime", EventUtils.formatDateTime(errorEvent.getTime()));
        params.put("logger", errorEvent.label("logger"));
        params.put("error", error);
        params.put("errorPre", StringUtils.contains(error, "\n"));
        params.put("stackTrace", EventUtils.highlight(StringUtils.abbreviate(errorEvent.takeStackTrace(), length),
                config.core().getBizPackagesRegex()));

        String detail = render("errorAlert", params);

        if (danger || isDanger(errorEvent, exception, feature)) {
            alertApi.alert(errorEvent.getAppId(), title, detail, true);
        } else {
            alertApi.alert(errorEvent.getAppId(), title, detail, false);
        }
    }

    private boolean isIgnore(ErrorEvent errorEvent, String exception, String feature) {
        if (isDanger(errorEvent, exception, feature)) {
            return false;
        }
        if (dataBase.isTrivial(feature)) {
            return true;
        }
        AppConfig appConfig = config.core().getAppConfig(errorEvent.getAppId());
        return isIgnore(errorEvent, appConfig.getIgnoreKeywords())
                || isIgnore(errorEvent, config.core().getIgnoreKeywords());
    }

    private boolean isIgnore(ErrorEvent errorEvent, String[] keywords) {
        String msg = null;
        if (StringUtils.isNotBlank(errorEvent.takeStackTrace())) {
            String[] split = errorEvent.takeStackTrace().split("\n", 2);
            if (split != null && split.length > 0) {
                msg = split[0];
            }
        }

        if (keywords != null) {
            for (String k : keywords) {
                if (StringUtils.isBlank(k)) {
                    continue;
                }
                if (StringUtils.contains(errorEvent.getError(), k)
                        || StringUtils.contains(msg, k)
                        || StringUtils.contains(errorEvent.label("logger"), k)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isDanger(ErrorEvent errorEvent, String exception, String feature) {
        if (dataBase.isCritical(feature)) {
            return true;
        }
        String[] exps = config.core().getCriticalExceptions();
        if (!EventUtils.isEmpty(exps)) {
            for (String exp : exps) {
                if (exp.equals(exception)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String render(String template, Map<String, Object> params) {
        Context context = new Context(LocaleContextHolder.getLocale());
        context.setVariables(params);
        return templateEngine.process(template, context);
    }
}
