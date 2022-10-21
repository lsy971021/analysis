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
import java.util.regex.Pattern;

/**
 * 江西省公安厅
 */
public class GatJiangxiAnalysis extends AbstractPubFilter {

    Pattern timePattern = Pattern.compile("发布时间：(\\d{4})-(\\d{1,2})-(\\d{1,2})");

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("江西省公安厅");
        regulatoryNotice.setProvince("江西省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleEl = document.getElementById("ct");
        if (titleEl != null) {
            String title = titleEl.text().trim();
            regulatoryNotice.setTitle(title);
        }
        Elements timeEl = document.getElementsByClass("fl p1");
        if (!timeEl.isEmpty()) {
            try {
                String text = timeEl.text();
                Matcher matcher = timePattern.matcher(text);
                while (matcher.find()) {
                    text = matcher.group(0);
                }
                LocalDateTime localDateTime = LocalDate.parse(text.replace("发布时间：",""), dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Elements elements = document.getElementsByClass("show_content");
        if (!elements.isEmpty()) {
            return elements.first();
        }
        return null;
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }
}
