package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractConsumerFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 陕西省消费者权益保护委员会
 */
public class SxwqAnalysis extends AbstractConsumerFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("陕西省消费者权益保护委员会");
        regulatoryNotice.setProvince("陕西省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleEl = document.getElementsByClass("title").first();
        String title = titleEl.text().trim();
        regulatoryNotice.setTitle(title);
        Elements timeEls = document.getElementsByClass("property");
        String time = null;
        try {
            Matcher matcher = localDateTimePattern.matcher(timeEls.text());
            while (matcher.find()) {
                time = matcher.group(0);
            }
            if (time != null) {
                LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter1);
                regulatoryNotice.setPublishTime(localDateTime);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementsByClass("conTxt").first();
    }
}
