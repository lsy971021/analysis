package com.icourt.entity.compliance.db;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * app专栏实体类
 */
@Data
public class AppColumn {

    @ApiModelProperty(name = "id", value = "id", example = "1")
    private Long id;

    @ApiModelProperty(name = "aid", value = "文章去重id")
    private String aid;

    @ApiModelProperty(name = "process", value = "0 监控  1 审核  2 列表，新增时传1", required = true, example = "1")
    private Integer process;

    @ApiModelProperty(hidden = true, value = "第三方id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String thirdId;

    @ApiModelProperty(name = "type", value = "操作类型:-1 不处理  0 待处理 1 待审核 2  审核通过  3 上线  4 报错  5 不提取  6 打回,新增时不传", required = true, example = "1")
    private Integer type;

    @ApiModelProperty(name = "appName", value = "通报产品（app名称）")
    private String appName;

    @ApiModelProperty(name = "appDeveloper", value = "应用开发者")
    private String appDeveloper;

    @ApiModelProperty(name = "appType", value = "app分类")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String appType;

    @ApiModelProperty(name = "appSource", value = "应用来源")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String appSource;

    @ApiModelProperty(name = "appVersion", value = "产品版本")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String appVersion;

    @ApiModelProperty(name = "noticeSourceTitle", value = "通报来源(标题)")
    private String noticeSourceTitle;

    @ApiModelProperty(name = "noticeSourceUrl", value = "通报来源url")
    private String noticeSourceUrl;

    @ApiModelProperty(name = "link", value = "系统（内部）链接")
    private String link;

    @ApiModelProperty(name = "noticeMainBody", value = "所属部委", example = "中华人民共和国工业和信息化部")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String noticeMainBody;

    @ApiModelProperty(name = "noticeOrgan", value = "监管/通报机构")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String noticeOrgan;

    @ApiModelProperty(name = "productForm", value = "产品形态")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String productForm;

    @ApiModelProperty(name = "regulatoryProcess", value = "监管处理、监管结果")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String result;

    @ApiModelProperty(name = "province", value = "地域", example = "北京市")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String province;

    @ApiModelProperty(value = "修改人")
    private String modifier;

    @ApiModelProperty(value = "审核人")
    private String auditor;

    @ApiModelProperty(name = "noticeDate", value = "通报时间（发布时间）", example = "2022-06-21 11:11:11")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private LocalDateTime noticeDate;

    @ApiModelProperty(value = "创建时间", example = "2022-06-21 11:11")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新时间", example = "2022-06-21 11:11")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "审核时间", example = "2022-06-21 11:11")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkTime;

    @ApiModelProperty(value = "上线时间", example = "2022-06-21 11:11")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadTime;

    @ApiModelProperty(hidden = true, value = "首次审核通过上线时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime firstUploadTime;

    @ApiModelProperty(name = "comment", value = "备注")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String comment;

    @ApiModelProperty(name = "reason", value = "原因", example = "报错原因:原网站无法打开")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String reason;

    @ApiModelProperty(hidden = true, value = "0 正常 1 删除")
    @JsonIgnore
    private Integer del;

}
