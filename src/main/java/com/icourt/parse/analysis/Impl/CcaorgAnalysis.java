package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractConsumerFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 中国消费者协会
 */
public class CcaorgAnalysis extends AbstractConsumerFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("中国消费者协会");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements timeEls = document.getElementsByClass("text_author");
        String time = null;
        try {
            Matcher matcher = localDatePattern.matcher(timeEls.text());
            while (matcher.find()) {
                time = matcher.group(0);
            }
            if (time != null) {
                LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            }
        } catch (Exception e) {
        }

    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementsByClass("text_content").first();
    }
}
