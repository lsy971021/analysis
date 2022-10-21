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
 * 北京市市场监督管理局
 */
public class ScjgjBeijingAnalysis extends AbstractMarketFilter {
    @Override
    public void preProcessor(NoticeSource noticeSource) {
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setNoticeOrgan("北京市市场监督管理局");
        regulatoryNotice.setProvince("北京市");
        String value = noticeSource.getValue();
        if (value == null)
            return;
        Map<String, String> map = JSON.parseObject(value, Map.class);
        StringBuilder stringBuilder = new StringBuilder();
        String CF_WSH = "行政处罚决定书文号";
        String CF_XDR_MC = "行政相对人名称";
        String CF_XDR_LB = "行政相对人类别";
        String CF_XDR_SHXYM = "行政相对人代码-统一社会信用代码";
        String CF_XDR_GSZC = "行政相对人代码-工商注册号";
        String CF_XDR_ZZJG = "行政相对人代码-组织机构代码";
        String CF_XDR_SWDJ = "行政相对人代码-税务登记号";
        String CF_XDR_SYDW = "行政相对人代码-事业单位证书号";
        String CF_XDR_SHZZ = "行政相对人代码-社会组织登记证号";
        String CF_XDR_ZJHM = "行政相对人代码-相对人证件号码";
        String CF_FRDB = "法定代表人";
        String CF_WFXW = "违法行为类型";
        String CF_SY = "违法事实";
        String CF_YJ = "处罚依据";
        String CF_CFLB = "处罚类别";
        String CF_NR = "处罚内容:";
        String CF_NR_FK = "罚款金额（万元）";
        String CF_NR_WFFF = "没收违法所得、没收非法财物的金额（万元）";
        String CF_NR_ZKDX = "暂扣或吊销证照名称及编号";
        String CF_JDRQ = "处罚决定日期";
        String CF_YXQ = "处罚有效期";
        String CF_GSJZQ = "公示截止期";
        String CF_CFJG = "处罚机关";
        String BZ = "备注";
        stringBuilder.append(CF_WSH).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_WSH")))).append("\n");
        stringBuilder.append(CF_XDR_MC).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_MC")))).append("\n");
        stringBuilder.append(CF_XDR_LB).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_LB")))).append("\n");
        stringBuilder.append(CF_XDR_SHXYM).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_SHXYM")))).append("\n");
        stringBuilder.append(CF_XDR_GSZC).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_GSZC")))).append("\n");
        stringBuilder.append(CF_XDR_ZZJG).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_ZZJG")))).append("\n");
        stringBuilder.append(CF_XDR_SWDJ).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_SWDJ")))).append("\n");
        stringBuilder.append(CF_XDR_SYDW).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_SYDW")))).append("\n");
        stringBuilder.append(CF_XDR_SHZZ).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_SHZZ")))).append("\n");
        stringBuilder.append(CF_XDR_ZJHM).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_XDR_ZJHM")))).append("\n");
        stringBuilder.append(CF_FRDB).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_FRDB")))).append("\n");
        stringBuilder.append(CF_WFXW).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_WFXW")))).append("\n");
        stringBuilder.append(CF_SY).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_SY")))).append("\n");
        stringBuilder.append(CF_YJ).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_YJ")))).append("\n");
        stringBuilder.append(CF_CFLB).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_CFLB")))).append("\n");
        stringBuilder.append(CF_NR).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_NR")))).append("\n");
        stringBuilder.append(CF_NR_FK).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_NR_FK")))).append("\n");
        stringBuilder.append(CF_NR_WFFF).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_NR_WFFF")))).append("\n");
        stringBuilder.append(CF_NR_ZKDX).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_NR_ZKDX")))).append("\n");
        stringBuilder.append(CF_JDRQ).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_JDRQ")))).append("\n");
        stringBuilder.append(CF_YXQ).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_YXQ")))).append("\n");
        stringBuilder.append(CF_GSJZQ).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_GSJZQ")))).append("\n");
        stringBuilder.append(CF_CFJG).append(":").append(StringUtils.defaultString(String.valueOf(map.get("CF_CFJG")))).append("\n");
        stringBuilder.append(BZ).append(":").append(StringUtils.defaultString(String.valueOf(map.get("BZ")))).append("\n");
        regulatoryNotice.setContent(stringBuilder.toString());
        regulatoryNotice.setTitle(String.valueOf(map.get("CF_WSH")));
        String time = map.get("CF_JDRQ");
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
