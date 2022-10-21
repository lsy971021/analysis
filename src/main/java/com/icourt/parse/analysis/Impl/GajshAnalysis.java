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
 * 上海市公安局
 */
public class GajshAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("上海市");
        regulatoryNotice.setNoticeOrgan("上海市公安局");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleElement = document.getElementById("ivs_title");
        if (titleElement != null) {
            String title = titleElement.text().trim();
            regulatoryNotice.setTitle(title);
        }
        Element dateElement = document.getElementById("ivs_date");
        if (dateElement != null) {
            try {
                String time = dateElement.text();
                Matcher matcher = localDatePattern.matcher(time);
                while (matcher.find()) {
                    time = matcher.group(0);
                }
                LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("content");
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }

}
