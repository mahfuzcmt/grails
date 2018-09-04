package com.webcommander.plugin.news

import com.webcommander.content.Article

class News {
    Long id

    String title
    Date newsDate
    String summary
    Boolean isInTrash = false
    Boolean isDisposable = false
    Article article

    Date created
    Date updated

    static constraints = {
        title(nullable: false, size: 2..100)
        summary(nullable: true, maxSize: 500)
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeInsert() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }
}