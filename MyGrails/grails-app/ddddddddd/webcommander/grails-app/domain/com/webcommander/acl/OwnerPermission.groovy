package com.webcommander.acl

import com.webcommander.events.AppEventManager

class OwnerPermission {
    Permission permission

    static initialize() {
        def _init = {
            if(OwnerPermission.count() == 0) {
                Permission.where {
                    applicableOnEntity == true
                }.list().each {
                    new OwnerPermission(permission: it).save()
                }
            }
        }

        if(Permission.count()) {
            _init()
        } else {
            AppEventManager.one("permission-bootstrap-init", "bootstrap-init", _init)
        }
    }
}
