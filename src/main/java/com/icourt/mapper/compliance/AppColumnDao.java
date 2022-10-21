package com.icourt.mapper.compliance;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.icourt.entity.compliance.db.AppColumn;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@DS("tidbBigdata")
@Mapper
public interface AppColumnDao extends BaseMapper<AppColumn> {


    /**
     * 提交上线
     *
     * @param appColumn
     * @return
     */
    @Update("update app_column set `type`=#{type},first_upload_time=#{firstUploadTime},process=#{process},upload_time=#{uploadTime},check_time=#{updateTime},auditor=#{auditor},reason=#{reason} where id=#{id}")
    int submitById(AppColumn appColumn);

    /**
     * 逻辑删除
     *
     * @param appColumn
     * @return
     */
    @Update("update app_column set `type`=#{type},process=#{process},update_time=#{updateTime},modifier=#{modifier} where id=#{id}")
    int updateDelById(AppColumn appColumn);

    /**
     * 打回、报错等
     *
     * @param appColumn
     * @return
     */
    @Update("update app_column set `type`=#{type},process=#{process},check_time=#{updateTime},auditor=#{auditor},reason=#{reason} where id=#{id}")
    int updateTypeById(AppColumn appColumn);

    /**
     * 列表搜索
     * @param wrapper
     * @return
     */
    @Select("select a.id,a.process,a.type,a.aid,a.app_name,a.notice_date,a.create_time,a.update_time,a.auditor,a.upload_time,a.modifier,a.notice_source_title,a.check_time,a.comment " +
            "from app_column a left join app_column_problem b on a.id=b.app_id and b.del=0 " +
            "${ew.customSqlSegment}")
    List<AppColumn> selectList(@Param("ew") Wrapper wrapper);


    @Select("select a.id " +
            "from app_column a left join app_column_problem b on a.id=b.app_id and b.del=0 " +
            "${ew.customSqlSegment}")
    IPage<AppColumn> selectIds(IPage<AppColumn> iPage, @Param("ew") Wrapper wrapper);
}
