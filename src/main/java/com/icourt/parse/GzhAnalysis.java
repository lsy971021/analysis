package com.icourt.parse;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.icourt.Application;
import com.icourt.entity.compliance.db.GzhSource;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.mapper.article.GzhSourceDao;
import com.icourt.mapper.compliance.RegulatoryNoticeDao;
import com.icourt.parse.analysis.Analysis;
import com.icourt.util.GzhAnalysisUtils;
import com.icourt.util.MD5Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@SuppressWarnings("all")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class GzhAnalysis {

    @Autowired
    private GzhSourceDao gzhSourceDao;
    @Autowired
    private RegulatoryNoticeDao regulatoryNoticeDao;

    //空格
    private final String blank = "(\\u00a0|\\u0020|\\u3000|\\u00A0|\\|)";

    // 标题过滤
    private final String filterToPick = ".*(App|安全保护|查处|处罚|电信服务质量|电信业务|非法推送商业信息|互联网安全|检测|健康信息|净网|权限|敏感数据|平台服务|软件|杀熟|身份信息|实名|收集使用|数据安全|数据杀熟|刷脸|算法歧视|通报|通告|网络安全|网络\\S+安全|违法|未经同意|携号转网|信息|信息安全|信息保护|信息化标准|信息系统|信息泄露|医疗大数据|医疗信息化|隐私|应用程序|应用软件|约谈|账号|账号注销|整治|治理|自动化决策|自动化推荐个人信息|隐私|携号转网|数据安全|敏感权限|App|应用程序|小程序|个人数据|敏感数据|泄露|信息系统|未经同意查询|人信息|泄漏|消费者信息|数据扒取|扒取数据|爬取数据|数据爬取|爬虫|用户信息|人脸|消费者数据|删除信息|交易记录|交易信息|大数据杀熟|个性化推荐|违法网站|应用软件|信息留存|SDK|实名制|身份验证|互联网信息服务|记录制度|信息资料|客户信息|网络运营|数据处理|信息处理|信息网络|信息泄露|登记信息|档案信息|信息安全|安全保护|健康信息|电子病历|健康码|个人病历|遗传信息|生命登记信息|患者信息|基因数据|医疗支付记录|医保记录|健康记录|医疗应用数据|个人属性数据|健康状况数据|医疗支付数据|卫生资源数据|公共卫生数据|医疗数据|生理数据|生理信息|健康医疗数据|医疗健康数据|健康数据|健康医疗信息系统|治疗笔记|受限制数据集|临床信息系统|现病史|既往病史|检验检查数据|遗传咨询数据|医保支付信息|保险信息|保险状态|保险金额|医院基本数据|医院运营数据|环境卫生数据|传染病疫情数据|疾病监测数据|疾病预防数据|出生死亡数据|资助信息|未成年个人信息|教育信息|敏感信息|系统安全|黑客|网络信息犯罪|净网行动|遗传资源信息|病理数据|检测数据|备案数据).*";
    private final String filterTitleToNotPick = ".*(精神|制造业|产业集群|智能制造|学习|精神|c919|车辆购置税|国庆|产业链|内河|飞机|生产|考试|捐赠|退休|教育|思想|微课堂|救助|服贸会|老人|老年|劳动者|冬奥|培训|志愿者|代表|答题|电视|纪录片|营商环境|电信网码号|经营性年报|培训|遴选|比赛|竞赛|党建|党史|扶贫|座谈会|招聘|面试|继续教育|人民币管理|反洗钱|支票|支付机构稳健|挪用|备案|保险条款|审慎经营|保险|代理|电视专题片|整风|研讨|党课|防汛|抗洪|救灾|劳模|比赛|竞赛|党建|比赛|纪念|灾害|党史|党校|防台|知识考核|能力考核|节日|继续教育|锦旗|扶贫|脱贫|暴雨|疫情|防疫|招聘|选举|工程安全|遴选|优秀方案|安全生产|生产安全|产品质量|爆炸|民爆|党日活动|教育|思想|表彰|消防|救援|祖国|培训|产品质量|食品安全|食品生产|违法生产|违法销售|长假|保健品|购物|免税|测试|药品|达标|虚标|电子烟|假货|套现|评价|著作权|专利权|虚假宣传|企业名称登记管理规定|合伙企业法|假冒伪劣|专利代理|知识产权|食品|食物生产|食品安全|未在注册地址|停止使用有关设备|健康证明|六个月未开业|不合格产品|公司登记|禁毒|驾车|教育|会议|思想|跳楼|表彰|迷路|消防|自杀|救援|代驾|祖国|吸毒|杀人|交通|出行|天气|火灾|醉酒|见义勇为|热心|交警|和谐|贼|短信|酒驾|高温|偷|抢劫|春节|强奸|台风|暴雨|洪水|爆炸|代表|盗窃|贩毒|毒品|走访|监控|衣服|防盗|车祸|追尾|婚礼|结婚|烟花|食品|环境|卫生|化学|垃圾|打架|殴打|寻衅|滋事|赌博|仿制|淫|流动|伤害|扰乱|住宅|入户|出境|入境|伪造|侮辱|陪侍|虐待|赃物|犬|狗|危险物质|财物|故意伤害|妨害公务|排放污染物|阻碍执行职务|紧急状态|牺牲|满意度|食品安全|化妆品|考察|视察|停车|培训|退货|换货|纸尿裤|最低消费|产品质量|怎么选|浪费|食品安全|食品生产|惩罚性赔偿|违法生产|违法销售|长假|保健品|购物|免税|测试|药品|达标|虚标|黑中介|炒鞋|搭售|电子烟|整理|退一赔三|进口|假货|套现|奢侈品|捆绑|天价|差评|好评|评价|性价比|连续6个月未|未在登记住所|考试作弊|码号备案|工程质量|考试|招标|投标|评比|复工|复产|污染治理|论坛|伪基站|高考|未办理纳税申报|质量抽检).*";
    private final String filterContentToNotPick = ".*(培训|遴选|比赛|竞赛|党建|党史|扶贫|座谈会|招聘|面试|继续教育|人民币管理|反洗钱|支票|支付机构稳健|挪用|备案|保险条款|审慎经营|保险|代理|电视专题片|整风|研讨|党课|防汛|抗洪|救灾|劳模|比赛|竞赛|党建|比赛|纪念|灾害|党史|党校|防台|知识考核|能力考核|节日|继续教育|锦旗|扶贫|脱贫|暴雨|疫情|防疫|招聘|选举|工程安全|遴选|优秀方案|安全生产|生产安全|产品质量|爆炸|民爆|党日活动|教育|思想|表彰|消防|救援|祖国|培训|产品质量|食品安全|食品生产|违法生产|违法销售|长假|保健品|购物|免税|测试|药品|达标|虚标|电子烟|假货|套现|评价|著作权|专利权|虚假宣传|企业名称登记管理规定|合伙企业法|假冒伪劣|专利代理|知识产权|食品|食物生产|食品安全|未在注册地址|停止使用有关设备|健康证明|六个月未开业|不合格产品|公司登记|禁毒|驾车|教育|会议|思想|跳楼|表彰|迷路|消防|自杀|救援|代驾|祖国|吸毒|杀人|交通|出行|天气|火灾|醉酒|见义勇为|热心|交警|和谐|贼|短信|酒驾|高温|偷|抢劫|春节|强奸|台风|暴雨|洪水|爆炸|代表|盗窃|贩毒|毒品|走访|监控|衣服|防盗|车祸|追尾|婚礼|结婚|烟花|食品|环境|卫生|化学|垃圾|打架|殴打|寻衅|滋事|赌博|仿制|淫|流动|伤害|扰乱|住宅|入户|出境|入境|伪造|侮辱|陪侍|虐待|赃物|犬|狗|危险物质|财物|故意伤害|妨害公务|排放污染物|阻碍执行职务|紧急状态|牺牲|满意度|食品安全|化妆品|考察|视察|停车|培训|退货|换货|纸尿裤|最低消费|产品质量|怎么选|浪费|食品安全|食品生产|惩罚性赔偿|违法生产|违法销售|长假|保健品|购物|免税|测试|药品|达标|虚标|黑中介|炒鞋|搭售|电子烟|整理|退一赔三|进口|假货|套现|奢侈品|捆绑|天价|差评|好评|评价|性价比|连续6个月未|未在登记住所|考试作弊|码号备案|工程质量|考试|招标|投标|评比|复工|复产|污染治理|论坛|伪基站|高考|未办理纳税申报|质量抽检).*";

    //工业和信息化部
    private final String manbody1 = ".*(工信微报|广东信息通信业|北京通信业|上海通信圈|八闽通信|冀通信|西藏自治区通信管理局|新疆维吾尔自治区通信管理局|吉林省通信管理局|山西省通信管理局|甘肃省通信管理局|青海省通信管理局|贵州省通信管理局|内蒙古通信管理局|广西区通信管理局|山东省通信管理局|河南省通信管理局|浙江省通信管理局|四川省通信管理局|湖北省通信管理局|云南省通信管理局|陕西省通信管理局|海南省通信管理局|龙江通信|津通信|辽宁省通信管理局|江西信息通信业|安徽省通信管理局|重庆信息通信业|宁夏信息通信业|江苏通信业).*";
    //国家互联网信息办公室
    private final String manbody2 = ".*(App个人信息举报|网信中国|网信北京|网信天津|网信河北|网信山西|网信内蒙古|网信辽宁|网信吉林|网信黑龙江|网信上海|网信江苏|网信浙江|网信安徽|网信福建|网信江西|网信山东|网信河南|网信湖北|网信湖南|网信广东|网信广西|网信海南|网信重庆|网信四川|网信贵州|网信云南|网信西藏|网信陕西|陕西违法和不良信息举报中心|网信甘肃|网信青海|网信宁夏|网信新疆|网信兵团).*";
    //公安部
    private final String manbody3 = ".*(公安部网安局|平安北京|平安天津|河北公安|山西公安|平安辽宁|吉林公安|龙警|警民直通车上海|江苏公安微警务|浙江公安|安徽公安网|福建警方|江西公安|山东公安|河南公安微警务|平安湖北|湖南公安服务平台|广东公安|广西公安|海南公安|平安重庆|四川公安|贵州110|云南经侦|平安西藏|陕西公安|甘肃公安|青海公安|平安宁夏|平安天山|兵团警界).*";
    //国家市场监督管理总局
    private final String manbody4 = ".*(市说新语|北京市场监管|天津市场监管|河北市场监管|山西市场监管|内蒙古市场监管|辽宁市场监管|吉林市场监管|龙江市场监管|上海市场监管|江苏市场监管|浙江市场监管矩阵|安徽市场监管|福建市场监管|江西市场监管|山东市场监管|河南市场监管|湖北市场监管|湖南市场监管|广东市场监管|广西市场监管|海南市场监管|重庆市场监管|天府记市|贵州市场监管|云南市场监管|西藏市场监管|陕西市场监管|甘肃市场监管|青海市场监管|宁夏市场监管|新疆市场监管).*";
    //国家卫生健康委员会
    private final String manbody5 = ".*(健康中国|健康北京|健康天津|天津市卫健委网上政务服务平台|河北卫生健康|健康山西官微|健康内蒙古官微|健康辽宁|吉林卫生健康|健康龙江服务平台|上海卫生健康监督|健康江苏|健康浙江|健康安徽HealthyAnhui|健康福建|江西卫生健康|健康山东服务号|河南省卫生健康委|健康湖北|健康湖南|健康广东|粤健通|健康八桂|健康新海南|重庆卫生健康|健康四川官微|健康贵州|云南卫健委|健康西藏官微|健康陕西发布|健康甘肃|健康青海|宁夏回族自治区卫生健康委员会|健康新疆12320).*";
    //自然资源部
    private final String manbody6 = ".*(自然资源部|自然资源部门户网站|北京规划自然资源|天津规划自然资源|河北自然资源|山西自然资源|内蒙古自然资源|辽宁省自然资源厅|吉林省自然资源厅|黑龙江省自然资源厅|上海规划资源|江苏自然资源|浙江自然资源|安徽省自然资源厅|福建自然资源|江西省自然资源厅|山东自然资源|河南自然资源|湖北自然资源|湖南自然资源|广东自然资源|广西自然资源|海南省自然资源和规划厅|重庆规划自然资源|四川自然资源|贵州自然资源|云南省自然资源厅|陕西自然资源|甘肃自然资源|青海省自然资源厅|宁夏自然资源|新疆自然资源).*";

    //html过滤
    private final String htmlFilterStr = "以下文章来源于";

    static {
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 500,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(90000));
    CountDownLatch countDownLatch = null;

    @Test
    public void formatArticles() throws InterruptedException {
        log.info("文章解析任务开始执行...");
        // 用于保证任务队列里任务不重复
        HashSet<Long> ids = new HashSet<>();
        int pageNum = 1;
        int pageSize = 1000;
        // 剩余数量
        int remaining;
        int times = 1;
        IPage<GzhSource> page = new Page(pageNum, pageSize, false);
        do {
            log.info("第{}次循环执行", times++);
            // 每次解析50篇文章
            IPage<GzhSource> sourceIPage = gzhSourceDao.selectAllForCompliance(page);
            // 获取待解析的列表
            List<GzhSource> gzhSources = sourceIPage.getRecords();
            if (gzhSources.isEmpty())
                break;
            remaining = gzhSources.size();
            countDownLatch = new CountDownLatch(gzhSources.size());
            for (int i = 0; i < gzhSources.size(); i++) {
                threadPoolExecutor.submit(new Task(gzhSources.get(i)));
            }
            countDownLatch.await(40,TimeUnit.SECONDS);
        } while (remaining != 0);
        log.info("文章解析任务执行完成...");
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(1,TimeUnit.HOURS);
    }

    private class Task implements Runnable {
        private GzhSource source;

        public Task(GzhSource source) {
            this.source = source;
        }

        @Override
        public void run() {
            //解析html
            try {
                RegulatoryNotice regulatoryNotice = new RegulatoryNotice();
                regulatoryNotice.setCreateTime(LocalDateTime.now());
                regulatoryNotice.setSourceUrl(source.getContentUrl());
                regulatoryNotice.setThirdId(source.getMid());

                //解析html
                solveHtml(regulatoryNotice, source.getHtml());

                if (StringUtils.isBlank(regulatoryNotice.getTitle())) {
                    regulatoryNotice.setTitle(source.getTitle());
                }

                try {
                    Date publishTime = source.getPublishTime();
                    Instant instant = publishTime.toInstant();
                    ZoneId zoneId = ZoneId.systemDefault();
                    LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();
                    regulatoryNotice.setPublishTime(localDateTime);
                } catch (Exception e) {
                }

                //标题|正文过滤
//
//                if (regulatoryNotice.getTitle().contains("||") || regulatoryNotice.getTitle().contains("|")) {
//                    String[] split = regulatoryNotice.getTitle().split("\\|");
//                    if (split.length == 2) {
//                        String s1 = split[0];
//                        String s2 = split[1];
//                        regulatoryNotice.setTitle(s1.length() >= s2.length() ? s1 : s2);
//                    }
//                }

                String title = regulatoryNotice.getTitle();
                String content = regulatoryNotice.getContent();
                if (title.matches(filterToPick) || content.matches(filterToPick)) {
                    if (title.matches(filterTitleToNotPick) || content.matches(filterContentToNotPick)) {
                        regulatoryNotice.setType(5);
                    } else {
                        regulatoryNotice.setType(0);
                    }
                } else {
                    regulatoryNotice.setType(5);
                }

                String noticeOrgan = regulatoryNotice.getNoticeOrgan();
                if (noticeOrgan != null) {
                    if (noticeOrgan.matches(manbody1)) {
                        regulatoryNotice.setNoticeMainBody("工业和信息化部");
                    } else if (noticeOrgan.matches(manbody2)) {
                        regulatoryNotice.setNoticeMainBody("国家互联网信息办公室");
                    } else if (noticeOrgan.matches(manbody3)) {
                        regulatoryNotice.setNoticeMainBody("公安部");
                    } else if (noticeOrgan.matches(manbody4)) {
                        regulatoryNotice.setNoticeMainBody("国家市场监督管理总局");
                    } else if (noticeOrgan.matches(manbody5)) {
                        regulatoryNotice.setNoticeMainBody("国家卫生健康委员会");
                    } else if (noticeOrgan.matches(manbody6)) {
                        regulatoryNotice.setNoticeMainBody("自然资源部");
                    } else {
                        regulatoryNotice.setNoticeMainBody("其他");
                    }
                } else {
                    regulatoryNotice.setNoticeMainBody("其他");
                }


                // 生成唯一id (标题|发布时间|作者)
                String combine = regulatoryNotice.getTitle() + regulatoryNotice.getPublishTime() + regulatoryNotice.getNoticeMainBody();
                String cid = MD5Util.md5Hex(combine.getBytes());
                regulatoryNotice.setCid(cid);

                Integer updateSuccess;

                updateSuccess = regulatoryNoticeDao.insert(regulatoryNotice);
                //更新成功
                if (updateSuccess > 0) {
                    gzhSourceDao.update(null, new UpdateWrapper<GzhSource>().lambda()
                            .eq(GzhSource::getId, source.getId())
                            .set(GzhSource::getIsResolving, 1)
                    );
                }
            } catch (Exception e) {
                Long sourceId = source.getId();
                log.info("元数据id={} 解析异常.err={}", sourceId, e);
                //更新gzh_source表,解析状态为异常
                gzhSourceDao.update(null, new UpdateWrapper<GzhSource>().lambda()
                        .eq(GzhSource::getId, sourceId)
                        .set(GzhSource::getIsResolving, 2));
            } finally {
                countDownLatch.countDown();
            }
        }
    }

    Pattern provincePattern = Pattern.compile("provinceName: '\\S+'");
    Pattern gzhPattern = Pattern.compile("var author = \"\\S+\";");
    Pattern fromPattern = Pattern.compile("来源：\\S+");
    //符号过滤
    String regEx = "来源|\\pP|\\pS|\\s+";

    /**
     * 解析html ：  文章标题|发布时间|地域|正文
     *
     * @param gzhArticles
     * @param html
     */
    private void solveHtml(RegulatoryNotice regulatoryNotice, String html) {
        String province = null;
        try {
            Matcher matcher = provincePattern.matcher(html);
            while (matcher.find()) {
                province = matcher.group(0);
            }
            if (province != null) {
                String replace = province.replace("provinceName: '", "");
                province = replace.substring(0, replace.length() - 2);
                String[] allProvince = Analysis.allProvince;
                for (int i = 0; i < allProvince.length; i++) {
                    String p = allProvince[i];
                    if (p.contains(province) || province.contains(p)) {
                        province = p;
                        break;
                    }
                }
                regulatoryNotice.setProvince(province);
            }
        } catch (Exception e) {
        }


        //标题
        Document document = Jsoup.parse(html);

        String noticeOrgan = null;
        try {
            Matcher matcher = fromPattern.matcher(document.text());
            while (matcher.find()) {
                noticeOrgan = matcher.group(0);
            }
            if (noticeOrgan != null) {
                regulatoryNotice.setNoticeOrgan(noticeOrgan.replaceAll(regEx, "").trim());
            }
            if (regulatoryNotice.getNoticeOrgan() == null) {
                Matcher m = gzhPattern.matcher(html);
                while (m.find()) {
                    noticeOrgan = m.group(0);
                }
                if (noticeOrgan != null) {
                    noticeOrgan = noticeOrgan.replace("var author = \"", "");
                    regulatoryNotice.setNoticeOrgan(noticeOrgan.substring(0, noticeOrgan.length() - 2).trim());
                }
            }
        } catch (Exception e) {
        }


        Element titleElement = document.getElementById("activity-name");
        if (null != titleElement)
            regulatoryNotice.setTitle(titleElement.text());

        if (regulatoryNotice.getNoticeOrgan() == null) {
            try {
                Element js_name = document.getElementById("js_name");
                regulatoryNotice.setNoticeOrgan(js_name.text().trim().replace(blank, ""));
            } catch (Exception e) {
            }
        }
        String mainText = GzhAnalysisUtils.analysis(html, false);
        regulatoryNotice.setContent(mainText);
    }
}
