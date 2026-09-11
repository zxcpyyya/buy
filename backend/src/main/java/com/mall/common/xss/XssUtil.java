package com.mall.common.xss;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * XSS防护工具类
 * 
 * <p>防护措施：
 * <ul>
 *   <li>HTML标签过滤</li>
 *   <li>JavaScript伪协议过滤</li>
 *   <li>事件处理器过滤</li>
 *   <li>CSS表达式过滤</li>
 *   <li>危险函数过滤</li>
 * </ul>
 *
 * @author xiu
 */
@Slf4j
public final class XssUtil {

    private XssUtil() {
    }

    /**
     * 危险模式列表
     */
    private static final Pattern[] DANGEROUS_PATTERNS = {
        // script标签
        Pattern.compile("<script[^>]*?>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // script标签（自闭合）
        Pattern.compile("<script[^>]*?/>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // javascript:伪协议
        Pattern.compile("javascript\\s*:", Pattern.CASE_INSENSITIVE),
        // vbscript:伪协议
        Pattern.compile("vbscript\\s*:", Pattern.CASE_INSENSITIVE),
        // data:伪协议
        Pattern.compile("data\\s*:", Pattern.CASE_INSENSITIVE),
        // onerror事件
        Pattern.compile("on\\w+\\s*=", Pattern.CASE_INSENSITIVE),
        // 表达式注入（CSS）
        Pattern.compile("expression\\s*\\(", Pattern.CASE_INSENSITIVE),
        // url()函数
        Pattern.compile("url\\s*\\([^)]*\\)", Pattern.CASE_INSENSITIVE),
        // import指令
        Pattern.compile("@import", Pattern.CASE_INSENSITIVE),
        // iframe标签
        Pattern.compile("<iframe[^>]*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // embed标签
        Pattern.compile("<embed[^>]*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // object标签
        Pattern.compile("<object[^>]*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // svg标签（可能包含脚本）
        Pattern.compile("<svg[^>]*?on\\w+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // img标签onerror
        Pattern.compile("<img[^>]*?onerror", Pattern.CASE_INSENSITIVE),
        // style标签
        Pattern.compile("<style[^>]*?>.*?</style>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // link标签（可能加载恶意资源）
        Pattern.compile("<link[^>]*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
        // 危险注释
        Pattern.compile("<!--.*?-->", Pattern.DOTALL),
        // XML数据岛
        Pattern.compile("<xml[^>]*?>.*?</xml>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
    };

    /**
     * 需要过滤的危险字符
     */
    private static final Pattern DANGEROUS_CHARS = Pattern.compile("[<>\"'&]");

    /**
     * 过滤XSS攻击字符串
     *
     * @param input 原始输入
     * @return 过滤后的安全字符串
     */
    public static String filter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String result = input;

        // 1. 先移除危险模式
        for (Pattern pattern : DANGEROUS_PATTERNS) {
            result = pattern.matcher(result).replaceAll("");
        }

        // 2. 转义危险字符
        result = escapeHtml(result);

        return result;
    }

    /**
     * 过滤并记录（用于日志记录）
     *
     * @param input      原始输入
     * @param fieldName  字段名（用于日志）
     * @return 过滤后的安全字符串
     */
    public static String filterAndLog(String input, String fieldName) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String filtered = filter(input);

        if (!filtered.equals(input)) {
            log.warn("检测到XSS攻击: field={}, before={}, after={}", 
                fieldName, maskInput(input), maskInput(filtered));
        }

        return filtered;
    }

    /**
     * HTML实体转义
     */
    private static String escapeHtml(String input) {
        if (input == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(input.length() + 64);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                case '"':
                    sb.append("&quot;");
                    break;
                case '\'':
                    sb.append("&#x27;");
                    break;
                case '/':
                    sb.append("&#x2F;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * HTML实体反转义
     */
    public static String unescapeHtml(String input) {
        if (input == null) {
            return null;
        }

        return input
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&amp;", "&")
                .replace("&quot;", "\"")
                .replace("&#x27;", "'")
                .replace("&#x2F;", "/");
    }

    /**
     * 检查是否包含XSS攻击
     *
     * @param input 输入
     * @return true-包含危险内容, false-安全
     */
    public static boolean containsXss(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(input).find()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 验证输入是否安全（严格模式）
     *
     * @param input 输入
     * @return true-安全, false-包含危险内容
     */
    public static boolean isSafe(String input) {
        if (input == null || input.isEmpty()) {
            return true;
        }

        // 检查危险模式
        if (containsXss(input)) {
            return false;
        }

        // 检查是否只包含合法字符
        return Pattern.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9\\s.,!?;:()，。！？；：（）@#$%^*+-=_/\\\\|\\[\\]{}]+$", input);
    }

    /**
     * 脱敏输入（用于日志记录）
     */
    private static String maskInput(String input) {
        if (input == null) {
            return null;
        }

        // 截断过长输入，保留首尾
        if (input.length() > 50) {
            return input.substring(0, 20) + "..." + input.substring(input.length() - 20);
        }

        return input;
    }

    /**
     * 净化SQL输入（不转义而是移除危险字符）
     * 注意：推荐使用参数化查询，这里只是辅助防护
     */
    public static String sanitizeSql(String input) {
        if (input == null) {
            return null;
        }

        // 移除可能导致SQL注入的字符序列
        return input
                .replaceAll("(?i);\\s*", "")
                .replaceAll("(?i)union\\s+", "")
                .replaceAll("(?i)select\\s+", "")
                .replaceAll("(?i)insert\\s+", "")
                .replaceAll("(?i)delete\\s+", "")
                .replaceAll("(?i)drop\\s+", "")
                .replaceAll("(?i)update\\s+", "")
                .replaceAll("(?i)--", "")
                .replaceAll("(?i)/\\*.*?\\*/", "")
                .replaceAll("(?i)exec\\s*\\(", "")
                .replaceAll("(?i)execute\\s*\\(", "");
    }
}
