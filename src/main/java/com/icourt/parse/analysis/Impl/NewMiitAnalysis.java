package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractMinistryFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 工信部新增类别5
 */
public class NewMiitAnalysis extends AbstractMinistryFilter {

    Pattern timePattern = Pattern.compile("发布时间：(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{1,2}):(\\d{1,2})");

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        String value = noticeSource.getValue();
        Document document = Jsoup.parse(value);
        Elements timeEl = document.getElementsByClass("cinfo center");
        String time = timeEl.text();
        Matcher matcher = timePattern.matcher(time);
        try {
            while (matcher.find()) {
                time = matcher.group(0);
            }
            String replace = time.replace("发布时间：", "");
            LocalDateTime localDateTime = LocalDateTime.parse(replace, dateTimeFormatter);
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
        Element titleEl = document.getElementById("con_title");
        regulatoryNotice.setTitle(titleEl.text().trim());

        String uri = "https://www.miit.gov.cn/%s.*";

        if (noticeSource.getUrl().matches(String.format(uri, "gyhxxhb/jgsj/cyzcyfgs")))
            regulatoryNotice.setNoticeOrgan("产业政策与法规司");
        else if (noticeSource.getUrl().matches(String.format(uri, "(jgsj/zbys/jxgy|jgsj/zbys/qcgy|jgsj/zbys/znzz)")))
            regulatoryNotice.setNoticeOrgan("装备工业一司");
        else if (noticeSource.getUrl().matches(String.format(uri, "(jgsj/txs/wjfb|jgsj/txs/txjs|jgsj/txs/wlfz|jgsj/txs/zcbz)")))
            regulatoryNotice.setNoticeOrgan("信息通信发展司");
        else if (noticeSource.getUrl().matches(String.format(uri, "(jgsj/xgj/gzzd|jgsj/xgj/wjfb|jgsj/xgj/ywzy|jgsj/xgj/scgl|jgsj/xgj/hlwgl|jgsj/xgj/fwjd)")))
            regulatoryNotice.setNoticeOrgan("信息通信管理局");
        else if (noticeSource.getUrl().matches(String.format(uri, "(jgsj/wgj/flfg|jgsj/wgj/bmgz|jgsj/wgj/wjfb|gyhxxhb/jgsj/wxdgljgjwxdbgs)")))
            regulatoryNotice.setNoticeOrgan("无线电管理局");
        else
            regulatoryNotice.setNoticeOrgan("信息技术发展司");
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.getElementById("con_con");
    }

}
