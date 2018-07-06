package com.constellio.app.modules.restapi.core.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class StringUtils {

    public static String concat(String... values) {
        String result = "";

        for (String value : values) {
            if (value != null && !value.isEmpty()) {
                result = result.concat(value);
            }
        }
        return result;
    }

    public static boolean isUnsignedInteger(String value) {
        try {
            int number = Integer.valueOf(value);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isUnsignedDouble(String value) {
        try {
            double number = Double.valueOf(value);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isUnsignedLong(String value) {
        try {
            long number = Long.valueOf(value);
            return number > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
