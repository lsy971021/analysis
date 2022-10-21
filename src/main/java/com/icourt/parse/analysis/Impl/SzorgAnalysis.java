package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractConsumerFilter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;

/**
 * 深圳市消费者权益保护委员会
 */
public class SzorgAnalysis extends AbstractConsumerFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("深圳市消费者权益保护委员会");
        regulatoryNotice.setProvince("广东省");
        String value = noticeSource.getValue();
        Object titleObj = JSONPath.read(value, "$.title");
        Object publishDateObj = JSONPath.read(value, "$.publishDate");
        Object introductionObj = JSONPath.read(value, "$.introduction");
        if (introductionObj != null) {
            noticeSource.setHtml(introductionObj.toString());
        } else {
            Object textObj = JSONPath.read(value, "$.text");
            if (textObj != null && !textObj.toString().isEmpty())
                regulatoryNotice.setContent(textObj.toString());
            else {
                regulatoryNotice.setContent("无正文");
                regulatoryNotice.setType(5);
            }
        }
        if (titleObj != null)
            regulatoryNotice.setTitle(titleObj.toString());
        if (publishDateObj != null) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(publishDateObj.toString(), dateTimeFormatter1);
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.body();
    }
}
