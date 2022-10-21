package com.icourt.service.parse.abs;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.icourt.service.parse.SourceConnectReader;

import java.util.List;

/**
 * 通过mybatis
 * @param <T>
 */
public abstract class AbstractMybatisConnectSourceReader<T,R> implements SourceConnectReader<T> {


    List getList(QueryWrapper queryWrapper){
        return getBaseMapper().selectList(queryWrapper);
    }

}
