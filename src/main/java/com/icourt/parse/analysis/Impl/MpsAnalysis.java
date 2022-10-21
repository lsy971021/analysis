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
 * 网络违法犯罪举报网站
 */
public class MpsAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("全国");
        regulatoryNotice.setNoticeOrgan("网络违法犯罪举报网站");
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Elements elements = document.getElementsByClass("Section0");
        if (elements.isEmpty())
            elements = document.getElementsByClass("neirongzhengwen");
        return elements.get(0);
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        Map<String,String> map = JSON.parseObject(extJson, Map.class);
        String title = map.get("title");
        regulatoryNotice.setTitle(title);
        String time = map.get("publishTime");
        String trim = time.replace("[", "").replace("]","").trim();
        try {
            LocalDate parse = LocalDate.parse(trim, dateFormatter);
            LocalDateTime localDateTime = parse.atStartOfDay();
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
    }
}
