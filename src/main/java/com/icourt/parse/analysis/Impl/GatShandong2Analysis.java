package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 山东省公安厅2
 * http://60.208.61.172:8081/ZFGK/page?type=5&fw=1#
 */
public class GatShandong2Analysis extends AbstractPubFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("山东省");
        regulatoryNotice.setNoticeOrgan("山东省公安厅");
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Element titleEl = document.getElementsByClass("hot1").first();
        Element title2El = document.getElementsByClass("hot2").first();
        Element title3El = document.getElementsByClass("hot3").first();
        String title = titleEl.text().trim();
        String title2 = title2El.text().trim();
        String title3 = title3El.text().trim();
        regulatoryNotice.setTitle(title + title2 + title3);
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementsByClass("maon").last();
    }
}
