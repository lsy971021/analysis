package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractConsumerFilter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;

/**
 * 吉林省消费者协会
 */
public class JlorgAnalysis extends AbstractConsumerFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("吉林省消费者协会");
        regulatoryNotice.setProvince("吉林省");
        String value = noticeSource.getValue();
        Object titleObj = JSONPath.read(value, "$.title");
        Object timeObj = JSONPath.read(value, "$.add_time");
        Object contentObj = JSONPath.read(value, "$.content");
        if (timeObj != null) {
            regulatoryNotice.setTitle(titleObj.toString());
        }
        if (contentObj != null && !contentObj.toString().isEmpty()) {
            noticeSource.setHtml(contentObj.toString());
        } else {
            regulatoryNotice.setContent("无正文");
            regulatoryNotice.setType(5);
        }
        if (timeObj != null) {
            String time = null;
            try {
                Matcher matcher = timestampPatternMills.matcher(timeObj.toString());
                while (matcher.find()) {
                    time = matcher.group(0);
                }
                if (time != null) {
                    LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(Long.valueOf(time) / 1000L, 0, ZoneOffset.ofHours(8));
                    regulatoryNotice.setPublishTime(localDateTime);
                }
            } catch (NumberFormatException e) {
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.body();
    }
}
