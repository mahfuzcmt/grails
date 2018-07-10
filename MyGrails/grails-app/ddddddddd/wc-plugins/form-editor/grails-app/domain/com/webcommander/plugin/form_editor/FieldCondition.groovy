package com.webcommander.plugin.form_editor

import com.webcommander.JSONSerializable

class FieldCondition extends JSONSerializable {
    Long id
    String targetOption
    String action
    String dependentFieldUUID

    static belongsTo = [formField: FormField]

    static clone_exclude = ["formField"]

    static constraints = {
    }
}
