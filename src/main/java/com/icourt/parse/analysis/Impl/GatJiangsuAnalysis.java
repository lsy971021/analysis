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
 * 江苏省公安厅
 */
public class GatJiangsuAnalysis extends AbstractPubFilter {

    Pattern localDatePattern = Pattern.compile("发布时间：(\\d{4})-(\\d{1,2})-(\\d{1,2})");

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("江苏省公安厅");
        regulatoryNotice.setProvince("江苏省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements timeElements = document.getElementsByClass("content_box_R");
        if (!timeElements.isEmpty()) {
            try {
                String text = timeElements.text();
                Matcher matcher = localDatePattern.matcher(text);
                while (matcher.find()) {
                    text = matcher.group();
                }
                String replace = text.replace("发布时间：", "");
                LocalDateTime localDateTime = LocalDate.parse(replace, dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
        Elements titleElements = document.getElementsByClass("MsoNormal");
        if (!titleElements.isEmpty()) {
            String title = titleElements.text().trim();
            regulatoryNotice.setTitle(title);
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Elements elementsByClass = document.getElementsByClass("artCon");
        if (elementsByClass.isEmpty()) {
            return null;
        }
        return elementsByClass.last();
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }
}
