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
 * 西藏自治区公安厅
 */
public class GatXizangAnalysis extends AbstractPubFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("西藏自治区公安厅");
        regulatoryNotice.setProvince("西藏自治区");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        try {
            String time = document.getElementsByClass("date lf").text();
            Matcher matcher = localDatePattern.matcher(time);
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
        return document.getElementsByClass("bd").first();
    }
}
