package com.icourt.service.parse.source;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * 通过mybatis获取数据
 */
public interface MybatisSource extends ISource{

    /**
     * 获取mybatis Mapper
     * @return
     */
    BaseMapper<? extends Object> getBaseMapper();
}
