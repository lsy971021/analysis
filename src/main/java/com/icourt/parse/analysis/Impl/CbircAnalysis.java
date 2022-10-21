package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPbcFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 银保监会
 */
public class CbircAnalysis extends AbstractPbcFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        String value = noticeSource.getValue();
        Map response = JSON.parseObject(value, Map.class);
        Object data = response.get("data");
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        if (data != null) {
            Map map = JSON.parseObject(data.toString(), Map.class);
            Object docClob = map.get("docClob");
            if (docClob != null)
                noticeSource.setHtml(docClob.toString());
            Object titleObj = map.get("docTitle");
            String title = null;
            if (titleObj != null)
                title = titleObj.toString().replace("\n", "");
            else
                title = map.get("docSubtitle") == null ? null : map.get("docSubtitle").toString().replace("\n", "");
            regulatoryNotice.setTitle(title);
            Object publishTimeObj = map.get("publishDate");
            LocalDateTime localDateTime = null;
            if (publishTimeObj != null) {
                String publishDate = publishTimeObj.toString();
                try {
                    localDateTime = LocalDateTime.parse(publishDate, dateTimeFormatter1);
                } catch (Exception e) {
                    try {
                        localDateTime = LocalDateTime.parse(publishDate, dateTimeSecondFormatter);
                    } catch (Exception ex) {
                    }
                }
            }
            regulatoryNotice.setPublishTime(localDateTime);
            Object docId = map.get("docId");
            if (docId == null)
                regulatoryNotice.setSourceUrl("http://www.cbirc.gov.cn/cn/view/pages/ItemList.html?itemPId=923&itemId=931&itemUrl=zhengwuxinxi/xingzhengchufa.html&itemName=行政处罚");
            else
                regulatoryNotice.setSourceUrl("https://www.cbirc.gov.cn/cn/view/pages/ItemDetail.html?docId=" + docId.toString());
            regulatoryNotice.setThemeLabels("行政处罚");

            Object docSource = map.get("docSource");
            if (docSource != null) {
                String province = docSource.toString();

                String s = provinceMap.get(province);
                if (StringUtils.isNotBlank(s))
                    regulatoryNotice.setProvince(s);
                else
                    regulatoryNotice.setProvince(province);
                regulatoryNotice.setNoticeOrgan("中国银行保险监督管理委员会" + province + "监管局");
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        if (document == null)
            return null;
        return document.body();
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }


    @Override
    public String mainBody() {
        return "中国银行保险监督管理委员会";
    }

    static Map<String, String> provinceMap = new HashMap<>();

    static {
        provinceMap.put("广东", "广东省");
        provinceMap.put("北京", "北京市");
        provinceMap.put("天津", "天津市");
        provinceMap.put("河北", "河北省");
        provinceMap.put("山西", "山西省");
        provinceMap.put("内蒙古", "内蒙古自治区");
        provinceMap.put("辽宁", "辽宁省");
        provinceMap.put("吉林", "吉林省");
        provinceMap.put("黑龙江", "黑龙江省");
        provinceMap.put("上海", "上海市");
        provinceMap.put("江苏", "江苏省");
        provinceMap.put("浙江", "浙江省");
        provinceMap.put("安徽", "安徽省");
        provinceMap.put("福建", "福建省");
        provinceMap.put("江西", "江西省");
        provinceMap.put("山东", "山东省");
        provinceMap.put("河南", "河南省");
        provinceMap.put("湖北", "湖北省");
        provinceMap.put("湖南", "湖南省");
        provinceMap.put("广西", "广西壮族自治区");
        provinceMap.put("海南", "海南省");
        provinceMap.put("重庆", "重庆市");
        provinceMap.put("四川", "四川省");
        provinceMap.put("贵州", "贵州省");
        provinceMap.put("云南", "云南省");
        provinceMap.put("西藏", "西藏自治区");
        provinceMap.put("陕西", "陕西省");
        provinceMap.put("甘肃", "甘肃省");
        provinceMap.put("青海", "青海省");
        provinceMap.put("宁夏", "宁夏省");
        provinceMap.put("新疆", "新疆维吾尔自治区");
    }
}
