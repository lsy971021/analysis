package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractConsumerFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 海南省消费者委员会
 */
public class HnnetAnalysis extends AbstractConsumerFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("海南省消费者委员会");
        regulatoryNotice.setProvince("海南省");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements titleEls = document.getElementsByClass("content");
        String title = titleEls.first().getElementsByTag("h3").first().text();
        regulatoryNotice.setTitle(title.trim());
        Elements timeEls = document.getElementsByClass("shij");
        String time = null;
        try {
            Matcher matcher = localDateTimePattern.matcher(timeEls.text());
            while (matcher.find()) {
                time = matcher.group(0);
            }
            if (time != null) {
                String[] split = time.split("-");
                StringBuilder stringBuilder = new StringBuilder();
                String month = split[1];
                stringBuilder.append(split[0]).append("-");
                if (month.length() > 1)
                    stringBuilder.append(month).append("-");
                else
                    stringBuilder.append(0).append(month).append("-");
                String[] dayInfo = split[2].split(" ");
                String day = dayInfo[0];
                if (day.length() > 1)
                    stringBuilder.append(day).append(" ");
                else
                    stringBuilder.append(0).append(day).append(" ");
                String[] timeInfo = dayInfo[1].split(":");
                for (int i = 0; i < timeInfo.length; i++) {
                    String s = timeInfo[i];
                    if (s.length() > 1)
                        stringBuilder.append(s);
                    else
                        stringBuilder.append(0).append(s);
                    if (timeInfo.length - 1 != i)
                        stringBuilder.append(":");
                }
                LocalDateTime localDateTime = LocalDateTime.parse(stringBuilder.toString(), dateTimeFormatter1);
                regulatoryNotice.setPublishTime(localDateTime);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Element content = document.getElementsByClass("content").first();
        Elements timeEls = content.getElementsByClass("shij");
        timeEls.remove();
        content.getElementsByTag("h3").first().remove();
        return content;
    }
}
