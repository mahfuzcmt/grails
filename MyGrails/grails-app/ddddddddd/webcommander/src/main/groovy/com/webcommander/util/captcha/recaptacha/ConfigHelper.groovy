package com.webcommander.util.captcha.recaptacha

class ConfigHelper {
    public static booleanValue(def value, boolean defaultValue) {
        if (!value) {
            return defaultValue
        }
        if (value.class == java.lang.Boolean) {
            return value
        } else {
            return value.toBoolean()
        }
    }
}
