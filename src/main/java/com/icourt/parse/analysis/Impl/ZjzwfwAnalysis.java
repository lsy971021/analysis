package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractMarketFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * 浙江省市场监督管理局
 */
public class ZjzwfwAnalysis extends AbstractMarketFilter {

    @Override
    public List<String> submitTaskPostProcessor(String mainJson) {
        List<String> list = new ArrayList<>();
        String value = null;
        //存在html时
        if (StringUtils.isNotBlank(mainJson)) {
            Object data = JSONPath.read(mainJson, "$main.text");
            if (data == null) {
                return null;
            }
            value = data.toString();
        }
        list.add(value);
        return list;
    }

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        String thirdId = noticeSource.getThirdId();
        if (thirdId.contains("_")) {
            int index = thirdId.indexOf("_") + 1;
            noticeSource.setThirdId(thirdId.substring(index));
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        Elements style = document.getElementsByAttributeValue("style", "padding:0 30px 30px 30px;");
        if (style.isEmpty())
            return null;
        Element element = style.get(0);

        if (element == null)
            return null;
        Elements table = element.getElementsByTag("table");
        if (table.isEmpty())
            return null;
        table.get(0).remove();
        return element;
    }

    @Override
    public void filterTitle(RegulatoryNotice regulatoryNotice) {
        super.filterTitle(regulatoryNotice);
    }

    
    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        String content = regulatoryNotice.getContent();
        if (content==null)
            return;

        super.filterContent(regulatoryNotice);
    }


    @Override
    public void fillInfo(RegulatoryNotice regulatoryNotice) {
        regulatoryNotice.setNoticeOrgan("浙江省市场监督管理局");
        regulatoryNotice.setProvince("浙江省");
        String content = regulatoryNotice.getContent();
        if (content == null)
            return;
        String[] split = content.split("\n");
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = true;
        boolean has = true;
        for (int i = 0; i < split.length; i++) {
            String row = split[i];
            if (row.matches(".*<img src=.*class=\"cDownFile\">.*"))
                continue;
            //标题
            if (flag && row.matches(".*[\\u4e00-\\u9fa5].*")) {
                flag = false;
                String title = Jsoup.parse(row).text();
                regulatoryNotice.setTitle(title);
            } else {
                Matcher matcher = localDatePattern.matcher(row);
                String time = null;
                while (matcher.find()) {
                    time = matcher.group(0);
                }
                if (time != null) {
                    LocalDateTime localDateTime = null;
                    try {
                        localDateTime = LocalDateTime.of(LocalDate.parse(time, dateFormatter), LocalTime.MIN);
                    } catch (Exception e) {
                    }
                    if (has && row.contains("公开时间")) {
                        has = false;
                        regulatoryNotice.setPublishTime(localDateTime);
                    } else if (has)
                        regulatoryNotice.setPublishTime(localDateTime);
                }
            }
            stringBuilder.append("\n").append(row);
        }
        String substring = stringBuilder.substring(1);
        regulatoryNotice.setContent(substring);
    }

}
