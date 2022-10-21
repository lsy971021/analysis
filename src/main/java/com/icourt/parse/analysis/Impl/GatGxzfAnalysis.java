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
 * 广西壮族自治区公安厅
 */
public class GatGxzfAnalysis extends AbstractPubFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("广西壮族自治区公安厅");
        regulatoryNotice.setProvince("广西壮族自治区");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements elements = document.getElementsByClass("article");
        Element first = elements.first();
        Elements h1 = first.getElementsByTag("h1");
        String title = h1.text().trim();
        regulatoryNotice.setTitle(title);
        Elements timeEl = first.getElementsByClass("article-inf-left");
        try {
            String text = timeEl.text();
            Matcher matcher = localDateTimePattern1.matcher(text);
            String time = null;
            while (matcher.find()) {
                time = matcher.group(0);
            }
            LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementsByClass("article-con").first();
    }

    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        super.filterContent(regulatoryNotice);
        String content = regulatoryNotice.getContent();
        String newContent = content;
        if (content.endsWith("文件下载：\n" +
                "关联文件：")) {
            newContent = content.replace("文件下载：\n" +
                    "关联文件：", "");
        }
        regulatoryNotice.setContent(newContent);
    }

}
