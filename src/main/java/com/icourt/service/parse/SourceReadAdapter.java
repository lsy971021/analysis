package com.icourt.service.parse;

import com.icourt.service.parse.source.ISource;

import java.sql.SQLException;
import java.util.List;

/**
 * 数据读取适配器
 * 通过实现 ISource 实现多种方式数据源读取
 * @param <T> 数据存储对象
 */
public interface SourceReadAdapter<T>{

    /**
     * 读取需要提取的数据
     * @return
     */
    List<T> ReadData();


    /**
     * 读取单条数据
     * @return
     */
    T readOne();

    /**
     * 更新
     */
    void update();

    /**
     * 数量统计
     * @return
     */
    Integer getCount();

    /**
     * 获取数据源
     * @return
     */
    ISource getSource();

}
