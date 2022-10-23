package com.icourt.service.compliance.ext.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.icourt.dsmanager.mapper.db.dao.ConfTopicKeywordsDao;
import com.icourt.dsmanager.mapper.db.dao.ConfTopicRuleDao;
import com.icourt.dsmanager.mapper.db.dataobj.ConfTopicKeywords;
import com.icourt.dsmanager.mapper.db.dataobj.ConfTopicRule;
import com.icourt.dsmanager.mapper.db.dataobj.TopicExtractResult;
import com.icourt.dsmanager.mapper.db.dataobj.TopicMatchResult;
import com.icourt.dsmanager.service.extract.TopicExtractFunctionService;
import com.icourt.dsmanager.service.extract.TopicExtractService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.icourt.dsmanager.util.ComplianceConstant.*;

/**
 * 监管通报 主题抽取服务
 */
@Slf4j
@Service
public class TopicExtractServiceImpl implements TopicExtractService {

    private List<ConfTopicRule> topicList;
    private Map<String, List<ConfTopicKeywords>> keywordsMap;
    //自定义规则提取器
    private Map<String, Class<TopicExtractFunctionService>> extractorMap = new HashMap<>();

    @Autowired
    public TopicExtractServiceImpl(ConfTopicRuleDao topicDao, ConfTopicKeywordsDao keywordsDao) {
        //加载主题词 提取规则
        try {
            topicList = topicDao.selectList(new LambdaQueryWrapper<ConfTopicRule>().eq(ConfTopicRule::getBizCode, "1001"))
                    .stream().collect(Collectors.toList());
            keywordsMap = keywordsDao.selectList(new LambdaQueryWrapper<ConfTopicKeywords>()
                    .eq(ConfTopicKeywords::getBizCode, "1001"))
                    .stream().collect(Collectors.groupingBy(obj -> obj.getTopic()));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("加载主题提取规则失败，本次不再提取主题，程序继续解析...", e);
        }
    }

    /**
     * 抽取主题
     *
     * @param dataObj
     * @return
     */
    @Override
    public TopicExtractResult extractTopic(Object dataObj) {
        if (topicList == null || topicList.isEmpty() || dataObj == null) return null;
        List<TopicMatchResult> totalResultList = topicList.stream().flatMap(topicObj -> {
            List<TopicMatchResult> matchResultList = topicObj.getFunction() != null
                    //自定义规则提取主题
                    ? matchSelfFunction(topicObj.getFunction(), dataObj)
                    //按关键词提取主题
                    : matchKeywords(topicObj, dataObj);
            return matchResultList == null || matchResultList.isEmpty() ? null : matchResultList.stream();
        }).filter(result -> result != null).collect(Collectors.toList());
        if (totalResultList == null || totalResultList.size() == 0) return null;

        String topics = totalResultList.stream().map(result -> result.getTopic()).distinct().collect(Collectors.joining(";"));
        String keywords = totalResultList.stream().flatMap(result -> Arrays.stream(result.getKeywords().split(";")))
                .distinct().collect(Collectors.joining(";"));
        return new TopicExtractResult(topics, keywords, totalResultList);
    }

