package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 河北公安局
 */
public class GatAnalysis extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setProvince("河北省");
        regulatoryNotice.setNoticeOrgan("河北省公安厅");
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Elements elements = document.getElementsByClass("mod_font08_t  ");
        return elements.last();
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        Map<String, String> map = JSON.parseObject(extJson, Map.class);
        String title = map.get("title");
        regulatoryNotice.setTitle(title);
        String time = map.get("time");
        if (StringUtils.isNotBlank(time)) {
            try {
                int index = time.indexOf("[") + 1;
                time = time.substring(index).replace("]", "");
                LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
    }
}
