package com.webcommander.webmarketting

class NewsletterReceiver {

    Long id
    String receiverType //customer, customerGroup, email, subscriber
    Long receiverId
    Newsletter parent

    Date created
    Date updated

    static belongsTo = [newsletter: Newsletter]

    static constraints = {
        receiverId (nullable: true)
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
