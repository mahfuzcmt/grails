package com.webcommander.plugin.live_chat

import com.webcommander.admin.Operator

class ChatDepartment {
    Long id
    String name
    String description
    String defaultWelcomeMessage

    Date created
    Date updated

    Collection<Operator> operators = []

    static hasMany = [operators: Operator];

    static constraints = {
        defaultWelcomeMessage(nullable: true, maxSize: 550);
        description(maxSize: 200);
    }

    def beforeValidate() {
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
