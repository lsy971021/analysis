package com.icourt.entity.compliance.db;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class GzhSource {

    @TableId
    private Long id;

    private String biz;

    private String html;

    private Date createTime;

    private Date updateTime;

    /**
     * 去重id
     */
    private String mid;

    /**
     * 文章发布时间
     */
    private Date publishTime;

    /**
     * 0 正常  1 重新采集
     */
    private Integer type;

    /**
     * 文章作者
     */
    private String author;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章url
     */
    private String contentUrl;

    /**
     * 封面url
     */
    private String coverUrl;

    /**
     * 扩展字段
     */
    private String extJson;

    /**
     * 是否需要解析 0 需要 1 不需要 2 解析异常
     */
    private Integer isResolving;

    /**
     * 所属分类 0 实务文章 1 合规
     */
    private Integer category;

    /**
     * html是否存在  0存在 1不存在
     */
    private Integer existHtml;

}
