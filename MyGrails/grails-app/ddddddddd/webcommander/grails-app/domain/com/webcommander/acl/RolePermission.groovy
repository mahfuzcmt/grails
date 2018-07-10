package com.webcommander.acl

import com.webcommander.admin.Role
import com.webcommander.events.AppEventManager

class RolePermission {
    Boolean isAllowed

    Role role
    Permission permission

    static initialize() {
        def _init = {
            if(!RolePermission.count()) {
                Role role = Role.findByName("Admin")
                Permission.list().each {
                    new RolePermission(role: role, permission: it, isAllowed: true).save()
                }
            }
        }
        def _init_role_pass = {
            if(Permission.count()) {
                _init()
            } else {
                AppEventManager.one("permission-bootstrap-init", "bootstrap-init", _init)
            }
        }
        if(Role.count()) {
            _init_role_pass()
        } else {
            AppEventManager.one("role-bootstrap-init", "bootstrap-init", _init_role_pass)
        }
    }
}
