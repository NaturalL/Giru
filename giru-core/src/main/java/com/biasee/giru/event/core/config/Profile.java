package com.biasee.giru.event.core.config;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class Profile {

    private Environment environment;

    public Profile(Environment environment) {
        this.environment = environment;
    }


    public boolean isOnline() {
        return "online".equalsIgnoreCase(getProfile());

    }

    public boolean isPrepare() {
        return "prepare".equalsIgnoreCase(getProfile());
    }


    public boolean isTest() {
        return "test".equalsIgnoreCase(getProfile());
    }

    public boolean isDev() {
        return "dev".equalsIgnoreCase(getProfile());
    }

    public boolean isLocal() {
        return "local".equalsIgnoreCase(getProfile());
    }


    public boolean isOnlineOrPrepare() {
        return isOnline() || isPrepare();

    }

    public boolean isTestOrDev() {
        return isTest() || isDev();
    }

    public String getProfile() {
        return environment.getActiveProfiles()[0];
    }

}
