package com.icourt.schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * 合规解析定时任务
 */
@Slf4j
@EnableAsync
@Configuration
@EnableScheduling
@Profile("online")
public class ComplianceSchedule {

}
