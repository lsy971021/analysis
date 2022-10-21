package com.icourt.service.parse;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * 数据读取器
 * @param <T> 数据存储对象
 */
public interface SourceReader<T>{

    /**
     * 通过mybatis-plus 读取需要提取的数据
     * @param queryWrapper
     * @return
     */
    List<T> ReadData(QueryWrapper queryWrapper);


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

}
