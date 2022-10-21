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
 * 青海省公安厅
 */
public class GatQinghaiAnalysis extends AbstractPubFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("青海省公安厅");
        regulatoryNotice.setProvince("青海省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements timeEl = document.getElementsByClass("pps");
        try {
            String text = timeEl.text();
            String time = null;
            Matcher matcher = localDatePattern.matcher(text);
            while (matcher.find()) {
                time = matcher.group(0);
            }
            LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }

    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("gjnrinfo");
    }
}
