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

/**
 * 辽宁省公安厅
 */
public class GatLnAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("辽宁省");
        regulatoryNotice.setNoticeOrgan("辽宁省公安厅");
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
        Map<String, String> map = JSON.parseObject(extJson, Map.class);
        String title = map.get("title");
        regulatoryNotice.setTitle(title);
        String publishTime = map.get("publishTime");
        try {
            LocalDateTime localDateTime = LocalDate.parse(publishTime, dateFormatter).atStartOfDay();
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
    }
}
