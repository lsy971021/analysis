package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSONObject;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPbcFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * 国家互联网信息办公室
 */
public class CacAnalysis extends AbstractPbcFilter {

    //某行的值等于任意一个，则删除该行
    final String[] contentFilter = new String[]{"【纠错】", "来源："};


    @Override
    public void preProcessor(NoticeSource noticeSource) {

    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("center");
    }


    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        if (extJson.isEmpty())
            return;
        HashMap hashMap = JSONObject.parseObject(extJson, HashMap.class);
        Object titleObj = hashMap.get("topic");
        Object provinceObj = hashMap.get("province");
        Object publishTimeObj = hashMap.get("pubtime");
        if (titleObj != null)
            regulatoryNotice.setTitle(titleObj.toString());
        if (provinceObj != null)
            regulatoryNotice.setProvince(provinceObj.toString());
        if (publishTimeObj != null) {
            try {
                LocalDateTime parse = LocalDateTime.parse(publishTimeObj.toString(), dateTimeSecondFormatter);
                regulatoryNotice.setPublishTime(parse);
            } catch (Exception e) {
            }
        }
    }


    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        String content = regulatoryNotice.getContent();
        if (StringUtils.isBlank(content))
            return;
        String[] split = content.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String row = split[i];
            if (StringUtils.equalsAny(row, contentFilter))
                continue;
            stringBuilder.append(row).append("\n");
        }
        String newContent = stringBuilder.toString();
        regulatoryNotice.setContent(newContent);
        super.filterContent(regulatoryNotice);
    }


    @Override
    public String mainBody() {
        return "国家互联网信息办公室";
    }

    @Override
    public String DealContentUrl(String url) {
        if (url.equals("http://www.cac.gov.cn"))
            return null;
        return url;
    }
}
