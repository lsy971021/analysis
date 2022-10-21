package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 安徽省公安厅
 */
public class GatAhAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("安徽省公安厅");
        regulatoryNotice.setProvince("安徽省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements newstitle = document.getElementsByClass("newstitle");
        if (!newstitle.isEmpty()) {
            String title = newstitle.first().text().trim();
            regulatoryNotice.setTitle(title);
        }
        Elements fbxx = document.getElementsByClass("fbxx");
        if (!fbxx.isEmpty()) {
            try {
                String publish = fbxx.text();
                Matcher matcher = localDateTimePattern1.matcher(publish);
                while (matcher.find()) {
                    publish = matcher.group(0);
                }
                LocalDateTime localDateTime = LocalDateTime.parse(publish, dateTimeFormatter);
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("zoom");
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }
}
