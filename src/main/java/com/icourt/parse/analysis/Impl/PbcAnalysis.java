package com.icourt.parse.analysis.Impl;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.abs.AbstractPbcFilter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 中国人民银行
 */
@Slf4j
public class PbcAnalysis extends AbstractPbcFilter {

    static String host = "r-2ze08c456e8680e4.redis.rds.aliyuncs.com";
    static int port = 6379;
    static String passwd = "Y8AC7hSUW8zYCWI5";
    static int database = 2;
    static final String FILE_OSS_REDIS_KEY = "oss-filebean-map";


    @Override
    public void preProcessor(NoticeSource noticeSource) {
    }


    @Override
    public Element getContentElement(Document document, String url) {
        if (document == null) {
            log.info("解析失败,url={}", url);
            return null;
        }
        String cityName = url.substring(7, url.indexOf("."));
        String[] info = tagMap.get(cityName).split("/");
        String path = info[1];
        //上层elm
        Elements elements = document.select(path);
        Element first = elements.first();
        if (url.contains("www")) {
            Elements detailBreadcrum = first.getElementsByClass("DetailBreadcrum");
            if (detailBreadcrum != null)
                detailBreadcrum.remove();
        }
        return first;
    }


    @Override
    public void delExtJson(String extJson, RegulatoryNotice regulatoryNotice) {

    }


    @Override
    public void fillInfo(RegulatoryNotice regulatoryNotice) {
        String content = regulatoryNotice.getContent();
        String[] split = content.split("\n");
        LocalDateTime publishTime = regulatoryNotice.getPublishTime();
        StringBuilder stringBuilder = new StringBuilder();
        boolean flag = true;
        for (int i = 0; i < split.length; i++) {
            String row = split[i];
            if (StringUtils.isBlank(row) || row.equals("\n") || row.equals("我的位置：")) {
                continue;
            }
            //包含中文
            if (flag && row.matches(".*[\\u4e00-\\u9fa5].*")) {
                flag = false;
                String title = Jsoup.parse(row).text();
                regulatoryNotice.setTitle(title);
            }
            stringBuilder.append(row).append("\n");
            //时间处理
            try {
                if (publishTime == null) {
                    if (row.contains("发布时间：")) {
                        String[] s = row.split("：");
                        String time = s[1];
                        regulatoryNotice.setPublishTime(LocalDateTime.of(LocalDate.parse(time, dateFormatter1), LocalTime.MIN));
                    }
                    if (row.contains("行政处罚信息公示")) {
                        String[] s = row.split("示");
                        String time = s[1];
                        regulatoryNotice.setPublishTime(LocalDateTime.of(LocalDate.parse(time, dateFormatter2), LocalTime.MIN));
                    }
                    if (row.contains("行政处罚信息公示表")) {
                        if (StringUtils.containsAny(row, "（", "）")) {
                            int start = row.indexOf("（");
                            int end = row.indexOf("）");
                            String time = row.substring(start + 1, end);
                            if (StringUtils.containsAny(time, "年", "月", "日")) {
                                if (time.length() != 11) {
                                    int year = time.indexOf("年");
                                    int month = time.indexOf("月");
                                    int day = time.indexOf("日");
                                    if (month - year < 3)
                                        time = time.replace("年", "年0");
                                    if (day - month < 3)
                                        time = time.replace("月", "月0");
                                }
                                regulatoryNotice.setPublishTime(LocalDateTime.of(LocalDate.parse(time, dateFormatter1), LocalTime.MIN));
                            } else if (time.contains(".")) {
                                regulatoryNotice.setPublishTime(LocalDateTime.of(LocalDate.parse(time, dateFormatter3), LocalTime.MIN));
                            }
                        }
                    }

                }
            } catch (Exception e) {
            }
        }
        String url = regulatoryNotice.getSourceUrl();
        String cityName = url.substring(7, url.indexOf("."));
        String[] info = tagMap.get(cityName).split("/");
        String province = info[0];
        regulatoryNotice.setProvince(province);
        //通报类型
        regulatoryNotice.setThemeLabels("行政处罚");
        String noticeOrgan = info[2];
        //通报机构
        regulatoryNotice.setNoticeOrgan(noticeOrgan);
        regulatoryNotice.setContent(stringBuilder.toString());

    }

