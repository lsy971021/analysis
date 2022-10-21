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
 * 海南省公安厅
 */
public class GaHainanAnalysis extends AbstractPubFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("海南省");
        regulatoryNotice.setNoticeOrgan("海南省公安厅");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleEl = document.getElementsByClass("title_cen mar-t2 text").first();
        String title = titleEl.text().trim();
        regulatoryNotice.setTitle(title);
        Elements timeEls = document.getElementsByClass("line mar-t2 con_div");
        String text = timeEls.text();
        Matcher matcher = localDateTimePattern1.matcher(text);
        String time = null;
        while (matcher.find()) {
            time = matcher.group(0);
        }
        if (time!=null){
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("zoom");
    }

    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        super.filterContent(regulatoryNotice);
    }

}
