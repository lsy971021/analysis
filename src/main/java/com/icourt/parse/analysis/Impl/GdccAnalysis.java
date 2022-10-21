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
 * 广东省消费者权益保护委员会
 */
public class GdccAnalysis extends AbstractConsumerFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("广东省消费者权益保护委员会");
        regulatoryNotice.setProvince("广东省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements titleEl = document.getElementsByClass("con");
        Elements h1 = titleEl.first().getElementsByTag("h1");
        String title = h1.text().trim();
        regulatoryNotice.setTitle(title);
        Elements timeEl = document.getElementsByClass("h1p");
        String timeInfo = timeEl.text();
        try {
            Matcher matcher = localDatePattern.matcher(timeInfo);
            while (matcher.find()) {
                timeInfo = matcher.group(0);
            }
            LocalDateTime localDateTime = LocalDate.parse(timeInfo, dateFormatter).atStartOfDay();
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementsByClass("c").first();
    }
}