    /**
     * 按关键词提取主题
     *
     * @param topicObj
     * @param dataObj
     * @return
     */
    private List<TopicMatchResult> matchKeywords(ConfTopicRule topicObj, Object dataObj) {
        List<ConfTopicKeywords> topicKeywords = keywordsMap.get(topicObj.getTopic());
        if (topicKeywords == null || topicKeywords.isEmpty()) return null;
        JSONObject jsonObj = (JSONObject) JSONObject.toJSON(dataObj);
        //匹配每一类的关键词
        List<List<TopicMatchResult>> totalResultList = topicKeywords.stream().map(conf -> {
            String[] locations = StringUtils.isBlank(conf.getLocation()) ? new String[]{"title", "content"} : conf.getLocation().split(",");
            List<TopicMatchResult> resultList = Arrays.stream(locations).map(location -> {
                if (LOCATION_PARAGRAPH.equalsIgnoreCase(location)) {
                    //匹配同段落
                    JSONArray paragraphJsonArray = jsonObj.getJSONArray(location);
                    List<String> paragraphList;
                    if (paragraphJsonArray == null || paragraphJsonArray.isEmpty()) {
                        //如果段落为空，则取正文，按\n分割
                        String content = jsonObj.getString("content");
                        if (StringUtils.isBlank(content)) return null;
                        paragraphList = Arrays.asList(content.split("\n"));
                    } else {
                        paragraphList = paragraphJsonArray.stream().map(obj -> ((JSONObject) obj).getString("text")).collect(Collectors.toList());
                    }
                    Set<String> keywordSet = paragraphList.stream().flatMap(text -> {
                        Set<String> keywords = match(conf, text);
                        return keywords == null || keywords.isEmpty() ? null : keywords.stream();
                    }).filter(keyword -> keyword != null).collect(Collectors.toSet());
                    if (keywordSet == null || keywordSet.isEmpty()) return null;
                    return new TopicMatchResult(conf.getTopic(), PARAGRAPH_HIT, String.join(";", keywordSet));
                } else {
                    //匹配标题、正文
                    String notParagraph = jsonObj.getString(location);
                    Set<String> keywordSet = match(conf, notParagraph);
                    if (keywordSet == null || keywordSet.isEmpty()) return null;
                    String matchType = LOCATION_TITLE.equalsIgnoreCase(location) ? TITLE_HIT : CONTENT_HIT;
                    return new TopicMatchResult(conf.getTopic(), matchType, String.join(";", keywordSet));
                }
            }).filter(result -> result != null).collect(Collectors.toList());
            return resultList;
        }).collect(Collectors.toList());

        //过滤为空的
        List<List<TopicMatchResult>> filterLists = totalResultList.stream().filter(list -> list != null && !list.isEmpty()).collect(Collectors.toList());
        if (topicObj.getCondition() != null && "&".equals(topicObj.getCondition()) && filterLists.size() != totalResultList.size()) {
            //各条件 且的关系
            return null;
        }
        //各条件 或的关系
        return filterLists.stream().flatMap(list -> list.stream()).collect(Collectors.toList());
    }

    private Set<String> match(ConfTopicKeywords conf, String text) {
        if (StringUtils.isBlank(text)) return null;
        Set<String> matchKeywords;
        if (StringUtils.isNotBlank(conf.getOtherWords())) {
            //有 附关键词
            String primaryWord = conf.getKeywords();
            String[] otherKeywords = conf.getOtherWords().split("、");
            matchKeywords = Arrays.stream(otherKeywords)
                    .filter(otherWord -> text.contains(otherWord) && text.contains(primaryWord))
                    .collect(Collectors.toSet());
            if (matchKeywords != null && !matchKeywords.isEmpty()) {
                matchKeywords.add(primaryWord);
            }
        } else {
            //无 附关键词
            matchKeywords = Arrays.stream(conf.getKeywords().split("、"))
                    .filter(keyword -> text.contains(keyword)).collect(Collectors.toSet());
        }
        return matchKeywords;
    }

    /**
     * 自定义规则提取主题
     *
     * @param function
     * @return
     */
    private List<TopicMatchResult> matchSelfFunction(String function, Object obj) {
        try {
            Class<TopicExtractFunctionService> extractor = extractorMap.get(function);
            if (extractor == null) {
                extractor = (Class<TopicExtractFunctionService>) Class.forName(function);
                //放入缓存
                extractorMap.put(function, extractor);
            }
            //反射执行自定义处理类的extract方法
            return (List<TopicMatchResult>) extractor.getDeclaredMethod("extract", Object.class)
                    .invoke(extractor.getDeclaredConstructor().newInstance(), obj);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("自定义函数：反射执行[{}]失败...", function, e);
            return null;
        }
    }
}
