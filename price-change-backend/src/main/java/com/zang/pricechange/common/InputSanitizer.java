package com.zang.pricechange.common;

import org.owasp.encoder.Encode;

/**
 * 输入清理工具类
 * 用于防止 XSS（跨站脚本攻击）和其他注入攻击
 * 
 * 安全特性：
 * - 对用户输入进行 HTML 转义
 * - 提供多种上下文相关的编码方法
 * - 统一处理所有需要清理的输入数据
 */
public class InputSanitizer {

    /**
     * 对字符串进行 HTML 转义
     * 适用于将用户输入插入到 HTML 正文中的场景
     * 
     * @param input 原始输入
     * @return 转义后的字符串
     */
    public static String sanitizeForHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Encode.forHtml(input);
    }

    /**
     * 对字符串进行 HTML 属性转义
     * 适用于将用户输入插入到 HTML 属性值中的场景
     * 
     * @param input 原始输入
     * @return 转义后的字符串
     */
    public static String sanitizeForHtmlAttribute(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Encode.forHtmlAttribute(input);
    }

    /**
     * 对字符串进行 JavaScript 转义
     * 适用于将用户输入插入到 JavaScript 代码中的场景
     * 
     * @param input 原始输入
     * @return 转义后的字符串
     */
    public static String sanitizeForJavaScript(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Encode.forJavaScript(input);
    }

    /**
     * 对字符串进行 URL 参数转义
     * 适用于将用户输入作为 URL 参数的场景
     * 
     * @param input 原始输入
     * @return 转义后的字符串
     */
    public static String sanitizeForUrl(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Encode.forUriComponent(input);
    }

    /**
     * 对字符串进行 CSS 转义
     * 适用于将用户输入插入到 CSS 样式中的场景
     * 
     * @param input 原始输入
     * @return 转义后的字符串
     */
    public static String sanitizeForCss(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Encode.forCssString(input);
    }

    /**
     * 对字符串进行 XML 转义
     * 适用于将用户输入插入到 XML 文档中的场景
     * 
     * @param input 原始输入
     * @return 转义后的字符串
     */
    public static String sanitizeForXml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return Encode.forXml(input);
    }

    /**
     * 批量清理字符串数组
     * 
     * @param inputs 原始输入数组
     * @return 转义后的字符串数组
     */
    public static String[] sanitizeArray(String[] inputs) {
        if (inputs == null) {
            return null;
        }
        String[] result = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            result[i] = sanitizeForHtml(inputs[i]);
        }
        return result;
    }
}
