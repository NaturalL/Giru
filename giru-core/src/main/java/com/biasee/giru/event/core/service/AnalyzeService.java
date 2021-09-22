package com.biasee.giru.event.core.service;

import com.biasee.giru.event.client.events.ErrorEvent;
import com.biasee.giru.event.core.config.EventConfig;
import com.biasee.giru.event.core.tools.EventMonitor;
import com.biasee.giru.event.core.db.DataBase;
import com.biasee.giru.event.core.db.domain.ErrorApp;
import com.biasee.giru.event.core.db.domain.ErrorStats;
import com.biasee.giru.event.core.util.EventUtils;
import com.biasee.giru.event.core.util.DLog;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AnalyzeService {

    private final static DLog root = DLog.getDLog("root", EventMonitor.class);

    @Autowired
    private DataBase dataBase;

    @Autowired
    private AlertService alertService;

    @Autowired
    private EventConfig eventConfig;


    public void analyze(ErrorEvent errorEvent, ErrorApp errorApp) {
        if (!eventConfig.core().isEnableAnalyze()) {
            return;
        }
        boolean alerted = false;
        try {
            String exceptionRaw = EventUtils.getExceptionRaw(errorEvent);
            if (StringUtils.isNotBlank(errorEvent.takeStackTrace())
                    || StringUtils.isNotBlank(exceptionRaw)) {
                alerted = analyzeNew(errorEvent, errorApp);
            }
            if (!alerted) {
                analyzeIncrease(errorEvent, errorApp);
            }
        } catch (Throwable t) {
            root.warn(t, "analyze error");
        }
    }

    /**
     * 新异常告警
     */
    private boolean analyzeNew(ErrorEvent errorEvent, ErrorApp errorApp) {

        Map<String, Long> exceptionFeatures = dataBase.getRuntimeData()
                .getExceptionFeatures();
        int expireDays = eventConfig.core().getNewExceptionExpireDays();

        String feature = EventUtils.getExceptionFeature(errorEvent);
        Long timestamp = exceptionFeatures.get(feature);
        exceptionFeatures.put(feature, errorEvent.getTime());

        ErrorStats errorStats = errorApp.getErrorStats(feature);

        long now = System.currentTimeMillis();
        if (timestamp == null || (now - timestamp) > TimeUnit.DAYS.toMillis(expireDays)) {
            if (errorStats.tryAlert("new")) {
                alertService.newErrorAlert(errorEvent);
            }
            return true;
        }
        return false;
    }

    /**
     * 突增异常告警,异常数量超过过去 N 天 1倍,10倍,100倍... 告警
     */
    private boolean analyzeIncrease(ErrorEvent errorEvent, ErrorApp errorApp) {
        if (!eventConfig.core().isIncreaseAnalyze()) {
            return false;
        }
        long now = System.currentTimeMillis();
        String feature = EventUtils.getExceptionFeature(errorEvent);


        Map<String, ErrorApp> baseStats = dataBase.getBaseStats();
        ErrorApp baseApp = baseStats.get(errorEvent.getAppId());
        ErrorStats errorStats = errorApp.getErrorStats(feature);

        if (baseApp != null) {
            ErrorStats baseErrorStats = baseApp.getErrorStats(feature);
            if (baseErrorStats == null) {
                baseErrorStats = new ErrorStats();
            }
            int baseTotal = baseErrorStats.getTotal().get();
            int currentTotal = errorStats.getTotal().get();

            if (currentTotal > baseTotal) {
                if (baseTotal == 0) {
                    baseTotal = 1;
                }
                int power = (int) Math.log10(errorStats.getTotal().get() / baseTotal);

                if (errorStats.getTotal().get() > 10
                        && now - baseApp.getUpdateTime() > TimeUnit.DAYS.toMillis(eventConfig.core().getBaseStatsDays() / 2)
                        && errorStats.tryAlert("increase:" + power)) {
                    alertService.increaseErrorAlert(errorEvent, errorStats.getTotal().get(), power);
                    return true;
                }
            }
        }
        return false;
    }


}
