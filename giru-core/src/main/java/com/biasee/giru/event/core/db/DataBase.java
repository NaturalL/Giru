package com.biasee.giru.event.core.db;

import com.biasee.giru.event.core.config.EventConfig;
import com.biasee.giru.event.core.config.vo.ErrorLevel;
import com.biasee.giru.event.core.config.vo.FeatureConfig;
import com.biasee.giru.event.core.tools.EventMonitor;
import com.biasee.giru.event.core.db.domain.RuntimeData;
import com.biasee.giru.event.core.db.domain.ErrorApp;
import com.biasee.giru.event.core.util.EventUtils;
import com.biasee.giru.event.core.util.DLog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
public class DataBase {

    private final static DLog configLogger = DLog.getDLog("config", EventMonitor.class);
    private final static String RUNTIME_DATA = "runtime-data";

    @Value("${giru.event.data.path}")
    private String dataPath;
    private DB db;
    private Map<String, String> kvs;
    private RuntimeData runtimeData;

    private volatile Map<String, ErrorApp> baseStats = new ConcurrentHashMap<>();

    private long errorAppUpdateTime = System.currentTimeMillis();

    @Autowired
    private EventConfig eventConfig;

    @PostConstruct
    public void init() {
        new File(dataPath).mkdirs();

        db = DBMaker.fileDB(dataPath + "/event.db")
                .fileMmapEnable()
                .transactionEnable()
                .make();

        kvs = db.hashMap("kvs", Serializer.STRING, Serializer.STRING)
                .createOrOpen();

        String runtimeDataJson = kvs.get(RUNTIME_DATA);
        if (runtimeDataJson != null) {
            runtimeData = EventUtils.fromJson(runtimeDataJson, RuntimeData.class);
        } else {
            runtimeData = new RuntimeData();
        }
        updateBaseStats(System.currentTimeMillis());
    }

    @PreDestroy
    public void close() {
        commit();
        db.close();
    }

    @Scheduled(cron = "0 0/1 * ? * *")
    public void commit() {
        kvs.put(RUNTIME_DATA, EventUtils.toJson(runtimeData));
        db.commit();
    }


    @Scheduled(cron = "0 0 ${giru.event.config.resetHour:10} ? * *")
    public void reset() {
        List<ErrorApp> allErrorApps = getAllErrorApps();
        long now = System.currentTimeMillis();

        //保存历史
        String day = EventUtils.formatDate(now);
        String key = "app-errors:" + day;
        kvs.put(key, EventUtils.toJson(allErrorApps));
        configLogger.info("保存历史异常统计, 日期:{} key:{} value:{}", day, key, kvs.get(key));

        //重置当日计数
        for (Entry<String, ErrorApp> entry : runtimeData.getErrorAppCache().entrySet()) {
            entry.getValue().reset();
            configLogger.info("服务:{} 重置", entry.getKey());
        }

        //删除过期特征
        Iterator<Entry<String, Long>> iterator = runtimeData.getExceptionFeatures().entrySet()
                .iterator();
        while (iterator.hasNext()) {
            Entry<String, Long> entry = iterator.next();
            if (now - entry.getValue() > TimeUnit.DAYS.toMillis(90)) {
                iterator.remove();
                configLogger.warn("删除过期异常特征: {} {}", entry.getKey(), entry.getValue());
            }
        }

        //更新基线
        updateBaseStats(now);

        commit();
    }


    /**
     * 过去 n 天总和作为基线
     */
    public void updateBaseStats(long time) {
        Map<String, ErrorApp> newBaseStats = new ConcurrentHashMap<>();
        int baseStatsDays = eventConfig.core().getBaseStatsDays();
        for (int i = 0; i < baseStatsDays; i++) {
            String day = EventUtils.formatDate(time - TimeUnit.DAYS.toMillis(i));
            String json = kvs.get("app-errors:" + day);
            if (json == null) {
                continue;
            }
            List<ErrorApp> ts = EventUtils.fromJsonToList(json, ErrorApp.class);
            for (ErrorApp t : ts) {
                ErrorApp errorApp = newBaseStats.get(t.getAppId());
                if (errorApp == null) {
                    ErrorApp app = new ErrorApp();
                    errorApp = app;
                    newBaseStats.put(t.getAppId(), app);
                }
                errorApp.merge(t);
            }
        }
        configLogger.info("updateBaseStats:{}", EventUtils.toJson(newBaseStats));
        baseStats = newBaseStats;
    }

    public List<ErrorApp> getAllErrorApps() {
        ConcurrentHashMap<String, ErrorApp> errorAppCache = runtimeData.getErrorAppCache();

        List<ErrorApp> list = new ArrayList<>();
        for (ErrorApp errorApp : errorAppCache.values()) {
            list.add(errorApp);
        }
        return list;
    }

    public List<ErrorApp> getAllErrorApps(Date date) {
        String day = EventUtils.formatDate(date.getTime());
        String key = "app-errors:" + day;
        String json = kvs.get(key);
        if (json == null) {
            return Collections.emptyList();
        }
        List<ErrorApp> errorApps = EventUtils.fromJsonToList(json, ErrorApp.class);
        return errorApps;
    }

    public ErrorApp getErrorApp(String appId, Date date) {
        List<ErrorApp> allErrorApps = getAllErrorApps(date);
        for (ErrorApp errorApp : allErrorApps) {
            if (appId.equals(errorApp.getAppId())) {
                return errorApp;
            }
        }
        return null;
    }

    public ErrorApp getErrorApp(String appId) {
        ConcurrentHashMap<String, ErrorApp> errorAppCache = runtimeData.getErrorAppCache();

        ErrorApp errorApp = errorAppCache.get(appId);
        if (errorApp == null) {
            errorApp = new ErrorApp();
            errorApp.setAppId(appId);
            errorAppCache.put(appId, errorApp);
        }
        return errorApp;
    }

    public void updateErrorAppTime() {
        errorAppUpdateTime = System.currentTimeMillis();
    }

    public long getErrorAppUpdateTime() {
        return errorAppUpdateTime;
    }

    public boolean isTrivial(String feature) {
        FeatureConfig featureConfig = runtimeData.getFeatureConfigs().get(feature);
        return featureConfig != null && ErrorLevel.TRIVIAL.equals(featureConfig.getErrorLevel());
    }
    public boolean isCritical(String feature) {
        FeatureConfig featureConfig = runtimeData.getFeatureConfigs().get(feature);
        return featureConfig != null && ErrorLevel.CRITICAL.equals(featureConfig.getErrorLevel());
    }

    public RuntimeData getRuntimeData() {
        return runtimeData;
    }

    public Map<String, ErrorApp> getBaseStats() {
        return baseStats;
    }
}
