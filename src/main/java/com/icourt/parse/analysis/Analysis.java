package com.icourt.parse.analysis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONPath;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public interface Analysis {

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    DateTimeFormatter dateFormatter1 = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    DateTimeFormatter dateFormatter2 = DateTimeFormatter.ofPattern("yyyyMMdd");
    DateTimeFormatter dateFormatter3 = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    DateTimeFormatter dateTimeFormatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    DateTimeFormatter dateTimeSecondFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    String[] allProvince = new String[]{"天津市", "北京市", "河北省", "山西省", "内蒙古自治区", "辽宁省", "吉林省", "黑龙江省", "上海市", "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省", "河南省", "湖北省", "湖南省", "广东省", "广西壮族自治区", "海南省", "重庆市", "四川省", "贵州省", "云南省", "西藏自治区", "陕西省", "甘肃省", "青海省", "宁夏回族自治区", "新疆维吾尔自治区"};
    Pattern localDateTimePattern = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{2}):(\\d{2}):(\\d{2})");
    Pattern localDateTimePattern1 = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2}) (\\d{2}):(\\d{2})");
    Pattern localDatePattern = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");
    Pattern timestampPattern = Pattern.compile("16\\d{8}");
    Pattern timestampPatternMills = Pattern.compile("\\d{13}");

    /**
     * 任务发布前置处理器
     *
     * @param mainJson
     * @return
     */
    default List<String> submitTaskPostProcessor(String mainJson) {
        List<String> list = new ArrayList<>();
        String value = null;
        //存在html时
        if (StringUtils.isNotBlank(mainJson)) {
            Object data = JSONPath.read(mainJson, "$html.value");
            if (data == null) {
                return null;
            }
            value = data.toString();
        }
        list.add(value);
        return list;
    }

    /**
     * 前置处理器
     *
     * @param noticeSource
     */
    void preProcessor(NoticeSource noticeSource);

    /**
     * 获取正文 Element
     *
     * @param document
     * @param url
     * @return
     */
    Element getContentElement(Document document, String url);

    /**
     * 处理ext_json字段，填充 RegulatoryNotice 属性
     *
     * @param extJson
     * @param regulatoryNotice
     */
    default void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {
        Map<String, String> map = JSON.parseObject(extJson, Map.class);
        //标题
        if (StringUtils.isBlank(regulatoryNotice.getTitle()))
            regulatoryNotice.setTitle(map.get("title") == null ? map.get("case_desc") : map.get("title"));

        //发布时间
        if (null == regulatoryNotice.getPublishTime()) {
            String dateTime = map.get("dateTime");
            try {
                LocalDateTime localDateTime = LocalDate.parse(dateTime, dateFormatter).atStartOfDay();
                regulatoryNotice.setPublishTime(localDateTime);
            } catch (Exception e) {
            }
        }
        //地域
        if (StringUtils.isBlank(regulatoryNotice.getProvince()))
            regulatoryNotice.setProvince(map.get("province"));

        //所属部委
        if (StringUtils.isBlank(regulatoryNotice.getNoticeMainBody()))
            regulatoryNotice.setNoticeMainBody(map.get("noticeMainBody"));

        if (StringUtils.isBlank(regulatoryNotice.getNoticeOrgan()))
            regulatoryNotice.setNoticeOrgan(map.get("noticeOrgan"));
    }

    ;

    /**
     * 标题过滤,正文优先级高
     *
     * @param regulatoryNotice
     */
    default void filterTitle(RegulatoryNotice regulatoryNotice) {
        String title = regulatoryNotice.getTitle();
        Integer type = regulatoryNotice.getType();
        if (title == null)
            return;
        if (type != null && type == 5)
            return;
        if (checkTitleToNotPick() != null && title.matches(checkTitleToNotPick())) {
            regulatoryNotice.setType(5);
            return;
        }
        if (checkTitleToPick() != null && title.matches(checkTitleToPick()))
            regulatoryNotice.setType(0);
        else if (type == null)
            regulatoryNotice.setType(5);
    }

    /**
     * 正文过滤
     *
     * @param regulatoryNotice
     * @return
     */
    default void filterContent(RegulatoryNotice regulatoryNotice) {
        String newContent = regulatoryNotice.getContent();
        if (checkContentToNotPick() != null && newContent.matches(checkContentToNotPick())) {
            regulatoryNotice.setType(5);
            return;
        }
        if (checkContentToPick() != null && newContent.matches(checkContentToPick()))
            regulatoryNotice.setType(0);
    }

    ;

    /**
     * 从regulatoryNotice提取基本信息(such as :title、publishTime、province)
     *
     * @param regulatoryNotice
     */
    default void fillInfo(RegulatoryNotice regulatoryNotice) {
    }

    ;


    /**
     * 所属部委
     *
     * @return
     */
    String mainBody();

    /**
     * 获取去重标识
     *
     * @param regulatoryNotice
     * @return
     */
    default String getDeduplication(RegulatoryNotice regulatoryNotice) {
        //title+_+publish+_+noticeMainBody
        return regulatoryNotice.getTitle() + "_" + regulatoryNotice.getPublishTime() + "_" + regulatoryNotice.getNoticeMainBody();
    }

    /**
     * 是否需要解析html,默认解析
     *
     * @return
     */
    default Boolean haveHtml() {
        return true;
    }


    /**
     * url过滤，处理
     *
     * @param url
     * @return 替换的url
     */
    default String DealContentUrl(String url) {
        return url;
    }

    /**
     * 标题包含，则状态为待处理
     *
     * @return
     */
    default String checkTitleToPick() {
        return ".*(通报|罚|App|应用软件|检测|约谈|个人信息|治理|网络安全|数据安全|整治|处罚|查处|通告|违法|隐私|执法|信息).*";
    }

    default String checkTitleToNotPick() {
        return null;
    }

    /**
     * 包含则为待处理状态
     *
     * @return
     */
    default String checkContentToPick() {
        return ".*(个人信息|网络安全|隐私|携号转网|数据安全|敏感权限|App|应用程序|未经同意查询).*";
    }

    /**
     * 包含则为不提取状态
     *
     * @return
     */
    default String checkContentToNotPick() {
        return ".*(遴选|比赛|竞赛|党建|党史|扶贫|座谈会|招聘|面试|继续教育|人民币管理|反洗钱|支票|支付机构稳健).*";
    }


}
