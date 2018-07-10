package com.webcommander.plugin.popup

class Popup {
    Long id

    String name
    String contentType = "html"
    String content
    String identifier

    Long contentId
    Boolean isDisposable = false

    Date created

    static constraints = {
        content(nullable: true)
        contentId(nullable: true)
        identifier(unique: true)
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
    }
    static mapping = {
        content type: "text"
    }

    public static void initialize() {
    }
}