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
 * 珠海市消费者权益保护委员会
 */
public class ZhuhaiAnalysis extends AbstractConsumerFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("珠海市消费者权益保护委员会");
        regulatoryNotice.setProvince("广东省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element con = document.getElementsByClass("con").last();
        String title = con.getElementsByTag("h1").first().text().trim();
        regulatoryNotice.setTitle(title);
        Elements timeEls = document.getElementsByClass("author");
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
        Element con = document.getElementsByClass("con").last();
        Element content = con.getElementsByClass("c").first();
        content.getElementsByClass("next").remove();
        return content;
    }
}
