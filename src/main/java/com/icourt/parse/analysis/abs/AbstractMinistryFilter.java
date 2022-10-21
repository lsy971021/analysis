package com.icourt.parse.analysis.abs;

import com.icourt.parse.analysis.Analysis;

/**
 * 工信部过滤
 */
public abstract class AbstractMinistryFilter implements Analysis {

    @Override
    public String checkContentToPick() {
        return ".*(个人信息|网络安全|隐私|携号转网|数据安全|敏感权限|App|应用程序|未经同意查询).*";
    }

    @Override
    public String checkContentToNotPick() {
        return ".*(遴选|比赛|竞赛|党建|党史|扶贫|座谈会|招聘|面试|继续教育|人民币管理|反洗钱|支票|支付机构稳健).*";
    }

    @Override
    public String checkTitleToPick() {
        return ".*(通报|处罚|App|应用软件|检测|约谈|个人信息|治理|网络安全|数据安全|整治|处罚|查处|通告|违法|隐私).*";
    }


    @Override
    public String mainBody() {
        return "工业和信息化部";
    }
}
