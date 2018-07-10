package com.webcommander.plugin.enterprise_variation

class EvariationDescription {
    Long id

    String content

    static constraints = {
        content(nullable: true)
    }

    static mapping = {
        content(type: "text")
    }

}
