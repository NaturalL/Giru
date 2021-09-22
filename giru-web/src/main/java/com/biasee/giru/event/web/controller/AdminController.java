package com.biasee.giru.event.web.controller;

import com.biasee.giru.event.core.config.EventConfig;
import com.biasee.giru.event.core.config.vo.ErrorLevel;
import com.biasee.giru.event.core.config.vo.FeatureConfig;
import com.biasee.giru.event.core.db.DataBase;
import com.biasee.giru.event.core.db.domain.ErrorApp;
import com.biasee.giru.event.core.db.domain.UserConfig;
import com.biasee.giru.event.core.service.VisualService;
import com.biasee.giru.event.core.service.dto.AppHistory;
import com.biasee.giru.event.web.controller.dto.FeatureConfigCmd;
import com.biasee.giru.event.web.controller.dto.Resp;
import com.biasee.giru.event.web.service.UserService;
import com.biasee.giru.event.web.service.dto.ClientUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private DataBase dataBase;
    @Autowired
    private EventConfig eventConfig;
    @Autowired
    private VisualService visualService;
    @Autowired
    private UserService userService;

    @GetMapping(value = "appErrors")
    public Resp appErrors(@RequestParam(value = "updateTime", required = false) Long updateTime) {

        long dbUpdateTime = dataBase.getErrorAppUpdateTime();
        if (updateTime != null && dbUpdateTime == updateTime) {
            return Resp.error(304).msg("无更新");
        }
        ClientUser loginUser = userService.getLoginUser();
        UserConfig userConfig = dataBase.getRuntimeData().getUserConfig(loginUser.getId());

        List<ErrorApp> list = dataBase.getAllErrorApps();
        Map<String,FeatureConfig> featureConfigs = new HashMap<>();
        for (ErrorApp errorApp : list) {
            for (String f : errorApp.getStats().keySet()) {
                featureConfigs.put(f, dataBase.getRuntimeData().getFeatureConfigs().get(f));
            }
        }
        return Resp.success().data(list)
                .put("updateTime", dbUpdateTime)
                .put("userConfig", userConfig)
                .put("featureConfigs", featureConfigs)
                ;
    }

    @GetMapping(value = "history")
    public Resp history(@RequestParam(value = "appId") String appId) {
        AppHistory history = visualService.getHistory(appId, 7);
        return Resp.success().data(history).put("size", history.getCounts().size());
    }

    @GetMapping(value = "client/config")
    public Resp getConfig() {
        return Resp.success()
                .put("version", eventConfig.core().getClientVersion())
                .put("sls", eventConfig.core().getAliyunSls())
                .put("bizPackages", eventConfig.core().getBizPackagesRegex());
    }

    @GetMapping(value = "user")
    public Resp getLoginUser() {
        ClientUser loginUser = userService.getLoginUser();
        return Resp.success()
                .put("user", loginUser.getName())
                .put("id", loginUser.getId());
    }

    @PostMapping(value = "followApp")
    public Resp followApp(@RequestParam("appId") String appId, @RequestParam("isFollow") Boolean isFollow) {
        ClientUser loginUser = userService.getLoginUser();
        UserConfig userConfig = dataBase.getRuntimeData()
                .getUserConfig(loginUser.getId());
        if (isFollow) {
            userConfig.follow(appId);
        } else {
            userConfig.unfollow(appId);
        }
        return Resp.success();
    }

    @PostMapping(value = "configFeature")
    public Resp configFeature(@Valid @RequestBody FeatureConfigCmd cmd) {
        FeatureConfig featureConfig = new FeatureConfig();
        featureConfig.setAppId(cmd.getAppId());
        featureConfig.setElements(cmd.getFeatureElements());
        featureConfig.setErrorLevel(ErrorLevel.valueOf(cmd.getErrorLevel()));
        dataBase.getRuntimeData().getFeatureConfigs().put(cmd.getErrorFeature(), featureConfig);
        return Resp.success();
    }
}
