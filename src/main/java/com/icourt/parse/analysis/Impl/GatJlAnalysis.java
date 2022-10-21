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
 * 吉林省公安厅
 */
public class GatJlAnalysis extends AbstractPubFilter {

    Pattern p = Pattern.compile("更新时间：(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{2}):(\\d{2}):(\\d{2})");

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("吉林省");
        regulatoryNotice.setNoticeOrgan("吉林省公安厅");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements elements = document.getElementsByClass("red_tit");
        if (elements.isEmpty())
            return;
        String title = elements.last().text();
        regulatoryNotice.setTitle(title);
        Elements left = document.getElementsByAttributeValue("align","center");
        if (left.isEmpty())
            return;
        String text = left.text();
        //更新时间：2022-04-06 11:26:00
        Matcher matcher = p.matcher(text);
        String time = null;
        while (matcher.find())
            time = matcher.group(0);
        if (time != null) {
            time = time.replace("更新时间：", "");
            LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter1);
            regulatoryNotice.setPublishTime(localDateTime);
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Elements elements = document.getElementsByClass("TRS_Editor");
        if (elements.isEmpty()) {
            return null;
        }
        return elements.last();
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }
}
