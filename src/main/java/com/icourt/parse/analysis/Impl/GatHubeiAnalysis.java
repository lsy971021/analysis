package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 湖北省公安厅
 */
public class GatHubeiAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("湖北省公安厅");
        regulatoryNotice.setProvince("湖北省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        try {
            String text = document.getElementsByClass("info").text();
            String time = null;
            Matcher matcher = localDateTimePattern1.matcher(text);
            if (matcher.find()) {
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
        return document.getElementById("article-box");
    }

    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        super.filterContent(regulatoryNotice);
        String content = regulatoryNotice.getContent();
        String newContent = content.replace("扫一扫在手机上查看当前页面", "");
        if (newContent.endsWith("附件:")) {
            newContent = newContent.replace("附件:", "");
        }
        regulatoryNotice.setContent(newContent);
    }
}
