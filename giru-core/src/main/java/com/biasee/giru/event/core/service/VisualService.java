package com.biasee.giru.event.core.service;

import com.biasee.giru.event.core.config.EventConfig;
import com.biasee.giru.event.core.db.DataBase;
import com.biasee.giru.event.core.db.domain.ErrorApp;
import com.biasee.giru.event.core.db.domain.ErrorStats;
import com.biasee.giru.event.core.service.dto.AppHistory;
import com.biasee.giru.event.core.util.EventUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class VisualService {
    @Autowired
    private EventConfig eventConfig;

    @Autowired
    private DataBase dataBase;

    public AppHistory getHistory(String appId, int days) {
        AppHistory history = new AppHistory();
        Map<String, String> nameMap = new HashMap<>();
        Map<String, List<Integer>> countMap = new HashMap<>();
        List<String> dayList = new ArrayList<>();

        ErrorApp all = new ErrorApp();

        List<ErrorApp> appDays = new ArrayList<>();

        long now = System.currentTimeMillis();
        for (int i = days; i >= 0; i--) {
            ErrorApp errorApp = dataBase.getErrorApp(appId, new Date(now - TimeUnit.DAYS.toMillis(i)));
            if (errorApp != null) {
                appDays.add(errorApp);
                all.merge(errorApp);
                dayList.add(EventUtils.formatDate(now - TimeUnit.DAYS.toMillis(i + 1)));
            }
        }
        ErrorApp current = dataBase.getRuntimeData().getErrorAppCache().get(appId);
        if (current != null) {
            current = current.copy();
            appDays.add(current);
            all.merge(current);
            if (new Date().getHours() < eventConfig.core().getResetHour()) {
                dayList.add(EventUtils.formatDate(now - TimeUnit.DAYS.toMillis(1)));
            } else {
                dayList.add(EventUtils.formatDate(now));
            }
        }
        for (ErrorStats errorStats : all.getStats().values()) {
            String errorFeature = errorStats.getErrorFeature();
            String codeLine = EventUtils.getCodeLine(errorStats.getStackTrace());
            nameMap.put(errorFeature, errorStats.getErrorName() + (codeLine != null ? "(" + codeLine + ")" : ""));
            List<Integer> counts = new ArrayList<>();
            for (ErrorApp errorApp : appDays) {
                ErrorStats stats = errorApp.getErrorStats(errorFeature);
                if (stats != null) {
                    counts.add(stats.getTotal().get());
                } else {
                    counts.add(0);
                }
            }
            countMap.put(errorFeature, counts);
        }
        history.setCounts(countMap);
        history.setDays(dayList);
        history.setNames(nameMap);
        return history;
    }

}
