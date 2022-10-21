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
import java.util.regex.Pattern;

/**
 * 浙江省公安厅
 */
public class GatZjAnalysis extends AbstractPubFilter {

    Pattern timePattern = Pattern.compile("发布日期：(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{1,2}):(\\d{1,2})");

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("浙江省");
        regulatoryNotice.setNoticeOrgan("浙江省公安厅");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements titleElements = document.getElementsByClass("art_title");
        if (!titleElements.isEmpty()) {
            String title = titleElements.get(0).getElementsByTag("h2").text().trim();
            regulatoryNotice.setTitle(title);
        }
        Elements timeElements = document.getElementsByClass("fz_xx");
        if (!timeElements.isEmpty()) {
            //发布日期：2022-07-28 16:40
            try {
                String text = timeElements.text();
                String time = null;
                Matcher matcher = timePattern.matcher(text);
                while (matcher.find()) {
                    time = matcher.group(0);
                }
                if (time != null) {
                    time = time.replace("发布日期：", "");
                    LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
                    regulatoryNotice.setPublishTime(localDateTime);
                }
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
