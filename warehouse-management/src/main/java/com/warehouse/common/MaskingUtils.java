package com.warehouse.common;

public final class MaskingUtils {

    private MaskingUtils() {
    }

    public static String maskPhone(String value) {
        if (value == null || value.length() < 7) {
            return value;
        }
        return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
    }

    public static String maskEmail(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        int atIndex = value.indexOf('@');
        if (atIndex <= 1) {
            return "***";
        }
        return value.charAt(0) + "***" + value.substring(atIndex);
    }

    public static String maskAddress(String value) {
        if (value == null || value.length() <= 8) {
            return value;
        }
        return value.substring(0, 6) + "***";
    }
}
