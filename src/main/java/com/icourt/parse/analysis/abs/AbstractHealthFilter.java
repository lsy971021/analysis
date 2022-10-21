package com.icourt.parse.analysis.abs;

import com.icourt.parse.analysis.Analysis;

/**
 * 卫健委过滤
 */
public abstract class AbstractHealthFilter implements Analysis {

    @Override
    public String checkContentToPick() {
        return ".*(个人信息|网络安全|数据安全|个人数据|敏感数据|健康信息|电子病历|健康码|个人病历|档案信息|隐私|遗传信息|生命登记信息|患者信息|基因数据|敏感数据|泄漏|信息系统|医疗支付记录|医保记录|健康记录|医疗应用数据|个人属性数据|健康状况数据|医疗应用数据|医疗支付数据|卫生资源数据|公共卫生数据|医疗数据|生理数据|生理信息|健康医疗数据|医疗健康数据|健康数据|健康医疗信息系统|治疗笔记|受限制数据集|临床信息系统|现病史|既往病史|检验检查数据|遗传咨询数据|医保支付信息|保险信息|保险状态|保险金额|卫生资源数据|医院基本数据|医院运营数据|公共卫生数据|环境卫生数据|传染病疫情数据|疾病监测数据|疾病预防数据|出生死亡数据).*";
    }

    @Override
    public String checkContentToNotPick() {
        return ".*(遴选|比赛|竞赛|党建|党史|扶贫|座谈会|招聘|面试|继续教育|党日活动).*";
    }

    @Override
    public String checkTitleToPick() {
        return ".*(个人信息|网络安全|数据安全|个人数据|敏感数据|健康信息|信息化标准|医疗大数据|医疗信息化).*";
    }

    @Override
    public String mainBody() {
        return "国家卫生健康委员会";
    }
}
