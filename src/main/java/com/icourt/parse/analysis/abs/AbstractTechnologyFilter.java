package com.icourt.parse.analysis.abs;

import com.icourt.parse.analysis.Analysis;

/**
 * 科技部过滤
 */
public abstract class AbstractTechnologyFilter implements Analysis {

    @Override
    public String checkContentToPick() {
        return ".*(个人信息|网络安全|隐私|携号转网|数据安全|敏感权限|App|应用程序|互联网安全|信息安全|遗传资源信息|遗传信息|医疗数据|病理数据|检测数据|基因数据).*";
    }

    @Override
    public String checkContentToNotPick() {
        return ".*(遴选|比赛|竞赛|党建|党史|扶贫|座谈会|招聘|面试|继续教育).*";
    }

    @Override
    public String checkTitleToPick() {
        return ".*(执法|信息|通报|App|应用软件|检测|约谈|个人信息|治理|网络安全|数据安全|整治|处罚|查处|通告|违法|隐私|个人数据|敏感数据).*";
    }
}
