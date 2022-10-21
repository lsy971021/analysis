package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPbcFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

/**
 * 上海证券交易所
 */
public class SzseAnalysis extends AbstractPbcFilter {

    @Override
    public List<String> submitTaskPostProcessor(String mainJson) {
        List<String> list = super.submitTaskPostProcessor(mainJson);
        String value = list.get(0);
        List<Map> results = JSON.parseObject(value, List.class);
        String one = results.get(0).get("data").toString();
        return JSON.parseObject(one, List.class);
    }

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        String data = noticeSource.getValue();
        if (data == null)
            return;

        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        Map map = JSON.parseObject(data, Map.class);
        Object urlObj = map.get("ck");
        String url = null;
        try {
            if (urlObj != null) {
                String urlTag = urlObj.toString();
                Document document = Jsoup.parse(urlTag);
                url = document.getElementsByTag("a").get(0).attr("encode-open");
                url = "http://reportdocs.static.szse.cn" + url;
            }
        } catch (Exception e) {
        }

        regulatoryNotice.setSourceUrl(url);

        Object dateObj = map.get("xx_fwrq");
        if (dateObj != null) {
            try {
                String date = dateObj.toString();
                LocalDateTime localDateTime = LocalDateTime.of(LocalDate.parse(date, dateFormatter), LocalTime.MIN);
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
        Object titleObj = map.get("xx_bt");
        if (titleObj != null) {
            String title = titleObj.toString();
            regulatoryNotice.setTitle(title);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<a href=\"" + url + "\" class=\"cDownFile\">");
        stringBuilder.append(titleObj == null ? null : titleObj.toString());
        stringBuilder.append("</a>");
        regulatoryNotice.setContent(stringBuilder.toString());
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return null;
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
    }


    @Override
    public String mainBody() {
        return "深圳证券交易所";
    }
}
