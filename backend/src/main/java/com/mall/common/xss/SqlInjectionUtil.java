package com.mall.common.xss;

import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * SQL注入防护工具类
 * 
 * <p>检测并防御常见的SQL注入攻击
 *
 * @author xiu
 */
@Slf4j
public final class SqlInjectionUtil {

    private SqlInjectionUtil() {
    }

    /**
     * SQL注入危险模式
     */
    private static final Pattern[] SQL_INJECTION_PATTERNS = {
        // 注释
        Pattern.compile("--"),
        Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL),
        Pattern.compile(";"),
        
        // UNION注入
        Pattern.compile("(?i)union\\s+(all\\s+)?select"),
        Pattern.compile("(?i)union\\s+select"),
        
        // 常见SQL关键字
        Pattern.compile("(?i)\\bexec\\b"),
        Pattern.compile("(?i)\\bexecute\\b"),
        Pattern.compile("(?i)\\bsp_executesql\\b"),
        Pattern.compile("(?i)\\bxp_cmdshell\\b"),
        Pattern.compile("(?i)\\bsp_executesql\\b"),
        
        // 条件注入
        Pattern.compile("(?i)\\bor\\b.*?=", Pattern.DOTALL),
        Pattern.compile("(?i)\\band\\b.*?=", Pattern.DOTALL),
        
        // DROP/DELETE/TRUNCATE
        Pattern.compile("(?i)\\bdrop\\s+(table|database|index)\\b"),
        Pattern.compile("(?i)\\bdelete\\s+from\\b"),
        Pattern.compile("(?i)\\btruncate\\s+table\\b"),
        
        // INSERT/UPDATE
        Pattern.compile("(?i)\\binsert\\s+into\\b"),
        Pattern.compile("(?i)\\bupdate\\s+\\w+\\s+set\\b"),
        
        // INTO OUTFILE
        Pattern.compile("(?i)\\binto\\s+outfile\\b"),
        
        // LOAD_FILE/LOAD_DATA
        Pattern.compile("(?i)\\bload_file\\b"),
        Pattern.compile("(?i)\\bload\\s+data\\b"),
        
        // 十六进制编码
        Pattern.compile("0x[0-9a-f]+", Pattern.CASE_INSENSITIVE),
        
        // CHAR函数绕过
        Pattern.compile("(?i)char\\s*\\(\\s*\\d+\\s*\\)"),
    };

    /**
     * 危险字符
     */
    private static final Pattern DANGEROUS_CHARS = Pattern.compile("['\";\\-\\-]");

    /**
     * 检查是否包含SQL注入特征
     *
     * @param input 输入
     * @return true-可能包含SQL注入, false-安全
     */
    public static boolean containsSqlInjection(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }

        for (Pattern pattern : SQL_INJECTION_PATTERNS) {
            if (pattern.matcher(input).find()) {
                log.warn("检测到SQL注入特征: pattern={}, input={}", pattern.pattern(), maskInput(input));
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

        return !containsSqlInjection(input);
    }

    /**
     * 脱敏输入（用于日志）
     */
    private static String maskInput(String input) {
        if (input == null) {
            return null;
        }
        if (input.length() > 50) {
            return input.substring(0, 20) + "..." + input.substring(input.length() - 20);
        }
        return input;
    }

    /**
     * 安全转义（用于日志记录，不用于SQL参数）
     * 注意：推荐使用参数化查询
     */
    public static String escape(String input) {
        if (input == null) {
            return null;
        }
        return input
                .replace("'", "''")
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
