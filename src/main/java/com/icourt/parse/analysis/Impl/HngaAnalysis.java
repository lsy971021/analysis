package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 河南省公安厅
 */
public class HngaAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("河南省公安厅");
        regulatoryNotice.setProvince("河南省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        try {
            Element timeEl = document.getElementById("pubDate");
            String text = timeEl.text();
            Matcher matcher = localDatePattern.matcher(text);
            while (matcher.find()){
                text = matcher.group(0);
            }
            LocalDateTime localDateTime = LocalDate.parse(text, dateFormatter).atStartOfDay();
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("content");
    }

}
