package com.biasee.giru.event.core.config;

import com.biasee.giru.event.core.config.vo.AppConfig;

import java.util.*;
import java.util.stream.*;

import com.biasee.giru.event.core.util.EventUtils;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@ConfigurationProperties(prefix = "giru.event.config")
@Configuration
@Data
public class EventCoreConfig {
    private Map<String, AppConfig> appConfigs = new HashMap<>();

    private int newExceptionExpireDays = 1;
    private int baseStatsDays = 7;
    private int resetHour = 10;

    private String[] criticalExceptions;
    private String[] ignoreKeywords;

    private boolean enableAnalyze = true;
    private boolean increaseAnalyze = true;

    private String[] bizPackages;
    private String clientVersion;
    private String aliyunSls;

    public String getBizPackagesRegex() {
        if (EventUtils.isEmpty(bizPackages)) {
            return "";
        }
        return Arrays.stream(bizPackages).filter(StringUtils::isNotBlank)
                .map(p -> p.replaceAll("\\.", "\\.") + ".*").collect(Collectors.joining("|"));
    }

    public AppConfig getAppConfig(String appId) {
        AppConfig appConfig = appConfigs.get(appId);
        if (appConfig == null) {
            appConfig = new AppConfig();
            appConfig.setIgnoreKeywords(ignoreKeywords);
        } else {
            if (EventUtils.isEmpty(appConfig.getIgnoreKeywords())) {
                appConfig.setIgnoreKeywords(ignoreKeywords);
            }
        }
        return appConfig;
    }
}
