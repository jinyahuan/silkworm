/*
 * Copyright 2018-2020 The Silkworm Authors
 */

package cn.jinyahuan.tool.silkworm.plugin;

import java.util.Objects;

/**
 * @author Yahuan Jin
 * @since 2.1
 */
final class StringUtils {
    private StringUtils() {}

    public static boolean isEmpty(final String str) {
        return Objects.isNull(str) || str.isEmpty();
    }

    public static boolean isNotEmpty(final String str) {
        return !isEmpty(str);
    }

    public static boolean isBlank(final String str) {
        return Objects.isNull(str) || isEmpty(str.trim());
    }

    public static boolean isNotBlank(final String str) {
        return !isBlank(str);
    }
}
