package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSONPath;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractMinistryFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 中华人民共和国工业和信息化部
 */
public class MiitAnalysis extends AbstractMinistryFilter {

    //正文过滤
    final String[] contentFilter = new String[]{"扫一扫在手机打开当前页", "用微信“扫一扫”，点击右上角分享按钮", "用微信“扫一扫”", "分享到：", "分享"};

    final static Map<String, String> provincesMap = new HashMap<>();


    @Override
    public void preProcessor(NoticeSource noticeSource) {

    }

    /**
     * 根据Document获取包含正文的element
     *
     * @param document
     * @return
     */
    public Element getContentElement(Document document, String url) {
        if (document == null)
            return null;
        String id = "article";
        Element element = document.getElementById(id);
        if (null == element) {
            //需要特殊处理
            Map<String, String> map = new HashMap<>();
            map.put("www", "w980 center cmain");
            map.put("hljca", "xxgk-wzy");
            String tap = null;
            try {
                String region = url.substring(8, url.indexOf("."));
                tap = map.get(region);
            } catch (Exception e) {
                return null;
            }
            if (tap == null)
                return null;
            Elements elementsByClass = document.getElementsByClass(tap);
            if (elementsByClass != null && !elementsByClass.isEmpty())
                element = elementsByClass.get(0);
            else
                return null;
        }
        return element;
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        //标题
        Object title = JSONPath.read(extJson, "title");
        regulatoryNotice.setTitle(title.toString());
        //省份
        Object province = JSONPath.read(extJson, "province");
        regulatoryNotice.setProvince(province.toString());

        if (regulatoryNotice.getPublishTime() != null)
            return;
        //发布时间  2022-07-21
        Object publishTime = JSONPath.read(extJson, "publishTime");
        LocalDate parse = null;
        if (publishTime != null) {
            try {
                parse = LocalDate.parse(publishTime.toString(), dateFormatter);
                regulatoryNotice.setPublishTime(parse.atStartOfDay());
            } catch (Exception e) {
            }
        }
    }


    Pattern timePattern = Pattern.compile("发布时间：(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{1,2}):(\\d{1,2})");

    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        String content = regulatoryNotice.getContent();
        Matcher matcher = timePattern.matcher(content);
        String time = null;
        if (matcher.find()) {
            time = matcher.group(0);
        }
        if (time != null) {
            try {
                LocalDateTime localDateTime = LocalDateTime.parse(time.replace("发布时间：", ""), dateTimeFormatter);
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }

        if (StringUtils.isBlank(content))
            return;
        String[] split = content.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String s = split[i].replace(" ", "");
            if (s.isEmpty())
                continue;
            if (StringUtils.containsAny(s, contentFilter)) {
                //去除正文这些地方的后面部分
                regulatoryNotice.setContent(stringBuilder.toString());
                break;
            }
            stringBuilder.append(s).append("\n");
        }
        super.filterContent(regulatoryNotice);
    }


    @Override
    public void fillInfo(RegulatoryNotice regulatoryNotice) {
        String content = regulatoryNotice.getContent();
        String[] split = content.split("\n");
        //默认正文第一行为title
        String title = split[0];

        regulatoryNotice.setTitle(title);
        //默认第二行为发布时间
        String time = split[1];
        if (time.contains("发布时间：")) {
            try {
                String dateTime = time.substring(time.indexOf("：") + 1);
                LocalDateTime publishTime = LocalDateTime.parse(dateTime, dateTimeFormatter);
                regulatoryNotice.setPublishTime(publishTime);
                //获取省份信息
                String url = regulatoryNotice.getSourceUrl();
                String region = url.substring(8, url.indexOf("."));
                String province = provincesMap.get(region);
                regulatoryNotice.setProvince(province);
            } catch (Exception e) {
            }
        }
    }


    static {
        provincesMap.put("www", "全国");
        provincesMap.put("bjca", "北京市");
        provincesMap.put("tjca", "天津市");
        provincesMap.put("gdca", "广东省");
        provincesMap.put("hbca", "河北省");
        provincesMap.put("sxca", "山西省");
        provincesMap.put("nmca", "内蒙古自治区");
        provincesMap.put("lnca", "辽宁省");
        provincesMap.put("jlca", "吉林省");
        provincesMap.put("hljca", "黑龙江省");
        provincesMap.put("shca", "上海市");
        provincesMap.put("jsca", "江苏省");
        provincesMap.put("zjca", "浙江省");
        provincesMap.put("ahca", "安徽省");
        provincesMap.put("fjca", "福建省");
        provincesMap.put("jxca", "江西省");
        provincesMap.put("sdca", "山东省");
        provincesMap.put("hca", "河南省");
        provincesMap.put("hubca", "湖北省");
        provincesMap.put("hunca", "湖南省");
        provincesMap.put("gxca", "广西壮族自治区");
        provincesMap.put("hnca", "海南省");
        provincesMap.put("cqca", "重庆市");
        provincesMap.put("scca", "四川省");
        provincesMap.put("gzca", "贵州省");
        provincesMap.put("ynca", "云南省");
        provincesMap.put("xzca", "西藏自治区");
        provincesMap.put("shxca", "陕西省");
        provincesMap.put("gsca", "甘肃省");
        provincesMap.put("qhca", "青海省");
        provincesMap.put("nxca", "宁夏省");
        provincesMap.put("xjca", "新疆维吾尔自治区");
    }
}
