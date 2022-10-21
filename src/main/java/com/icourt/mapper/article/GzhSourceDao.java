package com.icourt.mapper.article;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.icourt.entity.compliance.db.GzhSource;
import com.icourt.entity.compliance.db.GzhSourceExtendBo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@DS("tidbCrawdata")
@Mapper
public interface GzhSourceDao extends BaseMapper<GzhSource> {
    @Select(
            "select id,biz,html,create_time,mid,publish_time,author,title,content_url,cover_url " +
                    "from gzh_source " +
                    "where is_resolving=0 and category=0 and exist_html=0"
    )
    IPage<GzhSourceExtendBo> selectAllAndPartnerPage(IPage<GzhSourceExtendBo> page);

    @Select(
            "select id,html,mid,publish_time,title,content_url " +
                    "from gzh_source " +
                    "where is_resolving=0 and category=1 and exist_html=0"
    )
    IPage<GzhSource> selectAllForCompliance(IPage<GzhSource> page);
}
