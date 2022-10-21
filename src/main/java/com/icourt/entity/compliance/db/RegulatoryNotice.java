package com.icourt.entity.compliance.db;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 合规监管通告表
 */
@Data
@ApiModel(value = "监管通告对象", description = "监管通告参数")
public class RegulatoryNotice implements Serializable, Cloneable {

    @ApiModelProperty(name = "id", value = "id", example = "1")
    private Long id;

    @ApiModelProperty(name = "sourceUrl", value = "原链接", required = true, example = "xxx.com")
    @NotNull(message = "原链接不能为空")
    private String sourceUrl;

    @ApiModelProperty(name = "title", value = "标题", required = true, example = "“云上冬奥”等你参与")
    @NotNull(message = "标题不能为空")
    private String title;

    @ApiModelProperty(name = "province", value = "地域", example = "北京市")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String province;

    @ApiModelProperty(name = "content", value = "正文", required = true, example = "这里是正文。")
    @NotNull(message = "正文不能为空")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String content;

    @ApiModelProperty(name = "noticeProductNum", value = "产品通报数",  example = "1")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Integer noticeProductNum;

    @ApiModelProperty(name = "publishTime", value = "发布时间", example = "2022-06-21 11:11:11")
    private LocalDateTime publishTime;

    @ApiModelProperty(name = "process", value = "0 监控  1 审核  2 列表，新增时传1", required = true, example = "1")
    @NotNull(message = "process不能为空")
    private Integer process;


    @ApiModelProperty(hidden = true, value = "第三方id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thirdId;


    @ApiModelProperty(hidden = true, value = "创建时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    @ApiModelProperty(hidden = true, value = "更新时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;


    @ApiModelProperty(hidden = true, value = "审核时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;


    @ApiModelProperty(name = "type", value = "操作类型:-1 不处理  0 待处理 1 待审核 2  审核通过  3 上线  4 报错  5 不提取  6 打回,新增时不传", required = true, example = "1")
    @NotNull(message = "操作类型不能为空")
    private Integer type;


    @ApiModelProperty(hidden = true, name = "cid", value = "去重id")
    private String cid;


    @ApiModelProperty(name = "reason", value = "原因:json通用状态:报错、普通、打回、不提取")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String reason;


    @ApiModelProperty(name = "comment", value = "备注")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String comment;


    @ApiModelProperty(hidden = true, value = "修改人")
    private String modifier;


    @ApiModelProperty(hidden = true, value = "审核人")
    private String auditor;


    @ApiModelProperty(hidden = true, value = "0 正常 1 删除")
    @JsonIgnore
    private Integer del;


    @ApiModelProperty(hidden = true, value = "上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;


    @ApiModelProperty(hidden = true, value = "首次审核通过上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstUploadTime;


    @ApiModelProperty(name = "noticeMainBody", value = "所属部委", required = true, example = "中华人民共和国工业和信息化部")
    @NotNull(message = "所属部委不能为空")
    private String noticeMainBody;


    @ApiModelProperty(name = "noticeOrgan", value = "通报机构")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String noticeOrgan;


    @ApiModelProperty(name = "themeLabels", value = "通报类型", example = "行政处罚")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String themeLabels;


    @ApiModelProperty(name = "violateType", value = "处罚类型")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String violateType;


    @ApiModelProperty(name = "claimValue", value = "处罚金额")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String claimValue;

    @ApiModelProperty(name = "topic", value = "主题词")
    private String topic;

    @ApiModelProperty(name = "keywords", value = "主题命中的关键词")
    private String keywords;

    @ApiModelProperty(name = "matchResult", value = "命中结果")
    private String matchResult;

    @ApiModelProperty(name = "extractFlag", value = "抽取关键词标识")
    private Integer extractFlag;

    /**
     * 格式化符号
     */
    public void format() {
        String regex = "(,|，|/|;|；)";
        if (StringUtils.isNotBlank(this.violateType))
            this.violateType = violateType.replaceAll(regex, ";");
        if (StringUtils.isNotBlank(this.topic))
            this.topic = topic.replaceAll(regex, ";");
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
