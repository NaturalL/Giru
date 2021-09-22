package com.biasee.giru.event.web.controller.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

/**
 * @author zhengjie 2021-09-19
 */
@Data
public class FeatureConfigCmd {
    @NotBlank
    private String appId;
    @NotBlank
    private String errorLevel;
    @NotEmpty
    private String[] featureElements;
    @NotBlank
    private String errorFeature;
}
