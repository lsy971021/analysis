package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.Analysis;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

/**
 * 行政处罚-北京市民政局 待解析
 */
public class GgfwAnalysis implements Analysis {

    @Override
    public List<String> submitTaskPostProcessor(String mainJson) {
        List<String> mainJsons = Analysis.super.submitTaskPostProcessor(mainJson);
        String response = mainJsons.get(0);
        Document document = Jsoup.parse(response);
        Element body = document.body();
        String data = body.text();
        Object read = JSONPath.read(data, "$.rtData");
        if (read == null)
            return null;
        List<String> list = JSON.parseObject(read.toString(), List.class);
        return list;
    }

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        String value = noticeSource.getValue();
        Map<String, String> map = JSON.parseObject(value, Map.class);
        String title = map.get("partyName");
        regulatoryNotice.setTitle(title);
        regulatoryNotice.setProvince("北京市");
        regulatoryNotice.setThemeLabels("行政处罚");
        String publishTimeStr = String.valueOf(map.get("transferTime"));
        String time = String.valueOf(map.get("adminSanctionDate"));
        LocalDateTime publishTime = null;
        String timeStr = null;
        String adminSanctionDate = null;
        try {
            publishTime = LocalDateTime.ofEpochSecond(Long.valueOf(publishTimeStr) / 1000L, 0, ZoneOffset.ofHours(8));
            LocalDateTime localDateTime = LocalDateTime.ofEpochSecond(Long.valueOf(time) / 1000L, 0, ZoneOffset.ofHours(8));
            adminSanctionDate = dateFormatter.format(localDateTime);
            timeStr = dateFormatter.format(publishTime);
            regulatoryNotice.setPublishTime(publishTime);
        } catch (NumberFormatException e) {
        }
        //content
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("公示日期:");
        stringBuilder.append(timeStr).append("\n");
        stringBuilder.append("执法机构:");
        stringBuilder.append("北京市民政综合执法监察大队").append("\n");
        stringBuilder.append("处罚决定文书号:");
        stringBuilder.append(map.get("punishDocNum")).append("\n");
        stringBuilder.append("被处罚人名称（姓名）:");
        stringBuilder.append(map.get("partyName")).append("\n");
        stringBuilder.append("法人或负责人姓名:");
        stringBuilder.append(map.get("legalName")).append("\n");
        stringBuilder.append("社会统一信用代码:");
        stringBuilder.append(map.get("organCode")).append("\n");
        stringBuilder.append("处罚依据:");
        stringBuilder.append(map.get("illegalClause")).append("\n");
        stringBuilder.append("处罚决定内容:");
        stringBuilder.append(map.get("administrativePunish")).append("\n");
        stringBuilder.append("做出处罚决定机关名称:");
        stringBuilder.append(map.get("assignmentDeptName")).append("\n");
        stringBuilder.append("主要违法事实:");
        stringBuilder.append(map.get("illegalAct")).append("\n");
        stringBuilder.append("处罚日期:");
        stringBuilder.append(adminSanctionDate);
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
        return null;
    }

    @Override
    public Boolean haveHtml() {
        return false;
    }
}
