package com.webcommander.installation.template

class TemplateContent {

    Long id
    Long contentId
    String contentType

    Date created

    def beforeValidate() {
        if(!this.created) {
            this.created = new Date().gmt()
        }
    }
}
