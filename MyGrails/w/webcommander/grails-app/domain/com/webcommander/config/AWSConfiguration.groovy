package com.webcommander.config

class AWSConfiguration {
    Long id

    String bucketName
    String accessKey
    String secretKey
    String type

    Boolean isActive

    Date updated

    static constraints = {
        bucketName(nullable: false)
        accessKey(nullable: false)
        secretKey(nullable: false)
        type(nullable: true)
        isActive(nullable: true)
    }

    def beforeValidate() {
        if (!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }
}
