package com.icourt.service.parse.source.impl;


import com.icourt.service.parse.source.JdbcSource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 通过jdbc连接数据源
 */
public class JdbcSourceConnect implements JdbcSource {
    // 数据库驱动
    private final String jdbcDriver;
    //连接数据库url
    private final String url;
    //用户名
    private final String username;
    //密码
    private final String password;

    private Connection connection;

    private String updateSql;
    private String countSql;
    //获取list->sql
    private String listSql;
    private String column;

    public JdbcSourceConnect(String jdbcDriver, String url, String username, String password) {
        this.jdbcDriver = jdbcDriver;
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public JdbcSourceConnect( String url, String username, String password) {
        // 默认为mysql
        this.jdbcDriver = "com.mysql.jdbc.Driver";
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnect() {
        try {
            Class.forName(jdbcDriver);  //反射运行时
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
        }
        return connection;
    }

    @Override
    public void closeConnect() {
        try {
            connection.close();
        } catch (SQLException e) {
        }
    }

    public String getUpdateSql() {
        return updateSql;
    }

    public void setUpdateSql(String updateSql) {
        this.updateSql = updateSql;
    }

    public String getCountSql() {
        return countSql;
    }

    public void setCountSql(String countSql) {
        this.countSql = countSql;
    }

    public String getListSql() {
        return listSql;
    }

    public void setListSql(String listSql) {
        this.listSql = listSql;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }
}
