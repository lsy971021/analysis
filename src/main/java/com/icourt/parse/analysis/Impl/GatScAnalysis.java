package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 四川省公安厅
 */
public class GatScAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("四川省公安厅");
        regulatoryNotice.setProvince("四川省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleEl = document.getElementsByClass("mainbox-header").first();
        Elements h1 = titleEl.getElementsByTag("h1");
        String title = h1.first().text().trim();
        regulatoryNotice.setTitle(title);
        String publishTime = document.getElementsByClass("publishTime").text();
        Matcher matcher = localDatePattern.matcher(publishTime);
        while (matcher.find()) {
            publishTime = matcher.group(0);
        }
        try {
            LocalDateTime localDateTime = LocalDate.parse(publishTime, dateFormatter).atStartOfDay();
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementsByClass("mainbox-midpart").first();
    }
}
