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
 * 海南省市场监督管理局
 */
public class ArmHainanAnalysis extends AbstractMarketFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("海南省市场监督管理局");
        regulatoryNotice.setProvince("海南省");
        String value = noticeSource.getValue();
        Map<String, String> map = null;
        try {
            map = JSON.parseObject(value, Map.class);
        } catch (Exception e) {
            return;
        }
        if (map == null || map.isEmpty()) {
            return;
        }
        String companyname = "企业（商户）名称";
        String companysite = "注册地址";
        String companyman = "法定代表人姓名";
        String companymanid = "身份证号";
        String responsible_man = "负责人姓名";
        String resp_man_id = "身份证号";
        String direct_person = "直接责任人";
        String idcode = "社会信用代码";
        String toclassify = "案件分类";
        String losecase = "案件名称";
        String punish_writ_num = "行政处罚决定文书号";
        String losedetail = "主要违法事实";
        String punishway = "处罚依据和内容";
        String punishunit = "处罚机关";
        String punishtime = "处罚时间";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(companyname).append(":").append(StringUtils.defaultString(String.valueOf(map.get("companyname")))).append("\n");
        stringBuilder.append(companysite).append(":").append(StringUtils.defaultString(String.valueOf(map.get("companysite")))).append("\n");
        stringBuilder.append(companyman).append(":").append(StringUtils.defaultString(String.valueOf(map.get("companyman")))).append("\n");
        stringBuilder.append(companymanid).append(":").append(StringUtils.defaultString(String.valueOf(map.get("companymanid")))).append("\n");
        stringBuilder.append(responsible_man).append(":").append(StringUtils.defaultString(String.valueOf(map.get("responsible_man")))).append("\n");
        stringBuilder.append(resp_man_id).append(":").append(StringUtils.defaultString(String.valueOf(map.get("resp_man_id")))).append("\n");
        stringBuilder.append(direct_person).append(":").append(StringUtils.defaultString(String.valueOf(map.get("direct_person")))).append("\n");
        stringBuilder.append(idcode).append(":").append(StringUtils.defaultString(String.valueOf(map.get("idcode")))).append("\n");
        stringBuilder.append(toclassify).append(":").append(StringUtils.defaultString(String.valueOf(map.get("toclassify")))).append("\n");
        stringBuilder.append(losecase).append(":").append(StringUtils.defaultString(String.valueOf(map.get("losecase")))).append("\n");
        stringBuilder.append(punish_writ_num).append(":").append(StringUtils.defaultString(String.valueOf(map.get("punish_writ_num")))).append("\n");
        stringBuilder.append(losedetail).append(":").append(StringUtils.defaultString(String.valueOf(map.get("losedetail")))).append("\n");
        stringBuilder.append(punishway).append(":").append(StringUtils.defaultString(String.valueOf(map.get("punishway")))).append("\n");
        stringBuilder.append(punishunit).append(":").append(StringUtils.defaultString(String.valueOf(map.get("punishunit")))).append("\n");
        stringBuilder.append(punishtime).append(":").append(StringUtils.defaultString(String.valueOf(map.get("punishtime")))).append("\n");

        regulatoryNotice.setContent(stringBuilder.toString());

        String startTitle = StringUtils.defaultString(String.valueOf(map.get("losecase")));
        if (startTitle.isEmpty()) {
            startTitle = StringUtils.defaultString(String.valueOf(map.get("companyname")));
        }
        String endTitle = StringUtils.defaultString(String.valueOf(map.get("punish_writ_num")));
        regulatoryNotice.setTitle(startTitle + endTitle);

        String publishTime = String.valueOf(map.get("punishtime"));
        try {
            LocalDateTime localDateTime = LocalDate.parse(publishTime, dateFormatter3).atStartOfDay();
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
