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
import com.icourt.util.JsoupUtil;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.mapper.compliance.RegulatoryNoticeDao;
import com.icourt.parse.analysis.Analysis;
import com.icourt.parse.analysis.AnalysisFactory;
import com.icourt.parse.analysis.Impl.GatShandong2Analysis;
import com.icourt.service.compliance.ext.TopicExtractDomainService;
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


    //??????????????????
    final List<String> failed = new ArrayList<>();
    //???????????????
    final static List<String> tableList = new ArrayList<>();

    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 100,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(90000));
    //pg??????connection
    static Connection connection = null;
    AtomicLong atomicLong = new AtomicLong();
    //?????????????????????????????????????????????????????????
    CountDownLatch countDownLatch = null;
    //???????????????????????????
    int limitSize = 1000;
    //?????????????????????/???
    volatile static int count = 0;

    /**
     * ??????????????????
     *
     * @throws Exception
     */
    @Test
    public void analysis() throws Exception {
        log.info("????????????????????????...");
        Class.forName("org.postgresql.Driver");  //???????????????
        //??????
        AtomicInteger atomicInteger = new AtomicInteger();
        try {
            for (int i = 0; i < tableList.size(); i++) {
                String table = tableList.get(i);
                if (!StringUtils.containsAny(table, "www_zjzwfw_gov_cn111", "www_zjzwfw_gov_cn111"))
                    continue;

                log.info("??????????????????{}", table);

                //2.?????????????????????,???sql????????????????????????????????????
                //? ???????????????
                String querySql = "select id,page_url as pageUrl,main_json as mainJson,ext_json as extJson,code from " + table + " where stored_to_db = 0 LIMIT " + limitSize;
                String selectCount = "select count(*) as count from " + table + " where stored_to_db in (0,1)";
                //?????????
                do {
                    ResultSet countResult = executeSql(selectCount);
                    if (countResult.next()) {
                        String countStr = countResult.getString("count");
                        if (StringUtils.isNotEmpty(countStr))
                            count = Integer.valueOf(countStr);
                    }
                    if (count < 1)
                        break;
                    System.out.println("\n???????????????=" + count + "\n");
                    int newCount = count >= limitSize ? limitSize : count;
                    //?????????5???????????????????????????
                    countDownLatch = new CountDownLatch((newCount - 3) > 0 ? newCount : 3);
                    System.out.println("??????sql??????...");
                    long startQueryTime = System.currentTimeMillis();
                    ResultSet resultSet = executeSql(querySql);
                    long endQueryTime = System.currentTimeMillis();
                    System.out.println("\n???????????????????????????=====" + (endQueryTime - startQueryTime) / 1000 + "\n");
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
                            log.info("???{}?????????????????????,url={}...", atomicInteger.incrementAndGet(), noticeSource.getUrl());
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

    //????????????
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
                //???????????????
                Boolean result = analysisRow(noticeSource);
                if (result != null && result) {
                    //???????????????
                    sql = updateSql.format(updateSql, 2, noticeSource.getId());
                } else { //????????????????????????storeToDb??????
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
     * ???????????????
     */
    @Test
    public void reAnalysis() throws InterruptedException {
        //??????????????????
        int size = 1000;
        LambdaQueryWrapper<RegulatoryNotice> queryWrapper = new QueryWrapper<RegulatoryNotice>().lambda();
        queryWrapper.eq(RegulatoryNotice::getType, 0);
        queryWrapper.eq(RegulatoryNotice::getNoticeOrgan, "??????????????????");
        //?????????????????????
        Integer count = regulatoryNoticeDao.selectCount(queryWrapper);

        queryWrapper.select(RegulatoryNotice::getId, RegulatoryNotice::getTitle, RegulatoryNotice::getContent);

        //??????
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
     * ?????? sql???????????????
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
     * ?????????????????????
     *
     * @param noticeSource
     * @throws SQLException
     */
    public Boolean analysisRow(NoticeSource noticeSource) throws SQLException {


        Analysis analysis = noticeSource.getAnalysis();
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        countDownLatch.countDown();
        //???????????????
        try {
            analysis.preProcessor(noticeSource);
        } catch (Exception e) {
            return false;
        }

        if (StringUtils.isBlank(noticeSource.getHtml()))
            noticeSource.setHtml(noticeSource.getValue());

        //???value?????????????????????,??????content
        if (StringUtils.isEmpty(regulatoryNotice.getContent()) && analysis.haveHtml()) {
            formatHtml(noticeSource);
        }

        regulatoryNotice = buildEntity(noticeSource);
        //??????RegulatoryNotice???????????????????????????
        return save(regulatoryNotice);
    }


    /**
     * ???????????????????????????????????????
     *
     * @param regulatoryNotice
     */
    public boolean save(RegulatoryNotice regulatoryNotice) {
        if (regulatoryNotice == null) {
            return false;
        }
        //??????????????????
        boolean success = saveOrUpdate(regulatoryNotice);
        if (success) {
            log.info("?????????????????????title={},url={}", regulatoryNotice.getTitle(), regulatoryNotice.getSourceUrl());
        }
        return success;
    }


    /**
     * ??????html
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
        html = html.replaceAll("<[\\s]*?br[^>]*>", "???br???");
        Document document = Jsoup.parse(html);
        //?????????????????????element
        Element element = analysis.getContentElement(document, url);
        //??????element???????????????
        content = getContent(element, url, analysis);
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();
        regulatoryNotice.setContent(content);

        if (StringUtils.isBlank(content))
            log.info("html???????????????url={}", noticeSource.getUrl());
    }


    /**
     * ??????/?????????????????????
     *
     * @param noticeSource
     * @return
     */
    private RegulatoryNotice buildEntity(NoticeSource noticeSource) {
        Analysis analysis = noticeSource.getAnalysis();
        RegulatoryNotice regulatoryNotice = noticeSource.getRegulatoryNotice();

        //0 ??????  1 ??????  2 ??????
        regulatoryNotice.setProcess(0);
        //???url
        if (StringUtils.isBlank(regulatoryNotice.getSourceUrl()))
            regulatoryNotice.setSourceUrl(noticeSource.getUrl());
        //??????id
        regulatoryNotice.setThirdId(noticeSource.getThirdId());
        //????????????
        regulatoryNotice.setNoticeMainBody(analysis.mainBody());
        //????????????
        regulatoryNotice.setCreateTime(LocalDateTime.now());

        String extJson = noticeSource.getExtJson();

        //????????????title???publish???province ??????????????????
        if (StringUtils.isNotEmpty(extJson)) {
            //??????ext_json
            analysis.delExtJson(extJson, regulatoryNotice);
        }

        //????????????
        analysis.filterContent(regulatoryNotice);

        //??????regulatoryNotice??????
        if (regulatoryNotice.getTitle() == null && regulatoryNotice.getPublishTime() == null && regulatoryNotice.getProvince() == null)
            analysis.fillInfo(regulatoryNotice);

        String content = regulatoryNotice.getContent();
        if (StringUtils.isBlank(content))
            return null;
        regulatoryNotice.setContent(content.replaceAll("(???|??)", ""));
        //?????????????????????5000????????????
        analysis.filterTitle(regulatoryNotice);

        if (regulatoryNotice.getProvince() == null)
            regulatoryNotice.setProvince("??????");

        //??????id
        String deduplication = analysis.getDeduplication(regulatoryNotice);
        String cid = MD5Util.md5Hex(deduplication.getBytes());
        regulatoryNotice.setCid(cid);

        //???????????????
        extractDomainService.extractTopicFromNotice(regulatoryNotice);

        return regulatoryNotice;
    }

    final ReentrantLock lock = new ReentrantLock(false);

    private boolean saveOrUpdate(RegulatoryNotice regulatoryNotice) {
        try {
            if (regulatoryNotice.getNoticeOrgan().equals("?????????????????????????????????????????????")) {
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
            //???????????????????????????cid????????????
            try {
                LambdaUpdateWrapper<RegulatoryNotice> updateWrapper = new UpdateWrapper<RegulatoryNotice>().lambda();
                updateWrapper.eq(RegulatoryNotice::getSourceUrl, regulatoryNotice.getSourceUrl());
                updateWrapper.eq(RegulatoryNotice::getType, 0);
                updateWrapper.eq(RegulatoryNotice::getProcess, 0);
                regulatoryNoticeDao.update(regulatoryNotice, updateWrapper);
                failed.add("?????????" + regulatoryNotice.getSourceUrl());
            } catch (Exception ex) {
                log.info("?????????????????????url={},cid={}", regulatoryNotice.getSourceUrl(), regulatoryNotice.getCid());
                return false;
            }
        } finally {
            atomicLong.incrementAndGet();
            lock.unlock();
        }
        return true;
    }


    /**
     * ????????????
     *
     * @param contentElement
     * @param url
     * @return
     */
    public static String getContent(Element contentElement, String url, Analysis analysis) {
        if (contentElement == null)
            return null;
        //??????img??????
        Elements imgEleList = contentElement.select("img");
        if (!imgEleList.isEmpty()) {
            imgEleList.removeAttr("class");
            imgEleList.addClass("cDownFile");
        }
        //??????a??????
        Elements aEleList = contentElement.select("a");
        if (!aEleList.isEmpty()) {
            aEleList.removeAttr("class");
            aEleList.addClass("cDownFile");
        }
        //??????video??????
//        Elements viderEleList = contentElement.select("video");
//        if (!viderEleList.isEmpty()){
//            viderEleList.removeAttr("class");
//            viderEleList.addClass("cDownFile");
//        }

        Elements mainAttachList = contentElement.getElementsByClass("cDownFile");
        // ??????????????????
        for (Element element : mainAttachList) {
            //src ????????? img ??????????????? href ????????? a ???????????????
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

            //????????????????????????url?????????
            if (StringUtils.isBlank(value) || StringUtils.containsAny(value, "data:image/png", ".html", "javascript:window", "javascript:void(0)", "javascript:window.print()", "javascript:") || value.equals("#")) {
                element.remove();
                continue;
            }
            //url???????????????
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
        content = content.replace("???br???", "\n");
        return JsoupUtil.strFormat(content);
    }

    @After
    public void after() {
        System.out.println("failed:");
        System.out.println("failed count = " + failed.size());
        System.out.print("successed count = " + atomicLong.get());
    }


    // ??????????????????????????????????????????
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
