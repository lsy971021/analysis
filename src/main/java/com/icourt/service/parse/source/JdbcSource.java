package com.icourt.service.parse.source;


import java.sql.Connection;

/**
 * 通过jdbc获取数据
 */
public interface JdbcSource extends ISource{

    Connection getConnect();

    void closeConnect();

}
