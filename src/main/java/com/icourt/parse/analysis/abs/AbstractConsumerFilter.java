package com.icourt.parse.analysis.abs;

import com.icourt.parse.analysis.Analysis;

/**
 * 消保委过滤
 */
public abstract class AbstractConsumerFilter implements Analysis {

    @Override
    public String checkContentToPick() {
        return ".*(个人信息|网络安全|数据安全|隐私|携号转网|敏感权限|App|应用程序|互联网安全|消费者信息|账号注销|自动化推荐|信息泄露|收集使用|身份信息|信息安全|人脸|信息保护|信息安全|App|应用程序|非法推送商业信息|数据杀熟|算法歧视|自动化决策|平台服务|实名|净网|刷脸|互联网信息服务|个人数据|安全保护|信息系统).*";
    }

    @Override
    public String checkContentToNotPick() {
        return ".*(遴选|比赛|竞赛|党建|党史|扶贫|座谈会|招聘|面试|继续教育|普法|满意度|食品安全|化妆品|考察|视察).*";
    }

    @Override
    public String checkTitleToPick() {
        return null;
    }

    @Override
    public String checkTitleToNotPick() {
        return ".*(停车|培训|退货|换货|纸尿裤|最低消费|产品质量|怎么选|浪费|食品安全|食品生产|惩罚性赔偿|违法生产|违法销售|长假|保健品|购物|免税|测试|药品|达标|虚标|黑中介|炒鞋|搭售|电子烟|整理|退一赔三|进口|假货|套现|奢侈品|捆绑|天价|差评|好评|评价|性价比).*";
    }

    @Override
    public String mainBody() {
        return "中国消费者协会";
    }
}
