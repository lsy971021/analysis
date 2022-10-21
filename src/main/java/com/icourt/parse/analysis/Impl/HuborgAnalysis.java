package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractConsumerFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 湖北省消费者委员会
 */
public class HuborgAnalysis extends AbstractConsumerFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("湖北省消费者委员会");
        regulatoryNotice.setProvince("湖北省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element info = document.getElementsByClass("left-content").first();
        Element titleEl = info.getElementsByTag("h1").first();
        String title = titleEl.text().trim();
        regulatoryNotice.setTitle(title);
        Elements timeEls = document.getElementsByClass("info");
        String time = null;
        try {
            Matcher matcher = localDatePattern.matcher(timeEls.text());
            while (matcher.find()) {
                time = matcher.group(0);
            }
            if (time!=null){
                LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Element element = document.getElementsByClass("left-content").first();
        return element.getElementsByClass("text").first();
    }
}
