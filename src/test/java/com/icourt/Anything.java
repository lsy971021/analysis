package com.icourt;

import cn.hutool.core.io.resource.ResourceUtil;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.parse.AnalysisSource;
import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.parse.analysis.Analysis;
import com.icourt.parse.analysis.Impl.MiitAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;


@Slf4j
public class Anything {

    @Test
    public void test() throws Exception {
    }


    /**
     * 解析html，获取正文
     */
    @Test
    public void analysisContent() {
        String url = "https://zjca.miit.gov.cn/xwdt/gzdt/jgdt/art/2022/art_0f547df98e9e4b3d8998191064d25a4a.html";
        Analysis analysis = new MiitAnalysis();

        NoticeSource noticeSource = new NoticeSource();
        String html = ResourceUtil.readUtf8Str("anything.html");
        noticeSource.setUrl(url);
        noticeSource.setHtml(html);
        noticeSource.setAnalysis(analysis);
        RegulatoryNotice regulatoryNotice = new RegulatoryNotice();
        noticeSource.setRegulatoryNotice(regulatoryNotice);
        new AnalysisSource().formatHtml(noticeSource);
        analysis.filterContent(regulatoryNotice);
    }
}
