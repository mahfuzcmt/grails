package com.webcommander

class UtilTagLib {
    static namespace = "util"

    def paramDump = { attrs, body ->
        def dump = { key, value ->
            out << "<input type='hidden' name='"
            out << key.encodeAsBMHTML()
            out << "' value='"
            out << value.encodeAsBMHTML()
            out << "'>"
        }
        params.each { param ->
            if(!(param.value instanceof Map)) {
                if(param.value instanceof Collection || param.value instanceof Object[]) {
                    param.value.each { value ->
                        dump param.key, value
                    }
                } else {
                    dump param.key, param.value
                }
            }
        }
    }

    def mailTemplateMacroRenderer = { attrs, body ->
        def renderer;
        renderer = {Map macros->
            macros.each {
                out << "<div>"
                out << "<b>%${it.key}%</b>"
                if (it.value instanceof Map) {
                    Map map = it.value
                    String label = "repeatable";
                    if (map.containsKey("label")) {
                        label = map.label;
                    }
                    out <<  "<label> - ${g.message(code: label)}</label>"
                    out << "<blockquote>"
                    renderer(map.fields)
                    out << "</blockquote>"
                } else {
                    out <<  "<label> - ${g.message(code: it.value)}</label>"
                }
                out<< "</div>"
            }
        }
        renderer(attrs.macros);
    }
}
