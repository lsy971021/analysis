package com.icourt.entity.compliance.parse;

import com.icourt.entity.compliance.db.RegulatoryNotice;
import com.icourt.parse.analysis.Analysis;
import lombok.Data;

/**
 * 合规数据源解析对象模版
 */
@Data
public class NoticeSource implements Cloneable{
    //源id
    private Long id;
    //源url
    private String url;
    //包含html
    private String mainJson;
    //html
    private String html;
    //扩展字段（title、publishTime、province）
    private String extJson;
    //三方id
    private String thirdId;
    //对应的表名
    private String table;
    //解析对象
    private Analysis analysis;
    //解析mainJson后获取value值
    private String value;

    private RegulatoryNotice regulatoryNotice = new RegulatoryNotice();

    @Override
    public Object clone() throws CloneNotSupportedException {
        NoticeSource clone = (NoticeSource)super.clone();
        clone.regulatoryNotice = (RegulatoryNotice)regulatoryNotice.clone();
        return clone;
    }
}
