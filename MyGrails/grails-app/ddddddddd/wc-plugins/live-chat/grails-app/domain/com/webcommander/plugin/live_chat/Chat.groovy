package com.webcommander.plugin.live_chat

class Chat {
    Long id
    String name
    String email
    String phone
    String subject
    String rating
    Boolean isComplete = false
    Long agentId

    Date created
    Date updated

    Collection<ChatMessage> messages = []
    Collection<ChatTag> tags = []

    static hasMany = [messages: ChatMessage, tags: ChatTag]

    static constraints = {
        email(nullable: true)
        phone(nullable: true)
        rating(nullable: true)
        subject(nullable: true)
        agentId(nullable: true);
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
