package com.webcommander.plugin.myob

class MyobLink {

    Long id;
    Long componentId

    String linkComponent
    String uid
    String myobVersion

    Date created
    Date updated

    static constraints = {
        componentId unique: "linkComponent"
    }

    def beforeValidate() {
        if(!created) {
            created = new Date()
        }
        updated = new Date()
    }

    def beforeUpdate() {
        updated = new Date()
    }
}
