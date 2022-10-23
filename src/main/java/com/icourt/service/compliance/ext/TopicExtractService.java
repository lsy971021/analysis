package com.icourt.service.compliance.ext;


import com.icourt.entity.compliance.TopicExtractResult;

/**
 * 监管通报 主题抽取服务
 */
public interface TopicExtractService {

    /**
     * 抽取主题
     *
     * @param obj
     * @return
     */
    TopicExtractResult extractTopic(Object obj);
}
