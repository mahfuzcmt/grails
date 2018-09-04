package com.webcommander.admin

import com.webcommander.events.AppEventManager

class Role {

    Long id
    String name
    String description
    Date created
    Date updated

    Collection<Operator> users = []

    static hasMany = [users : Operator]
    static belongsTo = [Operator]

    static mapping = {
        users cache: true
        description length: 255
    }

    static constraints = {
        name(unique: true, size: 2..100)
        description(nullable: true, maxSize: 255)
    }

    public static void initialize() {
        if (Role.count() == 0) {
            def insertSql = [
                ["Admin", "System Administrator"],
                ["Moderator", "System Maintenance Users"],
                ["Basic Operator", "Default Operator"]
            ]
            insertSql.each {
                new Role(name: it[0], description: it[1]).save();
            }
            AppEventManager.fire("role-bootstrap-init")
        }
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

    @Override
    boolean equals(Object anotherRole) {
        if (!anotherRole instanceof Role){
            return false
        }
        if (this.id) {
            return anotherRole.id = this.id;
        }
        if (this.name) {
            return anotherRole.name == this.name;
        }
        return super.equals(anotherRole)
    }

    @Override
    int hashCode() {
        if (this.id) {
            return ("Role: " + this.id).hashCode()
        }
        if (this.name) {
            return ("Role: " + this.name).hashCode()
        }
        return super.hashCode();
    }
}
