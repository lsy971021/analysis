package com.icourt.schedule;

import cn.hutool.core.io.resource.ResourceUtil;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.service.compliance.source.PgComplianceSourceReader;
import com.icourt.service.parse.source.ISource;
import com.icourt.service.parse.source.impl.JdbcSourceConnect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 合规解析定时任务
 */
@Slf4j
@EnableAsync
@Configuration
@EnableScheduling
@Profile("online")
public class ComplianceSchedule {

    final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(20, 100,
            60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(90000));
    Integer limitSize = 1000;


    /**
     * 每 6h 执行一次
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000,fixedRate = 6 * 60 * 60 * 1000)
    public void task() {
        //读取需解析的表
        String tablesText = ResourceUtil.readUtf8Str("tables.txt");
        String[] tables = tablesText.split("\n");
        List<String> tableList = Arrays.asList(tables);

        JdbcSourceConnect jdbcSourceConnect = new JdbcSourceConnect(
                "org.postgresql.Driver"
                , "jdbc:postgresql://172.16.71.102:5432/crawlerdb"
                , "postgres"
                , "Ytk3Z21May7iUwkD");


        for (String table : tableList) {
            String querySql = "select id,page_url as pageUrl,main_json as mainJson,ext_json as extJson,code from " + table + " where stored_to_db = 0 LIMIT " + limitSize;
            jdbcSourceConnect.setListSql(querySql);


            PgComplianceSourceReader pgSourceReader = new PgComplianceSourceReader(jdbcSourceConnect);

            List<NoticeSource> noticeSources = pgSourceReader.ReadData();

            noticeSources.forEach(noticeSource -> {





            });



        }


        jdbcSourceConnect.closeConnect();
    }


}
