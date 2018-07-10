package com.webcommander.plugin.form_editor
/**
 * Created by shahin on 24/03/14.
 */
class FormSubmission {
    Long id
    String ip
    Date submitted

    Form form
    List<FormSubmissionData> submittedDataList = new LinkedList<FormSubmissionData>()
    static belongsTo = [form: Form]
    static hasMany = [submittedDataList: FormSubmissionData]

    static mapping = {
        submittedDataList cascade: "all-delete-orphan"
    }

    def beforeValidate() {
        if(!this.submitted) {
            this.submitted = new Date().gmt()
        }
    }
}
