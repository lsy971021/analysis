package com.icourt.service.compliance.ext.impl;

import com.alibaba.fastjson.JSONObject;
import com.icourt.dsmanager.mapper.db.dataobj.RegulatoryNotice;
import com.icourt.dsmanager.mapper.db.dataobj.TopicExtractResult;
import com.icourt.dsmanager.service.extract.TopicExtractDomainService;
import com.icourt.dsmanager.service.extract.TopicExtractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 主题抽取服务
 */
@Service
public class TopicExtractDomainServiceImpl implements TopicExtractDomainService {

    private static final Logger log = LoggerFactory.getLogger(TopicExtractDomainServiceImpl.class);

    @Autowired
    private TopicExtractService topicExtractService;

    /**
     * 监管通报抽取主题
     *
     * @param notice
     * @return
     */
    @Override
    public void extractTopicFromNotice(RegulatoryNotice notice) {
        try {
            TopicExtractResult result = topicExtractService.extractTopic(notice);
            notice.setExtractFlag(1);
            if (result != null && result.getMatchResults() != null && !result.getMatchResults().isEmpty()) {
                notice.setMatchResult(JSONObject.toJSONString(result.getMatchResults()));
                notice.setKeywords(result.getKeywords());
                notice.setTopic(result.getTopics());
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("抽取主题失败，程序继续解析...", e);
        }
    }
}
