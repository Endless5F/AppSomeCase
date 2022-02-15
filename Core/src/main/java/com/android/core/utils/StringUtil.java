package com.android.core.utils;

import android.text.TextUtils;

import java.util.Locale;

/**
 * 内部类单例模式保证线程安全且通过类的装载实现lazy-loading
 * 扩展方法请先增加IStringUtils
 */
public final class StringUtil {

    /**
     * 私有构造方法
     */
    private StringUtil() {

    }

    // ---------------------------------------------------------------------
    //  判断类
    // ---------------------------------------------------------------------
    /**
     * 检查输入 {@code CharSequence} 是否含有空白字符
     *
     * @param str 输入{@code CharSequence}(可能为空 {@code null})
     *
     * @return {@code true} 如果输入 {@code CharSequence} 非空且含有至少一个空白字符
     *
     * @see Character#isWhitespace
     */
    public static boolean containsWhitespace(CharSequence str) {
        if (TextUtils.isEmpty(str)) {
            return false;
        }
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测输入字符串 {@code String} 是否已特定前缀（不区分大小写）开头
     *
     * @param str    输入字符串 {@code String}
     * @param prefix 前缀
     * @param ignoreCase 是否忽略大小写
     *
     * @see String#startsWith
     */
    public static boolean startsWith(String str, String prefix, boolean ignoreCase) {
        if (str == null || prefix == null) {
            return false;
        }

        if (str.length() < prefix.length()) {
            return false;
        }

        if (str.startsWith(prefix)) {
            return true;
        }

        if (ignoreCase) {
            String lcStr = str.substring(0, prefix.length()).toLowerCase(Locale.getDefault());
            String lcPrefix = prefix.toLowerCase(Locale.getDefault());
            return lcStr.equals(lcPrefix);
        }
        return false;
    }

    /**
     * 检测输入字符串 {@code String} 是否已特定后缀（不区分大小写）开头
     *
     * @param str    输入字符串 {@code String}
     * @param suffix 后缀
     * @param ignoreCase 是否忽略大小写
     *
     * @see String#startsWith
     */
    public static boolean endsWith(String str, String suffix, boolean ignoreCase) {
        if (str == null || suffix == null) {
            return false;
        }
        if (str.endsWith(suffix)) {
            return true;
        }
        if (str.length() < suffix.length()) {
            return false;
        }

        if (ignoreCase) {
            String lcStr = str.substring(str.length() - suffix.length()).toLowerCase(Locale.getDefault());
            String lcSuffix = suffix.toLowerCase(Locale.getDefault());
            return lcStr.equals(lcSuffix);
        }
        return false;
    }

    // ---------------------------------------------------------------------
    //  字符串和子串的操作
    // ---------------------------------------------------------------------
    /**
     * 输入字符串{@code str}中匹配substring {@code sub}的次数.
     *
     * @param str 输入字符串{@code str}.
     * @param sub 匹配substring.
     */
    public static int getRepetitions(String str, String sub) {
        if (str == null || sub == null || str.length() == 0 || sub.length() == 0) {
            return 0;
        }
        int count = 0;
        int pos = 0;
        int idx;
        while ((idx = str.indexOf(sub, pos)) != -1) {
            ++count;
            pos = idx + sub.length();
        }
        return count;
    }

    /**
     * 计算文本的总字数
     *
     * 默认所有字符算2个字
     *
     * @param text text
     * @param discriminateLetters 针对字母进行特殊处理，算1个字
     *
     * @return text length
     */
    public static int length(String text, boolean discriminateLetters) {
        int length = 0;
        if (TextUtils.isEmpty(text)) {
            return length;
        }
        char[] chars = text.toCharArray();
        for (char ch : chars) {
            float step = 2;
            if (ch > 0 && ch < 127) { // SUPPRESS CHECKSTYLE
                if (discriminateLetters) {
                    step = 1;
                }
            }
            length += step;
        }
        return length;
    }

    /*********************************************************************************
     *     The following two methods copy from apache's commons-lang3's StringUtils; *
     *     Added by John Kenrinus Lee(lizhehao@baidu.com) at 2018-07-06, based on:   *
     *     1. {@link TextUtils#isEmpty(CharSequence)}  don't check blank like " ";   *
     *     2. {@link String#trim()} is slow;                                         *
     *********************************************************************************/

    /**
     * <p>Checks if a CharSequence is empty (""), null or whitespace only.</p>
     *
     * <p>Whitespace is defined by {@link Character#isWhitespace(char)}.</p>
     *
     * <pre>
     * StringUtils.isBlank(null)      = true
     * StringUtils.isBlank("")        = true
     * StringUtils.isBlank(" ")       = true
     * StringUtils.isBlank("bob")     = false
     * StringUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param cs the CharSequence to check, may be null
     *
     * @return {@code true} if the CharSequence is null, empty or whitespace only
     *
     * @since 2.0
     * @since 3.0 Changed signature from isBlank(String) to isBlank(CharSequence)
     */
    public static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 把二进制byte数组生成十六进制字符串，单个字节小于0xf，高位补0。
     *
     * @param bytes     输入
     * @param separator 分割线
     * @param upperCase true：大写， false 小写字符串
     *
     * @return 把二进制byte数组生成十六进制字符串，单个字节小于0xf，高位补0。
     */
    public static String toHexString(byte[] bytes, String separator, boolean upperCase) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String str = Integer.toHexString(0xFF & b); // SUPPRESS CHECKSTYLE
            if (upperCase) {
                str = str.toUpperCase(Locale.getDefault());
            }
            if (str.length() == 1) {
                hexString.append("0");
            }
            hexString.append(str).append(separator);
        }
        if (hexString.length() > 1) {
            return hexString.substring(0, hexString.length() - 1);
        } else {
            return hexString.toString();
        }
    }
}
