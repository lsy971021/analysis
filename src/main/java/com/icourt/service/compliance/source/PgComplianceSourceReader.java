package com.icourt.service.compliance.source;

import com.icourt.entity.compliance.parse.NoticeSource;
import com.icourt.service.parse.source.ISource;
import com.icourt.service.parse.source.abs.AbstractJdbcSourceReader;
import com.icourt.service.parse.source.impl.JdbcSourceConnect;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PgComplianceSourceReader extends AbstractJdbcSourceReader<NoticeSource> {

    JdbcSourceConnect jdbcSourceConnect = super.jdbcSourceConnect;

    public PgComplianceSourceReader(ISource source) {
        super(source);
    }

    @Override
    public List<NoticeSource> ReadData() {
        ResultSet resultSet = null;
        try {
            resultSet = execute(jdbcSourceConnect.getListSql());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (resultSet != null) {
            List<NoticeSource> list = new ArrayList<>();
            try {
                while (resultSet.next()) {
                    NoticeSource noticeSource = new NoticeSource();
                    noticeSource.setId(Long.valueOf(resultSet.getString("id")));
                    noticeSource.setExtJson(resultSet.getString("extJson"));
                    noticeSource.setUrl(resultSet.getString("pageUrl"));
                    noticeSource.setThirdId(resultSet.getString("code"));
                    list.add(noticeSource);
                }
                return list;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public NoticeSource readOne() {
        return null;
    }

    @Override
    public void update() {

    }

    @Override
    public Integer getCount() {
        return null;
    }

    @Override
    public ISource getSource() {
        return super.jdbcSourceConnect;
    }
}
