package com.icourt.parse.analysis.Impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPbcFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 证券监督管理委员会
 */
@Slf4j
public class CsrcAnalysis extends AbstractPbcFilter {

    @Override
    public void preProcessor(NoticeSource noticeSource) {
        String response = noticeSource.getValue();

        if (StringUtils.isBlank(response))
            return;

        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();

        String regex = "(\\u00a0|\\u0020|\\u3000)";

        Map<String, Object> body = (Map<String, Object>) JSONPath.read(response, "$.results.data.data[0]._source");
        if (CollectionUtil.isEmpty(body)) return;
        Object publishedTime = body.get("publishedTime");
        Object title = Objects.isNull(body.get("title")) ? body.get("subTitle") : body.get("title");
        Object uri = body.get("url");
        String resList = body.get("resList") == null ? null : body.get("resList").toString();
        noticeSource.setExtJson(resList);
        Object contentHtml = body.get("contentHtml");
        String path = null;
        String province = null;
        String noticeOrgan = null;
        try {
            path = uri.toString();
            String key = path.split("/")[1];
            String v = infoMap.get(key);
            String[] split = v.split("/");
            province = split[0];
            noticeOrgan = split[1];
        } catch (Exception e) {
        }

        String url = "http://www.csrc.gov.cn" + path;

        if (Objects.nonNull(contentHtml) && (contentHtml.toString().length() < 10 || contentHtml.toString().equals(" "))) {
            String content = body.get("content") == null ? "" : body.get("content").toString();
            if (StringUtils.isBlank(content) && resList == null)
                return;
            regulatoryNotice.setContent(content.replaceAll(regex, "\n"));
        } else {
            noticeSource.setHtml(contentHtml.toString());
        }
        //标题
        regulatoryNotice.setTitle(title.toString());
        //数据源第三方url
        regulatoryNotice.setSourceUrl(url);
        //处罚机构
        regulatoryNotice.setThemeLabels("行政处罚");
        //地域
        regulatoryNotice.setProvince(province);
        //通报机构
        regulatoryNotice.setNoticeOrgan(noticeOrgan);
        //处罚时间 yyyy-mm-dd
        if (Objects.nonNull(publishedTime)) {
            try {
                Instant instant = Instant.ofEpochMilli((Long) publishedTime);
                LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
                log.info("日期格式转换异常,sourceUrl={}", url);
            }
        }
    }

    @Override
    public Element getContentElement(Document document, String url) {
        return document.body();
    }


    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        if (StringUtils.isBlank(extJson))
            return;
        String content = regulatoryNotice.getContent();
        StringBuilder stringBuilder = new StringBuilder(content);
        List<Map> list = JSON.parseObject(extJson, List.class);
        try {
            for (int i = 0; i < list.size(); i++) {
                Map map = list.get(i);
                String filePath = map.get("filePath").toString();
                String fileName = map.get("fileName").toString();
                int index = filePath.lastIndexOf(".") + 1;
                String mode = filePath.substring(index);
                Object urlObj = map.get("url");
                if (urlObj != null) {
                    String urlStr = urlObj.toString();
                    int lastIndexOf = urlStr.lastIndexOf(".") + 1;
                    String substring = urlStr.substring(lastIndexOf);
                    if (!substring.equals(mode)) {
                        int indexOf = filePath.lastIndexOf("/") + 1;
                        filePath = filePath.substring(0, indexOf) + fileName + "." + substring;
                    }
                }
                stringBuilder.append("\n").append("<a href=\"").append("http://www.csrc.gov.cn").append(filePath).append("\" class=\"cDownFile\">").append(fileName).append("</a>");
            }
        } catch (Exception e) {
            return;
        }
        regulatoryNotice.setContent(stringBuilder.toString());
    }

    @Override
    public String mainBody() {
        return "中国证券监督管理委员会";
    }

    static Map<String, String> infoMap = new HashMap<>();

    static {
        infoMap.put("csrc", "全国/中国证券监督管理委员会");
        infoMap.put("beijing", "北京市/中国证券监督管理委员会北京监管局");
        infoMap.put("tianjin", "天津市/中国证券监督管理委员会天津监管局");
        infoMap.put("hebei", "河北省/中国证券监督管理委员会河北监管局");
        infoMap.put("shanxi", "山西省/中国证券监督管理委员会山西监管局");
        infoMap.put("neimenggu", "内蒙古自治区/中国证券监督管理委员会内蒙古监管局");
        infoMap.put("liaoning", "辽宁省/中国证券监督管理委员会辽宁监管局");
        infoMap.put("jilin", "吉林省/中国证券监督管理委员会吉林监管局");
        infoMap.put("heilongjiang", "黑龙江省/中国证券监督管理委员会黑龙江监管局");
        infoMap.put("shanghai", "上海市/中国证券监督管理委员会上海监管局");
        infoMap.put("jiangsu", "江苏省/中国证券监督管理委员会江苏监管局");
        infoMap.put("zhejiang", "浙江省/中国证券监督管理委员会浙江监管局");
        infoMap.put("anhui", "安徽省/中国证券监督管理委员会安徽监管局");
        infoMap.put("fujian", "福建省/中国证券监督管理委员会福建监管局");
        infoMap.put("jiangxi", "江西省/中国证券监督管理委员会江西监管局");
        infoMap.put("shandong", "山东省/中国证券监督管理委员会山东监管局");
        infoMap.put("henan", "河南省/中国证券监督管理委员会河南监管局");
        infoMap.put("hubei", "湖北省/中国证券监督管理委员会湖北监管局");
        infoMap.put("hunan", "湖南省/中国证券监督管理委员会湖南监管局");
        infoMap.put("guangdong", "广东省/中国证券监督管理委员会广东监管局");
        infoMap.put("guangxi", "广西省/中国证券监督管理委员会广西监管局");
        infoMap.put("hainan", "海南省/中国证券监督管理委员会海南监管局");
        infoMap.put("chongqing", "重庆市/中国证券监督管理委员会重庆监管局");
        infoMap.put("sichuan", "四川省/中国证券监督管理委员会四川监管局");
        infoMap.put("guizhou", "贵州省/中国证券监督管理委员会贵州监管局");
        infoMap.put("yunnan", "云南省/中国证券监督管理委员会云南监管局");
        infoMap.put("tibet", "西藏自治区/中国证券监督管理委员会西藏监管局");
        infoMap.put("shaanxi", "陕西省/中国证券监督管理委员会陕西监管局");
        infoMap.put("gansu", "甘肃省/中国证券监督管理委员会甘肃监管局");
        infoMap.put("qinghai", "青海省/中国证券监督管理委员会青海监管局");
        infoMap.put("ningxia", "宁夏回族自治区/中国证券监督管理委员会宁夏监管局");
        infoMap.put("xinjiang", "新疆维吾尔自治区/中国证券监督管理委员会新疆监管局");
        infoMap.put("shenzhen", "广东省/中国证券监督管理委员会深圳监管局");
        infoMap.put("dalian", "辽宁省/中国证券监督管理委员会大连监管局");
        infoMap.put("ningbo", "浙江省/中国证券监督管理委员会宁波监管局");
        infoMap.put("xiamen", "福建省/中国证券监督管理委员会厦门监管局");
        infoMap.put("qingdao", "山东省/中国证券监督管理委员会青岛监管局");
    }

}
