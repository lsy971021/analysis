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
 * 陕西省公安厅
 */
public class GatShaanxiAnalysis extends AbstractPubFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("陕西省公安厅");
        regulatoryNotice.setProvince("陕西省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements timeEl = document.getElementsByClass("cm-con");
        if (timeEl.isEmpty()) {
            timeEl = document.getElementsByClass("pages-date");
        }
        try {
            String text = timeEl.text();
            String time = null;
            Matcher matcher = localDateTimePattern1.matcher(text);
            while (matcher.find()) {
                time = matcher.group(0);
            }
            if (time != null) {
                LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
                regulatoryNotice.setPublishTime(localDateTime);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Elements elements = document.getElementsByClass("lf article-content content");
        if (elements.isEmpty()) {
            return document.getElementsByClass("pages_content").first();
        }
        Element first = elements.first();
        Element other = first.getElementById("div_div");
        other.remove();
        return first;
    }
}
