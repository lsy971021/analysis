package com.icourt.parse.analysis;

import com.icourt.parse.analysis.Impl.*;
import org.apache.commons.lang3.StringUtils;

public class AnalysisFactory {

    public static Analysis getAnalysis(String name) {
        if (name.contains("miit_gov_cn8"))
            return new MiitAnalysis();
        if (name.contains("cac_gov_cn8"))
            return new CacAnalysis();
        if (name.contains("pbc_gov_cn1"))
            return new PbcAnalysis();
        if (name.contains("csrc_gov_cn1"))
            return new CsrcAnalysis();
        if (name.contains("szse_cn8"))
            return new SzseAnalysis();
        if (name.contains("www_cbirc_gov_cn1"))
            return new CbircAnalysis();
        if (name.contains("query_sse_com_cn8"))
            return new SseAnalysis();
//        if (name.contains("gaj_beijing_gov_cn8"))
//            return new GajAnalysis();
//        if (name.contains("ggfw_mzj_beijing_gov_cn8"))
//            return new GgfwAnalysis();
        if (name.contains("www_zjzwfw_gov_cn111"))
            return new ZjzwfwAnalysis();
        if (name.contains("www_cnaac_org_cn8"))
            return new CnaacFilter();
        if (name.contains("cyberpolice_mps_gov_cn8"))
            return new MpsAnalysis();
        if (name.contains("gat_hebei_gov_cn8"))
            return new GatAnalysis();
        if (StringUtils.containsAny(name, "ga_tj_gov_cn8", "mp_weixin_qq_com8"))
            return new GaAnalysis();
        if (name.contains("gat_ln_gov_cn8"))
            return new GatLnAnalysis();
        if (name.contains("cfws_samr_gov_cn"))
            return new CfwsAnalysis();
        if (name.contains("gat_jl_gov_cn1"))
            return new GatJlAnalysis();
        if (name.contains("hljga_gov_cn1"))
            return new HljgaAnalysis();
        if (name.contains("gaj_sh_gov_cn1"))
            return new GajshAnalysis();
        if (name.contains("gat_jiangsu_gov_cn1"))
            return new GatJiangsuAnalysis();
        if (name.contains("gat_zj_gov_cn1"))
            return new GatZjAnalysis();
        if (name.contains("gat_ah_gov_cn1"))
            return new GatAhAnalysis();
        if (name.contains("gat_jiangxi_gov_cn1"))
            return new GatJiangxiAnalysis();
        if (name.contains("gat_shandong_gov_cn1"))
            return new GatShandongAnalysis();
        if (name.contains("miit_gov_cn1"))
            return new NewMiitAnalysis();
        if (name.contains("hnga_henan_gov_cn1"))
            return new HngaAnalysis();
        if (name.contains("gat_hubei_gov_cn1"))
            return new GatHubeiAnalysis();
        if (name.contains("gdga_gd_gov_cn1"))
            return new GdgaAnalysis();
        if (name.contains("gat_gxzf_gov_cn1"))
            return new GatGxzfAnalysis();
        if (name.contains("ga_hainan_gov_cn1"))
            return new GaHainanAnalysis();
        if (name.contains("gat_sc_gov_cn1"))
            return new GatScAnalysis();
        if (name.contains("gat_xizang_gov_cn1"))
            return new GatXizangAnalysis();
        if (name.contains("gat_shaanxi_gov_cn1"))
            return new GatShaanxiAnalysis();
        if (name.contains("gat_qinghai_gov_cn1"))
            return new GatQinghaiAnalysis();
        if (name.contains("gdcc315_cn1"))
            return new GdccAnalysis();
        if (name.contains("fs315_org1"))
            return new FsorgAnalysis();
        if (name.contains("sz315_org1"))
            return new SzorgAnalysis();
        if (name.contains("bj315_org1"))
            return new BjorgAnalysis();
        if (name.contains("jl315_org1"))
            return new JlorgAnalysis();
        if (name.contains("sxwq_org_cn1"))
            return new SxwqAnalysis();
        if (name.contains("hn315_net_cn1"))
            return new HnnetAnalysis();
        if (name.contains("qh315_org1"))
            return new QhorgAnalysis();
        if (name.contains("zhuhai315_com1"))
            return new ZhuhaiAnalysis();
        if (name.contains("scjgj_beijing_gov_cn1"))
            return new ScjgjBeijingAnalysis();
        if (name.contains("cca_org_cn1"))
            return new CcaorgAnalysis();
        if (name.contains("hub315_org1"))
            return new HuborgAnalysis();
        if (name.contains("gs12315_com1"))
            return new GscomAnalysis();
        if (name.contains("wjw_fujian_gov_cn1"))
            return new WjwFujianAnalysis();
        if (name.contains("amr_hainan_gov_cn1"))
            return new ArmHainanAnalysis();
        if (name.contains("gat_shandong_gov_cn2"))
            return new GatShandong2Analysis();
        if (name.contains("scjgj_xinjiang_gov_cn1"))
            return new ScjgjXinjiangAnalysis();
        if (name.contains("www_cnaac_org_cn1"))
            return new CnaacAnalysis2();
        return null;
    }
}
