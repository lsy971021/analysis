package com.icourt.entity.compliance;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 监管通报
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicMatchResult implements Serializable {

    @ApiModelProperty(value = "主题")
    private String topic;

    @ApiModelProperty(value = "命中类型：正文命中、标题命中")
    private String matchType;

    @ApiModelProperty(value = "命中关键词")
    private String keywords;

}
