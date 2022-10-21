package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 天津市公安局
 */
public class GaAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("天津市");
        regulatoryNotice.setNoticeOrgan("天津市公安局");
    }

    @Override
    public Element getContentElement(Document document, String url) {
        if (url.contains("https://ga.tj.gov.cn")) {
            Elements elements = document.getElementsByClass("article f14 mf26");
            if (!elements.isEmpty())
                return elements.get(0);
        } else if (url.contains("https://mp.weixin.qq.com"))
            return document.getElementById("js_content");
        return null;
    }


    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        Pattern timePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
        Map<String, String> map = JSON.parseObject(extJson, Map.class);
        String text = map.get("text");
        Matcher matcher = timePattern.matcher(text);
        String time = "";
        while (matcher.find()) {
            time = matcher.group(0);
        }
        if (time != null) {
            try {
                LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
        String title = text.replace(time, "").trim();
        regulatoryNotice.setTitle(title);
    }

    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        super.filterContent(regulatoryNotice);
        String content = regulatoryNotice.getContent();
        if (content == null)
            return;
        if (content.endsWith("附件：")) {
            content = content.substring(0, content.length() - 3);
            regulatoryNotice.setContent(content);
        }
    }
}
