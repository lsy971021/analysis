package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPbcFilter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 上海证券交易所
 */
public class SseAnalysis extends AbstractPbcFilter {

    @Override
    public List<String> submitTaskPostProcessor(String mainJson) {
        List<String> list = super.submitTaskPostProcessor(mainJson);
        String result = list.get(0);
        String replace = result.replace("jsonpCallback34826053(", "");
        String substring = replace.substring(0, replace.length() - 1);
        Object read = null;
        try {
            read = JSONPath.read(substring, "$.pageHelp.data");
            List<String> data = JSON.parseObject(read.toString(), List.class);
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();

        String value = noticeSource.getValue();
        Map map = JSON.parseObject(value, Map.class);
        if (!CollectionUtils.isEmpty(map)){
            Object titleObj = map.get("docTitle");
            if (titleObj!=null)
                regulatoryNotice.setTitle(titleObj.toString());
            Object urlObj = map.get("docURL");
            if (urlObj!=null) {
                regulatoryNotice.setSourceUrl(urlObj.toString());
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("<a href=\"" + urlObj.toString() + "\" class=\"cDownFile\">");
                stringBuilder.append(titleObj == null ? null : titleObj.toString());
                stringBuilder.append("</a>");
                regulatoryNotice.setContent(stringBuilder.toString());
            }
            Object dateObj = map.get("createTime");
            if (dateObj!=null){
                try {
                    LocalDateTime localDateTime = LocalDateTime.parse(dateObj.toString(), dateTimeFormatter1);
                    regulatoryNotice.setPublishTime(localDateTime);
                } catch (Exception e) {
                }
            }
        }
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
        return "上海证券交易所";
    }
}
