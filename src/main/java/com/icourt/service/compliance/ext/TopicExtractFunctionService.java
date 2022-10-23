package com.icourt.service.compliance.ext;


import com.icourt.entity.compliance.TopicMatchResult;

import java.util.List;

public interface TopicExtractFunctionService {

    /**
     * 自定义规则提取主题
     */
    List<TopicMatchResult> extract(Object obj);

}
