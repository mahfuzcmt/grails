package com.webcommander

import org.grails.gsp.GroovyPagesTemplateEngine
import org.grails.taglib.TagLibraryLookup
import org.grails.web.gsp.io.GrailsConventionGroovyPageLocator
import org.grails.buffer.StreamCharBuffer

class RenderService {

    static transactional = false

    GroovyPagesTemplateEngine groovyPagesTemplateEngine
    GrailsConventionGroovyPageLocator groovyPageLocator
    TagLibraryLookup gspTagLibraryLookup

    class EOF extends Exception {
        public EOF() {}
    }

    def renderContent(Reader reader, Writer out) {
        try {
            while(true) {
                skipUpto(reader, '<' as char, out)
                char next = getNext(reader)
                if(next == '/') {
                    out << "<"
                    out << next
                    skipUpto(reader, '>' as char, out)
                    out << ">"
                } else {
                    char[] terminatingChar = ['\0' as char] as char[]
                    String tagName = searchForAny(reader, [' ' as char, '>' as char] as char[], terminatingChar)
                    if(tagName.contains(":")) {
                        boolean hasEndTag = true
                        Map attrMap = [:]
                        if(terminatingChar[0] == ' ' as char) {
                            terminatingChar = ['\0' as char] as char[]
                            String attrs = searchForAny(reader, ['/' as char, '>' as char] as char[], terminatingChar)
                            try {
                                StringReader mapReader = new StringReader(attrs)
                                while(true) {
                                    String key = searchFor(mapReader, '=' as char).trim()
                                    searchFor(mapReader, '"' as char)
                                    String value = searchFor(mapReader, '"' as char).trim()
                                    attrMap.put(key, value)
                                }
                            } catch(EOF eof) {
                            }
                            if(terminatingChar[0] == '/' as char) {
                                hasEndTag = false
                                searchFor(reader, '>' as char)
                            }
                        } else if(tagName.charAt(tagName.length() - 1) == "/") {
                            tagName = tagName.substring(0, tagName.length() - 1)
                            hasEndTag = false
                        }
                        if(hasEndTag) {
                            searchFor(reader, '<' as char)
                            searchFor(reader, '>' as char)
                        }
                        String[] tags = ("" + next + tagName).split(":")
                        def tagLib = gspTagLibraryLookup.lookupTagLibrary(tags[0], tags[1])
                        if(tagLib) {
                            out << tagLib."${tags[1]}"(attrMap)
                        }
                    } else {
                        out << "<"
                        out << next
                        out << tagName
                        if(terminatingChar[0] != '>' as char) {
                            out << " "
                            skipUpto(reader, '>' as char, out)
                        }
                        out << ">"
                    }
                }
            }
        } catch(EOF eof) {
        }
    }

    private String searchFor(Reader reader, char lookup) {
        StringBuilder builder = new StringBuilder()
        char c
        while(true) {
            c = reader.read()
            if(c == (char)-1) {
                throw new EOF()
            }
            if(c == lookup) {
                return builder.toString()
            }
            builder << c
        }
    }

    private String searchForAny(Reader reader, char[] lookups, char[] terminatingChar) {
        StringBuilder builder = new StringBuilder()
        char c
        while(true) {
            c = reader.read()
            if(c == (char)-1) {
                throw new EOF()
            }
            if(lookups.contains(c)) {
                terminatingChar[0] = c
                return builder.toString()
            }
            builder << c
        }
    }

    private char getNext(Reader reader) {
        char c = reader.read()
        if(c == (char)-1) {
            throw new EOF()
        }
        return c
    }

    private char skipUpto(Reader reader, char lookup, Writer out) {
        char c
        while(true) {
            c = reader.read()
            if(c == (char)-1) {
                throw new EOF()
            }
            if(c == lookup) {
                return c
            }
            out << c
        }
    }

    def renderContent(String gsp, Writer out) {
        renderContent(new StringReader(gsp), out)
    }

    def renderContent(StreamCharBuffer gsp, Writer out) {
        renderContent(new StringReader(gsp.toString()), out)
    }

    String renderView(String viewPath, Map model, Writer out) {
        def source = groovyPageLocator.findViewByPath(viewPath)
        if (source == null) {
            return ""
        }
        def template = groovyPagesTemplateEngine.createTemplate(source)
        if (template != null) {
            template.make(model ?: [:]).writeTo(out)
        }
    }
}
