package com.icourt.parse.analysis.abs;

import com.icourt.parse.analysis.Analysis;

/**
 * 网信办，交通，金融等主管部门过滤 （例如，国务院交通运输部，银保监会等）
 */
public abstract class AbstractPbcFilter implements Analysis {

    @Override
    public String checkContentToPick() {
        return ".*(个人信息|网络安全|隐私|携号转网|数据安全|敏感权限|未经同意查询).*";
    }

    @Override
    public String checkContentToNotPick() {
        return ".*(培训|遴选|比赛|竞赛|党建|党史|扶贫|座谈会|招聘|面试|继续教育|人民币管理|反洗钱|支票|支付机构稳健).*";
    }

    @Override
    public String checkTitleToPick() {
        return ".*(通报|处罚|App|应用软件|检测|约谈|个人信息|治理|网络安全|数据安全|整治|处罚|查处|通告|违法|隐私).*";
    }
}
