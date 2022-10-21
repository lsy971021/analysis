package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 国家计算机病毒应急处理中心
 */
public class CnaacFilter extends AbstractPubFilter {

    @Override
    public List<String> submitTaskPostProcessor(String mainJson) {
        List<String> list = super.submitTaskPostProcessor(mainJson);
        String value = list.get(0);
        String result = JSONPath.read(value, "$.resultMap.resultList").toString();
        List parseObject = JSON.parseObject(result, List.class);
        return parseObject;
    }

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        String uri = "https://www.cnaac.org.cn/newShowData.html?id=";
        String value = noticeSource.getValue();
        Map<String,String> map = JSON.parseObject(value, Map.class);
        String id = String.valueOf(map.get("id"));
        regulatoryNotice.setSourceUrl(uri+id);
        String title = map.get("title");
        if (StringUtils.isBlank(title))
            title = map.get("subTitle");
        regulatoryNotice.setTitle(title);
        String content = map.get("content");
        noticeSource.setHtml(content);
        regulatoryNotice.setProvince("全国");
        String createDate = map.get("createDate");
        LocalDateTime localDateTime = null;
        try {
            localDateTime = LocalDateTime.parse(createDate, dateTimeFormatter1);
        } catch (Exception e) {
        }
        regulatoryNotice.setPublishTime(localDateTime);
        regulatoryNotice.setNoticeOrgan("国家计算机病毒应急处理中心");
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.body();
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }
}
