package com.biasee.giru.event.core.db.domain;

import com.biasee.giru.event.core.config.vo.FeatureConfig;

import java.util.concurrent.ConcurrentHashMap;


@lombok.Data
public class RuntimeData {
    private ConcurrentHashMap<String, Long> exceptionFeatures = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ErrorApp> errorAppCache = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, UserConfig> userConfigs = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, FeatureConfig> featureConfigs = new ConcurrentHashMap<>();

    public synchronized UserConfig getUserConfig(String userId) {
        UserConfig userConfig = userConfigs.get(userId);
        if (userConfig == null) {
            userConfig = new UserConfig();
            userConfigs.put(userId, userConfig);
        }
        return userConfig;
    }
}
