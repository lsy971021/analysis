package com.icourt.mapper.compliance;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.icourt.entity.compliance.db.RegulatoryNotice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@DS("tidbBigdata")
@Mapper
public interface RegulatoryNoticeDao extends BaseMapper<RegulatoryNotice> {

    /**
     * 逻辑删除
     *
     * @param regulatoryNotice
     * @return
     */
    @Update("update regulatory_notice set `type`=#{type},process=#{process},update_time=#{updateTime},modifier=#{modifier} where id=#{id}")
    int updateDelById(RegulatoryNotice regulatoryNotice);


    /**
     * 提交上线
     * @param regulatoryNotice
     * @return
     */
    @Update("update regulatory_notice set `type`=#{type},first_upload_time=#{firstUploadTime},process=#{process},upload_time=#{uploadTime},check_time=#{updateTime},auditor=#{auditor},reason=#{reason} where id=#{id}")
    int submitById(RegulatoryNotice regulatoryNotice);

    /**
     * 打回、报错等
     * @param regulatoryNotice
     * @return
     */
    @Update("update regulatory_notice set `type`=#{type},process=#{process},check_time=#{updateTime},auditor=#{auditor},reason=#{reason} where id=#{id}")
    int updateTypeById(RegulatoryNotice regulatoryNotice);

    /**
     * 重复数据
     * @return
     */
    @Select("select * from (select source_url,id,type,modifier,create_time,RANK() over(partition by source_url ORDER BY create_time asc) m from regulatory_notice ) a where a.m>1 and a.modifier is null")
    List<RegulatoryNotice> getAll();
}
