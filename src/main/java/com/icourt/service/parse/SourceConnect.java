package com.icourt.service.parse;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.sql.Connection;


/**
 * 数据源连接
 */
public interface SourceConnect {

    /**
     * 获取jdbc连接
     * @return
     */
    Connection getConnect();

    /**
     * 获取mybatis Mapper
     * @return
     */
    BaseMapper<? extends Object> getBaseMapper();

}
