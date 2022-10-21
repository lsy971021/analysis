package com.icourt.parse.analysis.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.dsmanager.util.PdfUtil;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractMarketFilter;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

/**
 * 中国市场监管行政处罚文书网
 */
public class CfwsAnalysis extends AbstractMarketFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("中国市场监管行政处罚文书网");
        String value = noticeSource.getValue();
        //标题
        try {
            String title = JSONPath.read(value, "$result.i0").toString();
            regulatoryNotice.setTitle(title);
        } catch (Exception e) {
        }
        //发布时间
        try {
            String time = JSONPath.read(value, "$result.i1").toString();
            //2020年05月06日
            LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter1).atStartOfDay();
            regulatoryNotice.setPublishTime(localDateTime);
        } catch (Exception e) {
        }
        //正文
        try {
            Function<String, String> function = str -> str.replaceAll("^第(\\S*)页.*|^本文书一式.*|^\\d{1,2}$|^本文书采用公告送达.*", "");
            String pdfString = JSONPath.read(value, "$result.i7").toString();
            String text = PdfUtil.readByString(pdfString, function);
            regulatoryNotice.setContent(text);
        } catch (Exception e) {
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return null;
    }

    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        Map<String, String> map = JSON.parseObject(extJson, Map.class);
        if (!CollectionUtils.isEmpty(map)) {
            if (StringUtils.isEmpty(regulatoryNotice.getTitle())) {
                String title = map.get("doc_no");
                regulatoryNotice.setTitle(title);
            }
            if (regulatoryNotice.getPublishTime() == null) {
                String time = map.get("pun_date");
                try {
                    LocalDateTime localDateTime = LocalDate.parse(time, dateFormatter).atStartOfDay();
                    regulatoryNotice.setPublishTime(localDateTime);
                } catch (Exception e) {
                }
            }
        }

        String area = map.get("area_info");
        for (int i = 0; i < allProvince.length; i++) {
            String province = allProvince[i];
            if (area.contains(province)) {
                regulatoryNotice.setProvince(province);
                break;
            }
        }
    }

    @Override
    public Boolean haveHtml() {
        return false;
    }
}
