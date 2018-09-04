package com.webcommander

import com.webcommander.util.AppUtil

import java.text.MessageFormat

class LocalizationTagLib {

    ApplicationTagLib applicationTagLib

    LocalizationTagLib() {
        applicationTagLib = AppUtil.getBean(ApplicationTagLib)
    }

    static namespace = "g"

    def message = { attrs ->
        if (!attrs.code) {
            out << ""
            return
        }
        if(attrs.code.startsWith("f:")) {
            out << attrs.code.substring(2)
            return
        }
        String value = applicationTagLib.message(code: attrs.code)
        if (!attrs.args) {
            attrs.args = []
        }
        if (attrs.rawargs) {
            if (attrs.rawargs instanceof List) {
                attrs.args.addAll(attrs.rawargs.collect {
                    it instanceof String && !it.startsWith("f:") ? applicationTagLib.message(code: it) : "" + it
                })
            } else {
                attrs.rawargs.each {
                    def key = it.key
                    if (key instanceof String) {
                        key = key.toInteger()
                    }
                    attrs.args.safeInsert(key, it.value instanceof String ? applicationTagLib.message(code: it.value) : ("" + it.value))
                }
            }
        }
        try {
            out << MessageFormat.format(value, *(attrs.args ?: []))
        } catch (Exception ex) {
            out << value
        }
    }
}