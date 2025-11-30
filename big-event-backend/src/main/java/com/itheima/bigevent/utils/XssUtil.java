package com.itheima.bigevent.utils;

import java.util.regex.Pattern;

/**
 * XSS 防护工具类
 */
public class XssUtil {

    // 常见的 XSS 攻击模式
    private static final Pattern[] XSS_PATTERNS = {
        // Script 标签
        Pattern.compile("<script(.*?)>(.*?)</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\\'(.*?)\\'", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("<script(.*?)>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        Pattern.compile("</script>", Pattern.CASE_INSENSITIVE),
        // eval 表达式
        Pattern.compile("eval\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // expression 表达式
        Pattern.compile("expression\\((.*?)\\)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // javascript:
        Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE),
        // vbscript:
        Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE),
        // onload 事件
        Pattern.compile("onload(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // onerror 事件
        Pattern.compile("onerror(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL),
        // onclick 事件
        Pattern.compile("onclick(.*?)=", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
    };

    /**
     * 清理 XSS 攻击字符串
     * @param value 原始字符串
     * @return 清理后的字符串
     */
    public static String clean(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        String result = value;
        for (Pattern pattern : XSS_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }
        
        return result;
    }

    /**
     * HTML 实体编码
     * @param value 原始字符串
     * @return 编码后的字符串
     */
    public static String escapeHtml(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        
        StringBuilder escaped = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '&':
                    escaped.append("&amp;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&#x27;");
                    break;
                case '/':
                    escaped.append("&#x2F;");
                    break;
                default:
                    escaped.append(c);
            }
        }
        return escaped.toString();
    }

    /**
     * 检查字符串是否包含 XSS 攻击
     * @param value 要检查的字符串
     * @return true 如果包含 XSS 攻击
     */
    public static boolean containsXss(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        
        for (Pattern pattern : XSS_PATTERNS) {
            if (pattern.matcher(value).find()) {
                return true;
            }
        }
        return false;
    }
}

