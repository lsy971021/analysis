package com.icourt.service.compliance;


import com.icourt.entity.compliance.db.RegulatoryNotice;

/**
 * 主题抽取服务
 */
public interface TopicExtractDomainService {

    /**
     * 监管通报抽取主题
     *
     * @param notice
     * @return
     */
    void extractTopicFromNotice(RegulatoryNotice notice);
}
