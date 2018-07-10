package com.webcommander.webmarketting

class NewsletterUnsubscribeHistory {

    Long id
    Date subscribed
    Date unsubscribed
    String reason

    NewsletterSubscriber subscriber

    static belongsTo = [subscriber: NewsletterSubscriber]

    static constraints = {
        reason(maxSize: 2000)
    }

    static mapping = {
        reason(type: "text")
    }

    def beforeValidate() {
        if (!this.unsubscribed) {
            this.unsubscribed = new Date().gmt()
        }
    }
}
