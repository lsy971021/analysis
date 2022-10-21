package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractConsumerFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 甘肃省消费者委员会
 */
public class GscomAnalysis extends AbstractConsumerFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("甘肃省消费者委员会");
        regulatoryNotice.setProvince("甘肃省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleEl = document.getElementById("tititlec");
        String title = titleEl.text().trim();
        regulatoryNotice.setTitle(title);
        Element timeEl = document.getElementById("dhc");
        String time = null;
        try {
            Matcher matcher = localDatePattern.matcher(timeEl.text());
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
        return document.getElementById("showcontent");
    }
}
