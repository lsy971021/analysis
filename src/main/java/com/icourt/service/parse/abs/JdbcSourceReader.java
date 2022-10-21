package com.icourt.service.parse.abs;


import com.icourt.service.parse.SourceReader;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;

/**
 * jdbc读取数据
 * @param <T>
 */
public abstract class JdbcSourceReader<T> implements SourceReader<T> {

    /**
     * 数据库连接connection
     */
    private Connection connection = null;

    /**
     * 数据库连接
     *
     * @param url      数据库连接url
     * @param username 数据库连接username
     * @param password 数据库连接password
     */
    public JdbcSourceReader(String jdbcDriver,String url, String username, String password) {
        try {
            Class.forName(jdbcDriver);  //反射运行时
//            connection = DriverManager.getConnection("jdbc:postgresql://172.16.71.102:5432/crawlerdb", "postgres", "Ytk3Z21May7iUwkD");
            connection = DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
        }
    }


    /**
     * 支持查询、修改操作
     * @param sql 要执行的sql
     * @return ResultSet 结果
     * @throws SQLException
     */
    ResultSet execute(String sql) throws SQLException {
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet resultSet;
        if (sql.contains("select")) {
            resultSet = pst.executeQuery();
            return resultSet;
        } else {
            pst.executeUpdate();
            return null;
        }
    }


    /**
     * @param sql sql语句
     * @param column 需查询字段
     * @return  返回colum字段值
     * @throws SQLException
     */
    String selectAndGetOneStringValue(String sql, String column) throws SQLException {
        if (StringUtils.isAnyBlank(sql, column)) {
            throw new RuntimeException("sql或column不能为空");
        }
        if (!sql.contains("select")) {
            throw new RuntimeException("仅支持查询语句");
        }
        PreparedStatement pst = connection.prepareStatement(sql);
        ResultSet resultSet;
        resultSet = pst.executeQuery();
        return resultSet.next() ? resultSet.getString(column) : null;
    }

}
