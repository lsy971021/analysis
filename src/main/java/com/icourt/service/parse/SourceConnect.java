package com.icourt.service.parse;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.poi.ss.formula.functions.T;

import java.sql.Connection;


/**
 * 数据源连接
 */
public interface SourceConnect extends BaseMapper<T> {

    /**
     * 获取jdbc连接
     * @return
     */
    Connection getConnect();


}
