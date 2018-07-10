package com.webcommander.plugin.form_editor

import com.webcommander.JSONSerializable

class FormField extends JSONSerializable {

    Long id
    String uuid
    String type //textbox,textarea,checkbox,radio,select,file,password,hidden
    String label
    String name
    String title
    String clazz
    String value
    String placeholder
    String validation

    Collection<FormExtraProp> extras = []
    Collection<FormField> fields = []
    Collection<FieldCondition> conditions = []

    private Map configs = null

    static hasMany = [extras: FormExtraProp, fields: FormField, conditions: FieldCondition]

    static transients = ["configs"]

    static clone_exclude = ["id", "uuid"]

    static constraints = {
        title nullable: true, maxSize: 100
        clazz nullable: true, maxSize: 100
        label nullable: true
        name maxSize: 100
        value maxSize: 500, nullable: true
        placeholder nullable: true
        validation nullable: true
        uuid unique: true
    }

    public Map getConfigs() {
        if(configs == null) {
            configs = [:]
            for (FormExtraProp prop : extras) {
                if(prop.type == "config") {
                    configs[prop.label] = prop.value
                }
            }
        }
        return configs
    }
}