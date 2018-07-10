package com.webcommander.oauth2

class OAuthClient {

    Long id
    String uuid
    String name
    String displayName
    String description

    Boolean enabled = true
    String clientId
    String clientSecret
    String redirectUrl
    Integer dailyCount = 0
    Boolean visible = true

    Date created
    Date updated

    static constraints = {
        uuid(nullable: true, unique: true)
        name(blank: false, minSize: 2, maxSize: 255)
        displayName(blank: false, minSize: 2, maxSize: 255)
        description(nullable: true, maxSize: 2000)
        clientId(unique: true)
        visible(nullable: true)
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
