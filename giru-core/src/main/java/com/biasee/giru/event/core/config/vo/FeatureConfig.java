package com.biasee.giru.event.core.config.vo;

import lombok.Data;


/**
 * @author zhengjie 2021-09-19
 */
@Data
public class FeatureConfig {
    private String appId;
    private String[] elements;
    private ErrorLevel errorLevel;
}
