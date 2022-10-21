package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractMarketFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 新疆维吾尔自治区市场监督管理局
 */
public class ScjgjXinjiangAnalysis extends AbstractMarketFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setSourceUrl("http://scjgj.xinjiang.gov.cn/xjaic/sjsgscx/sgscx2.shtml");
        regulatoryNotice.setNoticeOrgan("新疆维吾尔自治区市场监督管理局");
        regulatoryNotice.setProvince("新疆维吾尔自治区");
        String value = noticeSource.getValue();
        Map<String, String> map = JSON.parseObject(value, Map.class);

        String entname = "行政处罚相对人";
        String name = "行政处罚相对人";
        String uniscid = "行政处罚相对人代码";
//        String inscercode = "行政处罚相对人代码";
        String lerep = "法定代表人（负责人）姓名";
        String pendecno = "处罚决定书文号";
        String illegacttype = "违法行为类型";
        String penbasis_cn = "处罚依据";
        String mainillegfact = "主要违法事实";
        String pentype_cn = "处罚种类";
        String pencontent = "处罚内容";
        String penam = "罚款金额";
        String forfam = "没收金额";
        String penauth_cn = "决定机关名称";
//        String datadept = "决定机关名称";
        String pendecissdate = "处罚决定日期";
        String penperi = "处罚有效期";
        String publicdate = "公示日期";
        String pubdeadline = "公示截止期";

        StringBuilder sb = new StringBuilder();
        sb.append(entname).append("：").append(String.valueOf(StringUtils.defaultString(map.get("entname")))).append("\n");
        sb.append(name).append("：").append(String.valueOf(StringUtils.defaultString(map.get("name")))).append("\n");
        sb.append(uniscid).append("：").append(String.valueOf(StringUtils.defaultString(map.get("uniscid")))).append("\n");
//        sb.append(inscercode).append("：").append(String.valueOf(StringUtils.defaultString(map.get("inscercode")))).append("\n");
        sb.append(lerep).append("：").append(String.valueOf(StringUtils.defaultString(map.get("lerep")))).append("\n");
        sb.append(pendecno).append("：").append(String.valueOf(StringUtils.defaultString(map.get("pendecno")))).append("\n");
        sb.append(illegacttype).append("：").append(String.valueOf(StringUtils.defaultString(map.get("illegacttype")))).append("\n");
        sb.append(penbasis_cn).append("：").append(String.valueOf(StringUtils.defaultString(map.get("penbasis_cn")))).append("\n");
        sb.append(mainillegfact).append("：").append(String.valueOf(StringUtils.defaultString(map.get("mainillegfact")))).append("\n");
        sb.append(pentype_cn).append("：").append(String.valueOf(StringUtils.defaultString(map.get("pentype_cn")))).append("\n");
        sb.append(pencontent).append("：").append(String.valueOf(StringUtils.defaultString(map.get("pencontent")))).append("\n");
        sb.append(penam).append("：").append(String.valueOf(StringUtils.defaultString(map.get("penam")))).append("\n");
        sb.append(forfam).append("：").append(String.valueOf(StringUtils.defaultString(map.get("forfam")))).append("\n");
        sb.append(penauth_cn).append("：").append(String.valueOf(StringUtils.defaultString(map.get("penauth_cn")))).append("\n");
//        sb.append(datadept).append("：").append(String.valueOf(StringUtils.defaultString(map.get("datadept")))).append("\n");
        sb.append(pendecissdate).append("：").append(String.valueOf(StringUtils.defaultString(map.get("pendecissdate")))).append("\n");
        sb.append(penperi).append("：").append(String.valueOf(StringUtils.defaultString(map.get("penperi")))).append("\n");
        sb.append(publicdate).append("：").append(String.valueOf(StringUtils.defaultString(map.get("publicdate")))).append("\n");
        sb.append(pubdeadline).append("：").append(String.valueOf(StringUtils.defaultString(map.get("pubdeadline")))).append("\n");

        regulatoryNotice.setContent(sb.toString());

        String startTitle = String.valueOf(map.get("entname"));
        if (StringUtils.isBlank(startTitle)) {
            startTitle = String.valueOf(map.get("lerep"));
        }
        String endTitle = String.valueOf(map.get("pendecno"));
        regulatoryNotice.setTitle(startTitle + endTitle);

        String time = String.valueOf(map.get("publicdate"));
        try {
            LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }

    }

    @Override
    public Element getContentElement(Document document, String url) {
        return null;
    }

    @Override
    public Boolean haveHtml() {
        return false;
    }
}
