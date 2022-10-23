package com.icourt.service.compliance.ext.impl;


import com.icourt.entity.compliance.TopicMatchResult;
import com.icourt.service.compliance.ext.TopicExtractFunctionService;

import java.util.List;

/**
 * 分类分级 主题 自定义抽取类
 */
public class ClassifyServiceImpl implements TopicExtractFunctionService {

    /**
     * 自定义规则提取主题
     * 【数据】与【分级】或【分类】中间间隔不超过3个字
     *
     * @return
     */
//    public Set<String> extract(String str) {
//        Set<String> keywords1 = regexExtract("数据[\\s\\S]{0,3}分级", str);
//        Set<String> keywords2 = regexExtract("分级[\\s\\S]{0,3}数据", str);
//        Set<String> keywords3 = regexExtract("数据[\\s\\S]{0,3}分类", str);
//        Set<String> keywords4 = regexExtract("分类[\\s\\S]{0,3}数据", str);
//        keywords1.addAll(keywords2);
//        keywords1.addAll(keywords3);
//        keywords1.addAll(keywords4);
//        return keywords1;
//    }
    @Override
    public List<TopicMatchResult> extract(Object obj) {
        return null;
    }
}
