package com.icourt.util;

import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.icourt.core.Result;
import com.icourt.datacenter.utils.crawl.AgentUtil;
import com.icourt.datacenter.utils.formal.JsoupUtil;
import com.icourt.entity.GetProxyVo;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


/**
 * 公众号html正文解析工具类
 */
public class GzhAnalysisUtils {

    /**
     * 根据html获取正文
     *
     * @param html
     * @param isUrl 是否为url
     * @return
     */
    public static String analysis(String html, boolean isUrl) {
        if (isUrl)
            try {
                html = getHtmlByUrl(html);
            } catch (MalformedURLException e) {
                throw new RuntimeException("根据url解析失败:请求返回为空");
            }
        return doAnalysis(getGzhContentElement(getDocument(html)));
    }

    /**
     * 根据html 获取对br标签处理后的document
     *
     * @param html
     * @return
     */
    public static Document getDocument(String html) {
        html = html.replaceAll("<[\\s]*?br[^>]*>", "《br》");
        Document document = Jsoup.parse(html);
        return document;
    }

    /**
     * 根据document  获取公正好正文部分的Element
     *
     * @param document
     * @return
     */
    public static Element getGzhContentElement(Document document) {
        if(document==null)
            return null;
        //公众号的正文在此id下面
        return document.getElementById("js_content");
    }

    /**
     * @param pageUrl url
     */
    public static String getHtmlByUrl(String pageUrl) throws MalformedURLException {
        if (StringUtils.isEmpty(pageUrl)) {
            return "";
        }
        GetProxyVo getProxyVo = new GetProxyVo();
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Accept", "*/*");
        headerMap.put("Cache-Control", "no-cache");
        headerMap.put("Referer", new URL(pageUrl).getHost());
        headerMap.put("User-Agent", AgentUtil.get_randomUserAgent());

        getProxyVo.setUrl(pageUrl);
        getProxyVo.setHearderMap(headerMap);
        String reqStr = OkHttpUtil.execute(
                OkHttpUtil.OkHttp.builder()
                        .url(pageUrl)
                        .method("GET")
                        .headerMap(headerMap)
                        .build());

        //直接请求失败时，使用代理服务请求
        if (StringUtils.isEmpty(reqStr)) {
            Result<Object> proxyResult = null;
            try {
                String proxyResp = HttpRequest.post(pageUrl)
                        .body(JSON.toJSONString(getProxyVo))
                        .execute()
                        .body();

                proxyResult = JSON.parseObject(proxyResp, new TypeReference<Result<Object>>() {
                });
            } catch (Exception e) {
            }
            if (proxyResult != null && proxyResult.getIsSuccess()) {
                reqStr = (String) proxyResult.getData();
            }
        }
        if (StringUtils.isEmpty(reqStr)) {
            throw new RuntimeException("根据url解析失败:请求返回为空");
        }
        return reqStr;
    }


    /**
     * 根据公众号contentElement 获取正文部分文本
     *
     * @param contentElement
     */
    public static String doAnalysis(Element contentElement) {
        if(contentElement==null)
            return null;
        //添加img标签
        Elements aFileEleList = contentElement.select("img");
        if(!aFileEleList.isEmpty()) {
            aFileEleList.removeAttr("class");
            aFileEleList.addClass("cDownFile");
        }

        Elements mainAttachList = contentElement.getElementsByClass("cDownFile");
        // 修改图片标签格式
        for (Element element : mainAttachList) {
            String attachUrl = element.attr("data-src");
            element.attr("src", attachUrl);
        }

        Whitelist whitelist = JsoupUtil.iCourtWhiteList();
        String content = JsoupUtil.htmltoText(contentElement.outerHtml(), whitelist);
        content = content.replace("《br》", "\n");
        return JsoupUtil.strFormat(content);
    }
}
