package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPubFilter;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Matcher;

/**
 * 国家移动互联网应用安全管理中心
 */
public class CnaacAnalysis2 extends AbstractPubFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("国家移动互联网应用安全管理中心");
        regulatoryNotice.setThemeLabels("APP通报");
        String value = noticeSource.getValue();
        if (StringUtils.isBlank(value))
            return;
        Object appName = JSONPath.read(value, "$.name");
        Object appVersion = JSONPath.read(value, "$.appVersion");
        Object benavior = JSONPath.read(value, "$.benavior");
        Object md5 = JSONPath.read(value, "$.md5");
        Object appStore = JSONPath.read(value, "$.appStore.name");
        Object createDate = JSONPath.read(value, "$.createDate");
        try {
            if (createDate != null) {
                String date = createDate.toString();
                String time = null;
                Matcher matcher = localDatePattern.matcher(date);
                while (matcher.find()) {
                    time = matcher.group(0);
                }
                if (time!=null) {
                    LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
                    regulatoryNotice.setPublishTime(localDateTime);
                }
                String replace = date.substring(0, 10).replaceFirst("-", "年").replaceFirst("-", "月") + "日";
                regulatoryNotice.setTitle("国家移动互联网应用安全管理中心应用通报" + replace);
            }
        } catch (Exception e) {
        }
        StringBuilder stringBuilder = new StringBuilder();
        String text = "<table>\n" +
                "    <thead>\n" +
                "    <tr>\n" +
                "        <th>序号</th>\n" +
                "        <th>应用名称</th>\n" +
                "        <th>版本</th>\n" +
                "        <th>违法类型</th>\n" +
                "        <th>MD5</th>\n" +
                "        <th>商店名称</th>\n" +
                "        <th>发现日期</th>\n" +
                "    </tr>\n" +
                "    </thead>\n" +
                "    <tbody id=\"appData\">\n" +
                "    </tbody>\n" +
                "</table>";
        Document document = Jsoup.parse(text);
        Element element = document.getElementById("appData");

        stringBuilder.append("<tr>").append("\n");
        stringBuilder.append("<td>").append(1).append("</td>").append("\n");
        stringBuilder.append("<td>").append(appName).append("</td>").append("\n");
        stringBuilder.append("<td>").append(appVersion).append("</td>").append("\n");
        stringBuilder.append("<td>").append(benavior).append("</td>").append("\n");
        stringBuilder.append("<td>").append(md5).append("</td>").append("\n");
        stringBuilder.append("<td>").append(appStore).append("</td>").append("\n");
        stringBuilder.append("<td>").append(createDate).append("</td>").append("\n");
        stringBuilder.append("</tr>");
        element.append(stringBuilder.toString());
        regulatoryNotice.setComment(stringBuilder.toString());
        regulatoryNotice.setContent(document.body().outerHtml());
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return null;
    }

    @Override
    public void filterTitle(RegulatoryNotice regulatoryNotice) {
        regulatoryNotice.setType(0);
    }

    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
    }

    @Override
    public Boolean haveHtml() {
        return false;
    }
}
