package com.webcommander.acl

import com.webcommander.admin.Operator

class UserPermission {
    Boolean isAllowed

    Operator user
    Permission permission
}