package com.icourt.service.parse.source.abs;


import com.icourt.service.parse.SourceReadAdapter;
import com.icourt.service.parse.source.ISource;
import com.icourt.service.parse.source.JdbcSource;
import com.icourt.service.parse.source.impl.JdbcSourceConnect;
import org.apache.commons.lang3.StringUtils;

import java.sql.*;

/**
 * 抽象类
 * 通过jdbc读取数据，返回 <T> 类型格式
 * @param <T>
 */
public abstract class AbstractJdbcSourceReader<T> implements SourceReadAdapter<T> {


    private Connection connection;

    protected JdbcSourceConnect jdbcSourceConnect;

    public AbstractJdbcSourceReader(ISource source) {
        JdbcSourceConnect jdbcSourceConnect = (JdbcSourceConnect) source;
        this.jdbcSourceConnect = jdbcSourceConnect;
        this.connection = jdbcSourceConnect.getConnect();
    }

    /**
     * 支持查询、修改操作
     *
     * @param sql 要执行的sql
     * @return ResultSet 结果
     * @throws SQLException
     */
    protected ResultSet execute(String sql) throws SQLException {
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
     * @param sql    sql语句
     * @param column 需查询字段
     * @return 返回colum字段值
     * @throws SQLException
     */
    protected String selectAndGetOneStringValue(String sql, String column) throws SQLException {
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
