package com.icourt.parse.analysis;

import com.icourt.entity.compliance.parse.NoticeSource;

import java.util.List;

/**
 * 链接数据源读取数据源
 */
public interface SourceConnect {

    /**
     * 获取 table 中符合条件的NoticeSource 列表
     * @param tableName
     * @return
     */
    List<NoticeSource> getSources(String tableName);
}
