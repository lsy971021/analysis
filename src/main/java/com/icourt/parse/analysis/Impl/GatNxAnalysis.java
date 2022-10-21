package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 *  宁夏回族自治区公安厅
 */
public class GatNxAnalysis extends AbstractPubFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("宁夏回族自治区公安厅");
        regulatoryNotice.setProvince("宁夏回族自治区");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
//        document
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return null;
    }
}
