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
import java.util.regex.Pattern;

/**
 * 山东省公安厅
 */
public class GatShandongAnalysis extends AbstractPubFilter {

    Pattern timePattern = Pattern.compile("发布日期：(\\d{4})-(\\d{1,2})-(\\d{1,2})");

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("山东省");
        regulatoryNotice.setNoticeOrgan("山东省公安厅");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleEl = document.getElementById("title");
        if (titleEl!=null)
            regulatoryNotice.setTitle(titleEl.text().trim());
        Element timeEl = document.getElementById("product");
        if (timeEl!=null){
            try {
                String text = timeEl.text();
                Matcher matcher = timePattern.matcher(text);
                while (matcher.find()){
                    text = matcher.group(0).replace("发布日期：","");
                }
                LocalDateTime localDateTime = LocalDate.parse(text, dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }

        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("text");
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }
}
