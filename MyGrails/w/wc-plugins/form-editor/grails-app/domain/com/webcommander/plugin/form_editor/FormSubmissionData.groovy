package com.webcommander.plugin.form_editor

import com.webcommander.events.AppEventManager

class FormSubmissionData {

    Long id
    String fieldName
    String fieldValue
    Boolean isFile = false
    FormSubmission formSubmission

    static belongsTo = [formSubmission: FormSubmission]

    static constraints = {
        fieldName(blank: false, maxSize: 200)
        fieldValue(nullable: true)
    }

    static mapping = {
        fieldValue type: "text"
    }
}
