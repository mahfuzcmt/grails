package com.webcommander.webmarketting

class Newsletter {

    Long id

    String title
    String sender
    String subject
    String body

    Date scheduleTime = null
    Date created
    Date updated

    Boolean isSent = false
    Boolean isActive = true

    Collection<NewsletterReceiver> newsletterReceivers = []

    static hasMany = [newsletterReceivers: NewsletterReceiver]

    static constraints = {
        scheduleTime(nullable: true)
    }

    static mapping = {
        body type: "text"
        newsletterReceivers cache: true
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
