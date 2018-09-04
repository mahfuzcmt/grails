package com.webcommander.plugin.snippet

import com.webcommander.admin.Operator
import com.webcommander.content.Section
import com.webcommander.util.AppUtil

class Snippet {
    Long id
    String name
    String description

    Boolean isDisposable
    Boolean isAutoGenerated = false

    Operator createdBy
    Date created
    Date updated

    static belongsTo = [parent: Section]

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
        if (!this.createdBy) {
            def operator = AppUtil.session.admin ? Operator.proxy(AppUtil.session.admin) : null
            this.createdBy = operator
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

    static constraints = {
        name(blank: false, unique: true, size: 2..100)
        description(nullable: true, maxSize: 500)
        parent(nullable: true)
        createdBy(nullable: true)
    }
}