package com.webcommander.webmarketting

class NewsletterSubscriber {

    Long id
    String title
    String firstName
    String lastName
    String email
    Boolean isSubscribed = true
    Date created
    Date updated

    Collection<NewsletterUnsubscribeHistory> history = []

    static hasMany = [history: NewsletterUnsubscribeHistory]

    static constraints = {
        title(nullable: true)
        firstName(nullable: true)
        lastName(nullable: true)
        email(unique: true)
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
