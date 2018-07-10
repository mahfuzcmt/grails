package com.webcommander.license

class OperatorPendingNotification {

    Long id

    String type
    String message
    String msgType
    String jsonArgs
    String subjectId
    String licenseIdentifier
    Boolean isObsolete = false

    Date created

    static constraints = {
        jsonArgs nullable: true, maxSize: 500
    }

    def beforeValidate() {
        if (!this.created) {
            this.created = new Date().gmt()
        }
    }
}