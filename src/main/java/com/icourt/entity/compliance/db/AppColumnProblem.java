package com.icourt.entity.compliance.db;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import net.minidev.json.annotate.JsonIgnore;

/**
 * app专栏通报问题
 */
@Data
public class AppColumnProblem {

    @ApiModelProperty(name = "id", value = "id", example = "1")
    private Long id;

    @ApiModelProperty(name = "aid", value = "app_column 主键", example = "1")
    private Long appId;

    @ApiModelProperty(value = "通报问题大类")
    private String mainProblem;

    @ApiModelProperty(value = "通报问题子类")
    private String problem;

    @ApiModelProperty(value = "原文通报,json")
    private String originalNotification;

    @ApiModelProperty(value = "是否删除")
    @JsonIgnore
    private Integer del;

}
