package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractHealthFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 *  福建省卫生健康委员会
 */
public class WjwFujianAnalysis extends AbstractHealthFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("福建省卫生健康委员会");
        regulatoryNotice.setProvince("福建省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleInfo = document.getElementsByClass("smgb-xl-tit").first();
        Element titleEl = titleInfo.getElementsByTag("h2").first();
        String title = titleEl.text().trim();
        regulatoryNotice.setTitle(title);
        Elements timeEls = document.getElementsByClass("xl_tit2_l");
        String time = null;
        try {
            Matcher matcher = localDateTimePattern1.matcher(timeEls.text());
            while (matcher.find()) {
                time = matcher.group(0);
            }
            if (time!=null){
                LocalDateTime localDateTime = LocalDateTime.parse(time, dateTimeFormatter);
                regulatoryNotice.setPublishTime(localDateTime);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementsByClass("TRS_Editor").first();
    }

}
