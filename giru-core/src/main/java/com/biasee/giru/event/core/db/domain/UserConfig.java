package com.biasee.giru.event.core.db.domain;

import java.util.HashSet;

import lombok.Data;


@Data
public class UserConfig {
    private HashSet<String> followApps = new HashSet<>();


    public void follow(String appId) {
        followApps.add(appId);
    }

    public void unfollow(String appId) {
        followApps.remove(appId);
    }

}
