package com.icourt.parse;

import cn.hutool.core.io.resource.ResourceUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.icourt.Application;
import com.icourt.datacenter.utils.formal.JsoupUtil;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.mapper.compliance.RegulatoryNoticeDao;
import com.icourt.parse.analysis.Analysis;
import com.icourt.parse.analysis.AnalysisFactory;
import com.icourt.parse.analysis.Impl.GatShandong2Analysis;
import com.icourt.service.compliance.TopicExtractDomainService;
import com.icourt.util.MD5Util;
import org.springframework.test.context.junit4.SpringRunner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@SuppressWarnings("all")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {Application.class})
public class AnalysisSource {

    @Autowired
    private RegulatoryNoticeDao regulatoryNoticeDao;
    @Autowired
    private TopicExtractDomainService extractDomainService;


    //解析失败数据
    final List<String> failed = new ArrayList<>();
    //需解析的表
    final static List<String> tableList = new ArrayList<>();

    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 100,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(90000));
    //pg连接connection
    static Connection connection = null;
    AtomicLong atomicLong = new AtomicLong();
    //用于等待线程池任务结束，开始下批次执行
    CountDownLatch countDownLatch = null;
    //每次查询带解析数量
    int limitSize = 1000;
    //需解析数据总量/表
    volatile static int count = 0;

    /**
     * 解析数据源表
     *
     * @throws Exception
     */
    @Test
    public void analysis() throws Exception {
        log.info("监管通告解析开始...");
        Class.forName("org.postgresql.Driver");  //反射运行时
        //计数
        AtomicInteger atomicInteger = new AtomicInteger();
        try {
            for (int i = 0; i < tableList.size(); i++) {
                String table = tableList.get(i);
                if (!StringUtils.containsAny(table, "www_zjzwfw_gov_cn111", "www_zjzwfw_gov_cn111"))
                    continue;

                log.info("开始解析表：{}", table);

                //2.获得预处理对象,将sql语句发送给数据库进行编译
                //? 表示占位符
                String querySql = "select id,page_url as pageUrl,main_json as mainJson,ext_json as extJson,code from " + table + " where stored_to_db = 0 LIMIT " + limitSize;
                String selectCount = "select count(*) as count from " + table + " where stored_to_db in (0,1)";
                //总数量
                do {
                    ResultSet countResult = executeSql(selectCount);
                    if (countResult.next()) {
                        String countStr = countResult.getString("count");
                        if (StringUtils.isNotEmpty(countStr))
                            count = Integer.valueOf(countStr);
                    }
                    if (count < 1)
                        break;
                    System.out.println("\n待解析数量=" + count + "\n");
                    int newCount = count >= limitSize ? limitSize : count;
                    //当剩下5个时候执行下次查询
                    countDownLatch = new CountDownLatch((newCount - 3) > 0 ? newCount : 3);
                    System.out.println("开始sql查询...");
                    long startQueryTime = System.currentTimeMillis();
                    ResultSet resultSet = executeSql(querySql);
                    long endQueryTime = System.currentTimeMillis();
                    System.out.println("\n查询结束，执行时间=====" + (endQueryTime - startQueryTime) / 1000 + "\n");
                    Thread.sleep(2000);
                    while (resultSet.next()) {
                        Analysis analysis = AnalysisFactory.getAnalysis(table);
                        if (analysis == null)
                            continue;
                        NoticeSource noticeSource = new NoticeSource();
                        noticeSource.setId(Long.valueOf(resultSet.getString("id")));
                        noticeSource.setExtJson(resultSet.getString("extJson"));
                        noticeSource.setUrl(resultSet.getString("pageUrl"));
                        noticeSource.setAnalysis(analysis);
                        noticeSource.setThirdId(resultSet.getString("code"));
                        noticeSource.setTable(table);
                        List<String> mainJsonList = analysis.submitTaskPostProcessor(resultSet.getString("mainJson"));
                        for (int r = 0; r < mainJsonList.size(); r++) {
                            NoticeSource analysisClone = (NoticeSource) noticeSource.clone();
                            String mainJson = String.valueOf(mainJsonList.get(r));
                            analysisClone.setValue(mainJson);
                            ParsingTask parsingTask = new ParsingTask(analysisClone);
                            log.info("第{}条数据开始解析,url={}...", atomicInteger.incrementAndGet(), noticeSource.getUrl());
                            threadPoolExecutor.submit(parsingTask);
                        }
                    }
                    countDownLatch.await(1, TimeUnit.MINUTES);
                    Thread.sleep(5000);
                } while (count > 0);
            }
            threadPoolExecutor.shutdown();
            threadPoolExecutor.awaitTermination(2, TimeUnit.HOURS);
        } finally {
            connection.close();
        }
    }

    //解析任务
    private class ParsingTask implements Runnable {
        private NoticeSource noticeSource;

        public ParsingTask(NoticeSource noticeSource) {
            this.noticeSource = noticeSource;
        }

        @Override
        public void run() {
            String updateSql = "update " + noticeSource.getTable() + " set stored_to_db=%d where id=%d and stored_to_db in (0,1)";
            String sql;
            try {
                //解析并存储
                Boolean result = analysisRow(noticeSource);
                if (result != null && result) {
                    //若解析成功
                    sql = updateSql.format(updateSql, 2, noticeSource.getId());
                } else { //若解析失败，更新storeToDb状态
                    failed.add(noticeSource.getUrl());
                    sql = updateSql.format(updateSql, -1, noticeSource.getId());
                }
            } catch (SQLException e) {
                failed.add(noticeSource.getUrl());
                sql = updateSql.format(updateSql, -2, noticeSource.getId());
            }
            try {
                executeSql(sql);
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                System.out.println("countDownLatch = " + countDownLatch.getCount());
            }
        }
    }


    /**
     * 重新解析表
     */
    @Test
    public void reAnalysis() throws InterruptedException {
        //每次查询数量
        int size = 1000;
        LambdaQueryWrapper<RegulatoryNotice> queryWrapper = new QueryWrapper<RegulatoryNotice>().lambda();
        queryWrapper.eq(RegulatoryNotice::getType, 0);
        queryWrapper.eq(RegulatoryNotice::getNoticeOrgan, "山东省公安厅");
        //需操作数据总量
        Integer count = regulatoryNoticeDao.selectCount(queryWrapper);

        queryWrapper.select(RegulatoryNotice::getId, RegulatoryNotice::getTitle, RegulatoryNotice::getContent);

        //页数
        int pages = count / size + (count % size > 0 ? 1 : 0);

        for (int i = 1; i <= pages; i++) {
            IPage<RegulatoryNotice> page = new Page(i, size, false);
            IPage<RegulatoryNotice> pageRecords = regulatoryNoticeDao.selectPage(page, queryWrapper);
            List<RegulatoryNotice> records = pageRecords.getRecords();
            if (records.isEmpty())
                break;
            countDownLatch = new CountDownLatch(records.size());
            records.stream().forEach(regulatoryNotice -> {
                threadPoolExecutor.submit(new Task(regulatoryNotice));
            });
            countDownLatch.await(1, TimeUnit.MINUTES);
        }
        if (!ids.isEmpty()) {
            List<List<Long>> partition = Lists.partition(ids, 1000);
            for (int i = 0; i < partition.size(); i++) {
                LambdaUpdateWrapper<RegulatoryNotice> updateWrapper = new UpdateWrapper<RegulatoryNotice>().lambda();
                updateWrapper.in(RegulatoryNotice::getId, partition.get(i));
                updateWrapper.set(RegulatoryNotice::getType, 5);
                regulatoryNoticeDao.update(null, updateWrapper);
            }

        }
        threadPoolExecutor.shutdown();
        threadPoolExecutor.awaitTermination(1, TimeUnit.HOURS);
    }

    static volatile List<Long> ids = new ArrayList<>();

    private class Task implements Runnable {
        RegulatoryNotice regulatoryNotice;
        GatShandong2Analysis gatShandong2Analysis = new GatShandong2Analysis();

        public Task(RegulatoryNotice regulatoryNotice) {
            this.regulatoryNotice = regulatoryNotice;
        }

        @Override
        public void run() {
            countDownLatch.countDown();
            gatShandong2Analysis.filterContent(regulatoryNotice);
            gatShandong2Analysis.filterTitle(regulatoryNotice);
            if (regulatoryNotice.getType() == 5) {
                ids.add(regulatoryNotice.getId());
            }
            System.out.println("countDownLatch = " + countDownLatch.getCount());
        }
    }


    /**
     * 查询 sql，返回结果
     *
     * @param sql
     * @return
     * @throws SQLException
     */
    public ResultSet executeSql(String sql) throws SQLException {
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet resultSet;
        if (sql.contains("select")) {
            resultSet = pst.executeQuery();
            return resultSet;
        } else {
            pst.executeUpdate();
            return null;
        }
    }


    /**
     * 对单行数据解析
     *
     * @param noticeSource
     * @throws SQLException
     */
    public Boolean analysisRow(NoticeSource noticeSource) throws SQLException {


        Analysis analysis = noticeSource.getAnalysis();
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        countDownLatch.countDown();
        //前置处理器
        try {
            analysis.preProcessor(noticeSource);
        } catch (Exception e) {
            return false;
        }

        if (StringUtils.isBlank(noticeSource.getHtml()))
            noticeSource.setHtml(noticeSource.getValue());

        //对value解析，获取正文,填充content
        if (StringUtils.isEmpty(regulatoryNotice.getContent()) && analysis.haveHtml()) {
            formatHtml(noticeSource);
        }

        regulatoryNotice = buildEntity(noticeSource);
        //创建RegulatoryNotice对象并保存在数据库
        return save(regulatoryNotice);
    }


    /**
     * 创建实体类，并更新到数据库
     *
     * @param regulatoryNotice
     */
    public boolean save(RegulatoryNotice regulatoryNotice) {
        if (regulatoryNotice == null) {
            return false;
        }
        //保存到解析表
        boolean success = saveOrUpdate(regulatoryNotice);
        if (success) {
            log.info("数据同步成功，title={},url={}", regulatoryNotice.getTitle(), regulatoryNotice.getSourceUrl());
        }
        return success;
    }


    /**
     * 解析html
     *
     * @param noticeSource
     * @return
     */
    public void formatHtml(NoticeSource noticeSource) {
        String html = noticeSource.getHtml();
        if (StringUtils.isBlank(html))
            return;
        Analysis analysis = noticeSource.getAnalysis();
        String url = noticeSource.getUrl();
        String content = null;
        html = html.replaceAll("<[\\s]*?br[^>]*>", "《br》");
        Document document = Jsoup.parse(html);
        //获取包含正文的element
        Element element = analysis.getContentElement(document, url);
        //处理element，获取正文
        content = getContent(element, url, analysis);
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setContent(content);

        if (StringUtils.isBlank(content))
            log.info("html解析失败，url={}", noticeSource.getUrl());
    }


    /**
     * 构建/填充属性实体类
     *
     * @param noticeSource
     * @return
     */
    private RegulatoryNotice buildEntity(NoticeSource noticeSource) {
        Analysis analysis = noticeSource.getAnalysis();
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();

        //0 监控  1 审核  2 列表
        regulatoryNotice.setProcess(0);
        //源url
        if (StringUtils.isBlank(regulatoryNotice.getSourceUrl()))
            regulatoryNotice.setSourceUrl(noticeSource.getUrl());
        //三方id
        regulatoryNotice.setThirdId(noticeSource.getThirdId());
        //所属部委
        regulatoryNotice.setNoticeMainBody(analysis.mainBody());
        //创建时间
        regulatoryNotice.setCreateTime(LocalDateTime.now());

        String extJson = noticeSource.getExtJson();

        //若不包含title、publish、province 则从正文提取
        if (StringUtils.isNotEmpty(extJson)) {
            //处理ext_json
            analysis.delExtJson(extJson, regulatoryNotice);
        }

        //正文过滤
        analysis.filterContent(regulatoryNotice);

        //填充regulatoryNotice属性
        if (regulatoryNotice.getTitle() == null && regulatoryNotice.getPublishTime() == null && regulatoryNotice.getProvince() == null)
            analysis.fillInfo(regulatoryNotice);

        String content = regulatoryNotice.getContent();
        if (StringUtils.isBlank(content))
            return null;
        regulatoryNotice.setContent(content.replaceAll("( | )", ""));
        //标题过滤（小于5000不过滤）
        analysis.filterTitle(regulatoryNotice);

        if (regulatoryNotice.getProvince() == null)
            regulatoryNotice.setProvince("全国");

        //唯一id
        String deduplication = analysis.getDeduplication(regulatoryNotice);
        String cid = MD5Util.md5Hex(deduplication.getBytes());
        regulatoryNotice.setCid(cid);

        //提取关键词
        extractDomainService.extractTopicFromNotice(regulatoryNotice);

        return regulatoryNotice;
    }

    final ReentrantLock lock = new ReentrantLock(false);

    private boolean saveOrUpdate(RegulatoryNotice regulatoryNotice) {
        try {
            if (regulatoryNotice.getNoticeOrgan().equals("国家移动互联网应用安全管理中心")) {
                lock.lock();
                LambdaQueryWrapper<RegulatoryNotice> queryWrapper = new QueryWrapper<RegulatoryNotice>().lambda();
                queryWrapper.eq(RegulatoryNotice::getTitle, regulatoryNotice.getTitle());
                queryWrapper.eq(RegulatoryNotice::getNoticeMainBody, regulatoryNotice.getNoticeMainBody());
                queryWrapper.eq(RegulatoryNotice::getPublishTime, regulatoryNotice.getPublishTime());
                queryWrapper.select(RegulatoryNotice::getId, RegulatoryNotice::getContent);

                RegulatoryNotice notice = regulatoryNoticeDao.selectOne(queryWrapper);
                if (notice != null) {
                    String content = notice.getContent();
                    Document document = Jsoup.parse(content);
                    Element element = document.getElementById("appData");
                    Elements children = element.children();
                    if (children.size() != 0) {
                        String comment = regulatoryNotice.getComment();
                        Element trEl = new Element("tr");
                        trEl.append(comment.replace("<td>1</td>", "<td>" + (children.size() + 1) + "</td>"));
                        element.appendChild(trEl);
                        String newContent = document.body().outerHtml();
                        LambdaUpdateWrapper<RegulatoryNotice> updateWrapper = new UpdateWrapper<RegulatoryNotice>().lambda();
                        updateWrapper.eq(RegulatoryNotice::getId, notice.getId());
                        updateWrapper.set(RegulatoryNotice::getContent, newContent);
                        regulatoryNoticeDao.update(null, updateWrapper);
                        return true;
                    }
                }
                regulatoryNotice.setComment(null);
            }
            regulatoryNoticeDao.insert(regulatoryNotice);

        } catch (Exception e) {
            //若插入失败，则根据cid进行更新
            try {
                LambdaUpdateWrapper<RegulatoryNotice> updateWrapper = new UpdateWrapper<RegulatoryNotice>().lambda();
                updateWrapper.eq(RegulatoryNotice::getSourceUrl, regulatoryNotice.getSourceUrl());
                updateWrapper.eq(RegulatoryNotice::getType, 0);
                updateWrapper.eq(RegulatoryNotice::getProcess, 0);
                regulatoryNoticeDao.update(regulatoryNotice, updateWrapper);
                failed.add("更新：" + regulatoryNotice.getSourceUrl());
            } catch (Exception ex) {
                log.info("数据更新失败，url={},cid={}", regulatoryNotice.getSourceUrl(), regulatoryNotice.getCid());
                return false;
            }
        } finally {
            atomicLong.incrementAndGet();
            lock.unlock();
        }
        return true;
    }


    /**
     * 获取正文
     *
     * @param contentElement
     * @param url
     * @return
     */
    public static String getContent(Element contentElement, String url, Analysis analysis) {
        if (contentElement == null)
            return null;
        //添加img标签
        Elements imgEleList = contentElement.select("img");
        if (!imgEleList.isEmpty()) {
            imgEleList.removeAttr("class");
            imgEleList.addClass("cDownFile");
        }
        //添加a标签
        Elements aEleList = contentElement.select("a");
        if (!aEleList.isEmpty()) {
            aEleList.removeAttr("class");
            aEleList.addClass("cDownFile");
        }
        //添加video标签
//        Elements viderEleList = contentElement.select("video");
//        if (!viderEleList.isEmpty()){
//            viderEleList.removeAttr("class");
//            viderEleList.addClass("cDownFile");
//        }

        Elements mainAttachList = contentElement.getElementsByClass("cDownFile");
        // 修改标签格式
        for (Element element : mainAttachList) {
            //src 一般是 img 标签的属性 href 一般是 a 标签的属性
            String key = "src";
            String value = element.attr(key);
            if (StringUtils.isBlank(value)) {
                key = "href";
                value = element.attr(key);
            }

            if (StringUtils.isNotBlank(value) && (value.startsWith("/") || value.startsWith("./") || !value.contains("http"))) {
                if (value.startsWith("/")) {
                    String[] split = url.split("/");
                    StringBuilder stringBuilder = new StringBuilder();
                    int count = 0;
                    for (int i = 0; i < split.length; i++) {
                        String s = split[i];
                        if (count == 3)
                            break;
                        if (s.isEmpty()) {
                            stringBuilder.append("/");
                            count = count + 1;
                            continue;
                        }
                        count = count + 1;
                        stringBuilder.append(s).append("/");
                    }
                    stringBuilder.append(value.substring(1));
                    value = stringBuilder.toString();
                } else {
                    int index = url.lastIndexOf("/");
                    String uri = url.substring(0, index);
                    value = uri + value.substring(1);
                }
//                else {
//                    int i = url.lastIndexOf("/");
//                    value = url.substring(0, i + 1) + value;
//                }
            }

            //如果两个都不存在url则移除
            if (StringUtils.isBlank(value) || StringUtils.containsAny(value, "data:image/png", ".html", "javascript:window", "javascript:void(0)", "javascript:window.print()", "javascript:") || value.equals("#")) {
                element.remove();
                continue;
            }
            //url过滤，处理
            String newUrl = analysis.DealContentUrl(value);
            if (newUrl == null) {
                element.remove();
                continue;
            }
            value = newUrl;
            List<Attribute> attributes = element.attributes().asList();
            final String attrName = key;
            attributes.stream().filter(attr -> attr.getKey().equals(attrName)).findFirst().get().setValue(value);
        }

        Whitelist whitelist = JsoupUtil.iCourtWhiteList();
//        whitelist.addAttributes("video","src");
        String content = JsoupUtil.htmltoText(contentElement.outerHtml(), whitelist);
        content = content.replace("《br》", "\n");
        return JsoupUtil.strFormat(content);
    }

    @After
    public void after() {
        System.out.println("failed:");
        System.out.println("failed count = " + failed.size());
        System.out.print("successed count = " + atomicLong.get());
    }


    // 填充需要解析的表名和地域信息
    static {
        String tablesText = ResourceUtil.readUtf8Str("tables.txt");
        String[] tables = tablesText.split("\n");
        for (int i = 0; i < tables.length; i++) {
            String table = tables[i];
            tableList.add(table);
        }
        try {
            connection = DriverManager.getConnection("jdbc:postgresql://172.16.71.102:5432/crawlerdb", "postgres", "Ytk3Z21May7iUwkD");
        } catch (SQLException e) {
        }
    }
}
