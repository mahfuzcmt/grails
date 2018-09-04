package com.webcommander.admin

import com.webcommander.events.AppEventManager
import com.webcommander.util.StringUtil

class Operator {

    Long id
    String fullName
    String uuid
    String email
    String password
    Boolean isActive = true
    Boolean isInTrash = false
    Boolean isAPIAccessOnly = false
    Date created
    Date updated
    Boolean isMatured = false

    Collection<Role> roles = []

    static hasMany = [roles: Role]

    static mapping = {
        roles cache: true
    }

    static constraints = {
        fullName size: 2..200
        email unique: true
        uuid nullable: true, unique: true
    }

    public static void initialize() {
        def _init = {
            if (Operator.countByEmailNotEqual("implementer@webcommander") == 0) {
                def insertSql = [
                    ['WebCommander Administrator', 'admin'.encodeAsMD5(), "admin@webcommander.com", Role.findByName("Admin")]
                ]
                insertSql.each {
                    new Operator(fullName: it[0], password: it[1], email: it[2], roles: [it[3]], uuid: StringUtil.uuid).save();
                }
            }
            if(!Operator.findByEmail("implementer@webcommander")) {
                new Operator(email: "implementer@webcommander", fullName: "Webcommander Implementer", password: "C7012FD8-78DC-4585-8415-9987CA8BC52E".encodeAsMD5(), uuid: StringUtil.uuid).save()
            }
        }
        if(Role.count()) {
            _init()
        } else {
            AppEventManager.one("role-bootstrap-init", "bootstrap-init", _init)
        }
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

    @Override
    boolean equals(Object obj) {
        if(!obj instanceof Operator) {
            return false;
        }
        if(this.id) {
            return this.id == obj.id;
        }
        if(this.email) {
            this.email == obj.email;
        }
        return super.equals(obj);
    }

    @Override
    int hashCode() {
        if(this.id) {
            return ("Operator: " + this.id).hashCode();
        }
        if(this.email) {
            return ("Operator: " + this.email).hashCode();
        }
        return super.hashCode()
    }
}
