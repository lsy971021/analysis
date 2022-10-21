package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.regex.Matcher;

/**
 * 黑龙江省公安厅
 */
public class HljgaAnalysis extends AbstractPubFilter {


    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("黑龙江省公安厅");
        regulatoryNotice.setProvince("黑龙江省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        String url = noticeSource.getUrl();
        if (url.contains("hljga")) {
            Elements info = document.getElementsByClass("list_detail_top");
            if (!info.isEmpty()) {
                Element first = info.first();
                Elements h1 = first.getElementsByTag("h1");
                if (!h1.isEmpty()) {
                    String title = h1.text();
                    if (title != null)
                        title = title.trim();
                    regulatoryNotice.setTitle(title);
                    String time = info.text();
                    Matcher matcher = localDateTimePattern.matcher(time);
                    while (matcher.find()) {
                        time = matcher.group(0);
                    }
                    try {
                        LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter1);
                        regulatoryNotice.setPublishTime(localDateTime);
                    } catch (Exception e) {
                    }
                }
            }
        } else if (url.contains("https://mp.weixin.qq.com")) {
            String title = document.getElementById("activity-name").text().trim();
            regulatoryNotice.setTitle(title);
            Matcher matcher = timestampPattern.matcher(value);
            String time = null;
            while (matcher.find()) {
                time = matcher.group(0);
            }
            if (time != null) {
                try {
                    LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(Long.valueOf(time), 0, ZoneOffset.ofHours(8));
                    regulatoryNotice.setPublishTime(localDateTime);
                } catch (NumberFormatException e) {
                }
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Elements elements = null;
        if (url.contains("hljga")) {
            elements = document.getElementsByClass("list_detail_box1");
            if (elements.isEmpty())
                return null;
        }
        if (url.contains("https://mp.weixin.qq.com"))
            return document.getElementById("js_content");
        return elements.last();
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }
}
