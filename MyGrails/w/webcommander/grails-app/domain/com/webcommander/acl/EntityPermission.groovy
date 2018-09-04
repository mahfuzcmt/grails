package com.webcommander.acl

import com.webcommander.admin.Operator

class EntityPermission {
    Long entityId

    Boolean isAllowed

    Operator user
    Permission permission

    static def deleteEntity(String typeName, Long id) {
        EntityPermission.where {
            def entity = EntityPermission
            exists(Permission.where {
                def perm = Permission
                eqProperty "perm.id", "entity.permission.id"
                type == "${typeName}"
            }.id()) and
            entityId == id
        }.deleteAll()
    }

}
