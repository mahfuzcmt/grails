package com.webcommander.plugin.form_editor

import com.webcommander.JSONSerializable

class FormExtraProp extends JSONSerializable {
    Long id

    String type
    String label
    String value
    String extraValue

    static clone_exclude = ["id"]

    static constraints = {
        label nullable: true, maxSize: 100
        value maxSize: 255
        extraValue nullable: true
    }

}
