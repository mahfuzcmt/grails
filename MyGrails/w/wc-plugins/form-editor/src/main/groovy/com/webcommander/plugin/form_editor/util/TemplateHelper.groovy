package com.webcommander.plugin.form_editor.util

class TemplateHelper {

    public static String name(String value) {
        return getAttribute('name', value)
    }
    public static String validation(String value) {
        return getAttribute('validation', value)
    }
    public static String placeholder(String value) {
        return getAttribute('placeholder', value)
    }
    public static String value(String value) {
        return getAttribute('value', value)
    }
    public static String title(String value) {
        return getAttribute('title', value)
    }
    public static String clazz(String value) {
        return getAttribute('class', value)
    }
    public static String text(String value) {
        value = value.encodeAsBMHTML()
        if(value?.size()) {
            return "$value"
        }
        return ""
    }
    private static String getAttribute(String attr, String value) {
        value = value.encodeAsBMHTML()
        if(value?.size()) {
            return "$attr='$value'"
        }
        return ''
    }

}