    @Override
    public void filterContent(RegulatoryNotice regulatoryNotice) {
        super.filterContent(regulatoryNotice);
        String content = regulatoryNotice.getContent();
        regulatoryNotice.setContent(content.replace(" >  >  > ", ""));
    }

    @Override
    public String getDeduplication(RegulatoryNotice regulatoryNotice) {
        return regulatoryNotice.getSourceUrl();
    }


    @Override
    public String mainBody() {
        return "中国人民银行";
    }


    @Override
    public String DealContentUrl(String url) {
        Jedis jedis = null;
        String newUrl = url;
        try {
            jedis = new Jedis(host, port);
            //切换到数据库2
            jedis.auth(passwd);
            jedis.select(database);
            newUrl = jedis.hget(FILE_OSS_REDIS_KEY, url);
            if (StringUtils.isBlank(newUrl))
                return url;
        } catch (Exception e) {
        } finally {
            jedis.close();
        }

        return newUrl;
    }


    private static Map<String, String> tagMap = new HashMap();


    static {
        //http://shanghai.pbc.gov.cn/fzhshanghai/113577/114832/114918/2965438/index.html 表格
        //http://shanghai.pbc.gov.cn/fzhshanghai/113577/114832/114918/2186650/index.html 文件
//        tagMap.put("shanghai", "#zoom"); //文件为<a>  表格为<div>
        tagMap.put("shanghai", "上海市/#15163/中国人民银行上海分行"); //文件为<a>  表格为<div>
        tagMap.put("shenzhen", "广东省/#14995/中国人民银行深圳市中心支行");
        tagMap.put("jinan", "山东省/#13861/中国人民银行济南分行");
        //http://kunming.pbc.gov.cn/kunming/133736/133760/133767/4437078/index.html 表格
        //http://kunming.pbc.gov.cn/kunming/133736/133760/133767/2975216/index.html 文件
        //http://kunming.pbc.gov.cn/kunming/133736/133760/133767/2574164/index.html 文本
        tagMap.put("kunming", "云南省/#20189/中国人民银行昆明中心支行");
        tagMap.put("chengdu", "四川省/#18213/中国人民银行成都分行");
        //http://fuzhou.pbc.gov.cn/fuzhou/126805/126823/126830/2383444/index.html 表格
        //http://fuzhou.pbc.gov.cn/fuzhou/126805/126823/126830/4201279/index.html 文件
        tagMap.put("fuzhou", "福建省/#16896/中国人民银行福州中心支行");
        tagMap.put("guangzhou", "广东省/#18101/中国人民银行广州分行");
        tagMap.put("hefei", "安徽省/#14627/中国人民银行合肥中心支行");
        //http://zhengzhou.pbc.gov.cn/zhengzhou/124182/124200/124207/4475194/index.html 多个文件
        tagMap.put("zhengzhou", "河南省/#15801/中国人民银行郑州中心支行");
        //http://wulumuqi.pbc.gov.cn/wulumuqi/121755/121777/121784/3906166/index.html 表格
        //http://wulumuqi.pbc.gov.cn/wulumuqi/121755/121777/121784/4445625/index.html 文件
        tagMap.put("wulumuqi", "新疆维吾尔自治区/#14369/中国人民银行乌鲁木齐中心支行");
        //http://shenyang.pbc.gov.cn/shenyfh/108074/108127/108208/4462327/index.html 多个文件
        tagMap.put("shenyang", "辽宁省/#40d7f63bf2e044d1867ef705f854d2be/中国人民银行沈阳分行");
        tagMap.put("tianjin", "天津市/#11033/中国人民银行天津分行");
        tagMap.put("xian", "陕西省/#18325/中国人民银行西安分行");
        tagMap.put("taiyuan", "山西省/#20398/中国人民银行太原中心支行");
        tagMap.put("nanning", "广西省/#19925/中国人民银行南宁中心支行");
        tagMap.put("changsha", "湖南省/#18713/中国人民银行长沙中心支行");
        tagMap.put("huhehaote", "内蒙古自治区/#18507/中国人民银行呼和浩特中心支行");
        tagMap.put("guiyang", "贵州省/#10905/中国人民银行贵阳中心支行");
        //http://nanchang.pbc.gov.cn/nanchang/132372/132390/132397/2708126/index.html 没用
        //http://nanchang.pbc.gov.cn/nanchang/132372/132390/132397/4461239/index.html 文件
        //http://nanchang.pbc.gov.cn/nanchang/132372/132390/132397/4445526/index.html 表格
        tagMap.put("nanchang", "江西省/#19449/中国人民银行南昌中心支行");
        tagMap.put("chongqing", "重庆市/#7946/中国人民银行重庆营业管理部");
        tagMap.put("haerbin", "黑龙江省/#10356/中国人民银行哈尔滨中心支行");
        //http://beijing.pbc.gov.cn/beijing/132030/132052/132059/4182502/index.html 表格
        tagMap.put("beijing", "北京市/#19228/中国人民银行营业管理部（北京）");
        tagMap.put("wuhan", "湖北省/#15319/中国人民银行武汉分行");
        tagMap.put("hangzhou", "浙江省/#16441/中国人民银行杭州中心支行");
        tagMap.put("changchun", "吉林省/#16163/中国人民银行长春中心支行");
        //http://ningbo.pbc.gov.cn/ningbo/127076/127098/127105/3053564/index.html 文件 有的p标签无a标签
        tagMap.put("ningbo", "浙江省/#17005/中国人民银行宁波市中心支行");
        //http://qingdao.pbc.gov.cn/qingdao/126166/126184/126191/16720/index9.html   列表中对应的详情有的直接为下载链接倒数3
        //http://qingdao.pbc.gov.cn/qingdao/126166/126184/126191/4179110/index.html 表格（有的是table有的是div）
        //http://qingdao.pbc.gov.cn/qingdao/126166/126184/126191/4442346/index.html  表格 table
        tagMap.put("qingdao", "山东省/#16595/中国人民银行青岛市中心支行");
        tagMap.put("lanzhou", "甘肃省/#12347/中国人民银行兰州中心支行");
        //http://dalian.pbc.gov.cn/dalian/123812/123830/123837/4460932/index.html 文件  较特殊 有div或p标签下的a
        //http://dalian.pbc.gov.cn/dalian/123812/123830/123837/4451858/index.html 表格
        tagMap.put("dalian", "辽宁省/#15472/中国人民银行大连市中心支行");
        //http://nanjing.pbc.gov.cn/nanjing/117542/117560/117567/3189572/index.html 文件
        //http://nanjing.pbc.gov.cn/nanjing/117542/117560/117567/4437230/index.html 表格
        tagMap.put("nanjing", "江苏省/#12607/中国人民银行南京分行");
        tagMap.put("haikou", "海南省/#19607/中国人民银行海口中心支行");
        tagMap.put("yinchuan", "宁夏回族自治区/#13436/中国人民银行银川中心支行");
        tagMap.put("shijiazhuang", "河北省/#18976/中国人民银行石家庄中心支行");
        tagMap.put("lasa", "西藏自治区/#13645/中国人民银行拉萨中心支行");
        tagMap.put("xining", "青海省/#13033/中国人民银行西宁中心支行");
        tagMap.put("xiamen", "福建省/#17382/中国人民银行厦门市中心支行");
        //人民银行总行
        tagMap.put("www", "全国/.content_box/中国人民银行");
    }
}
