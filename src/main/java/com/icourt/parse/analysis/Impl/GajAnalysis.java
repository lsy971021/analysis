package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.Analysis;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 行政处罚-北京公安局。待解析
 */
public class GajAnalysis implements Analysis {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("mainText");
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        regulatoryNotice.setProvince("北京市");
        Object read = JSONPath.read(extJson, "$.text");
        if (read == null)
            return;
        String data = read.toString();
        int index = data.indexOf("公示") + 2;
        String title = data.substring(0, index);
        regulatoryNotice.setTitle(title);
        String time = data.substring(index);
        try {
            LocalDateTime localDateTime = LocalDateTime.of(LocalDate.parse(time, dateFormatter), LocalTime.MIN);
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
    }

    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        Analysis.super.filterContent(regulatoryNotice);
        regulatoryNotice.setContent(regulatoryNotice.getContent().replace("扫一扫在手机打开当前页", ""));
    }

    @Override
    public String mainBody() {
        return null;
    }
}
