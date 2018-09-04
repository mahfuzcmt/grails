package com.webcommander.plugin.form_editor

import com.webcommander.JSONSerializable
import com.webcommander.plugin.form_editor.constants.DomainConstants

class Form extends JSONSerializable {

    Long id

    String name
    String clazz
    String submitButtonLabel = "s:submit"
    String actionType = DomainConstants.FORM_ACTION_TYPE.INTERNAL
    String actionUrl
    String emailTo
    String emailCc
    String emailBcc
    String senderEmailFieldUUID
    String emailSubject
    String successMessage
    String failureMessage
    String beforeHandler
    String afterHandler
    String termCondition
    String termConditionText

    Boolean isInTrash = false
    Boolean isDisposable = false

    Boolean useCaptcha = false
    Boolean resetEnabled = true
    Boolean isTermConditionEnabled = false
    Boolean isTermConditionTextEnabled = false

    Integer submissionCount = 0;

    Date created
    Date updated

    Collection<FormField> fields = []

    static hasMany = [fields: FormField]

    static clone_exclude = ["id", "name", "created", "updated"]

    static constraints = {
        clazz(nullable: true)
        actionUrl(nullable: true)
        emailSubject nullable: true
        emailTo(nullable: true)
        emailCc(nullable: true)
        emailBcc nullable: true
        successMessage nullable: true, maxSize: 500
        failureMessage nullable: true, maxSize: 500
        beforeHandler nullable: true, maxSize: 1000
        afterHandler nullable: true, maxSize: 1000
        senderEmailFieldUUID nullable: true
        termCondition nullable: true, maxSize: 500
        termConditionText nullable: true, maxSize: 500
    }

    static mapping = {
        actionUrl length: 500
    }

    def beforeValidate() {
        if(!this.created) {
            this.created = new Date().gmt()
        }
        if(!this.updated) {
            this.updated = new Date().gmt()
        }
    }

    def beforeUpdate() {
        this.updated = new Date().gmt()
    }

}
