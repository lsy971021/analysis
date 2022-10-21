package com.icourt.mapper.compliance;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icourt.entity.compliance.db.AppColumnProblem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@DS("tidbBigdata")
@Mapper
public interface AppColumnProblemDao extends BaseMapper<AppColumnProblem> {

    @Select("update app_column_problem set del=1 where app_id=#{id}")
    void updateDel(@Param("id") Long id);
}
