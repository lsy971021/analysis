package com.icourt.util;

import cn.hutool.core.codec.Base64Decoder;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * pdf工具类
 */
public class PdfUtil {

    static Logger log = LoggerFactory.getLogger(PdfUtil.class);

    /**
     * 使用pdfbox读取PDF全部文字(格式已处理)
     *
     * @param pdfString PDF解码后的base64字符串
     */
    public static String readByString(String pdfString) {
        return readByString(pdfString, null);
    }

    /**
     * 使用pdfbox读取PDF全部文字(格式已处理)
     *
     * @param pdfString PDF解码后的base64字符串
     * @param function  PDF文本格式解析特殊处理
     */
    public static String readByString(String pdfString, Function<String, String> function) {
        InputStream inputStream = toInputStream(pdfString);
        return formatPdfText(readByStream(inputStream), function);
    }

    /**
     * 使用pdfbox读取PDF全部文字（无格式）
     *
     * @param inputStream PDF IO流
     */
    public static String readByStream(InputStream inputStream) {
        //创建文档对象
        PDDocument doc = null;
        String text = "";
        try {
            doc = PDDocument.load(inputStream);
            //获取一个PDFTextStripper文本剥离对象
            PDFTextStripper textStripper = new PDFTextStripper();
            textStripper.getPageStart();
            text = textStripper.getText(doc);
            return text;
        } catch (IOException e) {
            log.error("pdf读取失败");
            return "";
        } finally {
            try {
                Objects.requireNonNull(doc).close();
            } catch (IOException exception) {
                log.error("pdf关闭失败");
            }
        }
    }

    /**
     * 通用PDF文本格式化
     * 去除多余换行
     *
     * @param text PDF读取后未处理的文本
     * @return
     */
    public static String formatPdfText(String text, Function<String, String> function) {
        List<String> splitList = new ArrayList<>(Arrays.asList(text.split("\n")));
        IntSummaryStatistics statistics = splitList.stream().mapToInt(item -> StrUtil.bytes(item, "GBK").length).summaryStatistics();
        int max = statistics.getMax();
        List<String> collect = splitList.stream().map(item -> {
            String str = item.replaceAll(" ", "");
            if (Objects.nonNull(function)) {
                str = function.apply(str);
            }
            if (StrUtil.bytes(str, "GBK").length < max * 0.75 || StringUtils.endsWith(str, "。")) {
                str += "\n";
            }
            return str;
        }).collect(Collectors.toList());
        return String.join("", collect);
    }


    /**
     * 将PDF解码后的base64字符串转换为 InputStream
     *
     * @param ioString PDF解码后的base64字符串
     */
    public static InputStream toInputStream(String ioString) {
        byte[] bytes = Base64Decoder.decode(ioString);
        return new ByteArrayInputStream(bytes);
    }

}
