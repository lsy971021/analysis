package com.icourt.entity.compliance;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 监管通报
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopicExtractResult implements Serializable {

    @ApiModelProperty(value = "主题")
    private String topics;

    @ApiModelProperty(value = "命中关键词")
    private String keywords;

    @ApiModelProperty(value = "匹配结果")
    private List<TopicMatchResult> matchResults;


}
