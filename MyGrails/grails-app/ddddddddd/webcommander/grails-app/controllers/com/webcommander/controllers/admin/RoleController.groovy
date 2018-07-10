package com.webcommander.controllers.admin

import com.webcommander.acl.EntityPermission
import com.webcommander.acl.OwnerPermission
import com.webcommander.acl.RolePermission
import com.webcommander.acl.UserPermission
import com.webcommander.admin.Operator
import com.webcommander.admin.Role
import com.webcommander.admin.RoleService
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.authentication.annotations.Restrictions
import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.manager.HookManager
import com.webcommander.models.RestrictionPolicy
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.AppUtil
import grails.converters.JSON
import com.webcommander.acl.Permission

class RoleController {
    CommonService commonService
    RoleService roleService

    @Restriction(permission = "role.view.list")
    def loadAppView() {
        Integer count = roleService.getRolesCount(params);
        params.max = params.max ?: "10";
        List<Role> roles = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
            params.offset = offset
            roleService.getRoles(params)
        }
        render view: "/admin/role/appView", model: [count: count, roles: roles];
    }

    def create() {
        render view: "/admin/role/create", model: [role: new Role()];
    }

    def edit() {
        Role role = Role.get(params.long("id"));
        render(view: "/admin/role/create", model: [role: role]);
    }

    def view() {
        Role role = Role.get(params.long("id"));
        render(view: "/admin/role/viewRole", model: [role: role]);
    }

    def isUnique() {
        roleService.checkRoleNameForConflict(params.value, params.long("id"));
        render([status: "success", message: g.message(code: "provided.field.available", args: [params.field, params.value])] as JSON)
    }

    @License(required = "allow_acl_feature")
    def save() {
        params.remove("action");
        params.remove("controller")
        if (roleService.save(params)) {
            render([status: "success", message: g.message(code: "role.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "role.save.failed")] as JSON)
        }
    }

    def advanceFilter() {
        render(view: "/admin/role/filter", model: [get: 0]);
    }

    @License(required = "allow_acl_feature")
    def manageUsers() {
        def users = Operator.list();
        def role = Role.get(params.long("id"))
        render(view: "/admin/role/manageUsers", model: [users: users, role: role])
    }

    @Restrictions([
            @Restriction(permission = "page.edit.permission", params_match_key = ["type", "for"], params_match_value = ["page", "owner"]),
            @Restriction(permission = "page.edit.permission", params_match_key = ["type", "for"], params_match_value = ["page", "entity"]),
            @Restriction(permission = "page.edit.permission", params_match_key = "type", params_match_value = "page"),

            @Restriction(permission = "product.edit.permission", params_match_key = ["type", "for"], params_match_value = ["product", "owner"]),
            @Restriction(permission = "product.edit.permission", params_match_key = ["type", "for"], params_match_value = ["product", "entity"]),
            @Restriction(permission = "product.edit.permission", params_match_key = "type", params_match_value = "product"),

            @Restriction(permission = "category.edit.permission", params_match_key = ["type", "for"], params_match_value = ["category", "owner"]),
            @Restriction(permission = "category.edit.permission", params_match_key = ["type", "for"], params_match_value = ["category", "entity"]),
            @Restriction(permission = "category.edit.permission", params_match_key = "type", params_match_value = "category"),

            @Restriction(permission = "section.edit.permission", params_match_key = ["type", "for"], params_match_value = ["section", "owner"]),
            @Restriction(permission = "section.edit.permission", params_match_key = ["type", "for"], params_match_value = ["section", "entity"]),
            @Restriction(permission = "section.edit.permission", params_match_key = "type", params_match_value = "section"),

            @Restriction(permission = "article.edit.permission", params_match_key = ["type", "for"], params_match_value = ["article", "owner"]),
            @Restriction(permission = "article.edit.permission", params_match_key = ["type", "for"], params_match_value = ["article", "entity"]),
            @Restriction(permission = "article.edit.permission", params_match_key = "type", params_match_value = "article"),

            @Restriction(permission = "navigation.edit.permission", params_match_key = ["type", "for"], params_match_value = ["navigation", "owner"]),
            @Restriction(permission = "navigation.edit.permission", params_match_key = ["type", "for"], params_match_value = ["navigation", "entity"]),
            @Restriction(permission = "navigation.edit.permission", params_match_key = "type", params_match_value = "navigation"),

            @Restriction(permission = "administration.edit.permission"),
            @Restriction(permission = "asset_library.edit.permission", params_match_key = "type", params_match_value = "asset_library"),
            @Restriction(permission = "customer.edit.permission", params_match_key = "type", params_match_value = "customer"),
            @Restriction(permission = "order.edit.permission", params_match_key = "type", params_match_value = "order"),
            @Restriction(permission = "operator.edit.permission", params_match_key = "type", params_match_value = "user"),
            @Restriction(permission = "role.edit.permission", params_match_key = "type", params_match_value = "role"),
            @Restriction(permission = "trash.edit.permission", params_match_key = "type", params_match_value = "trash")
    ])
    @License(required = "allow_acl_feature")
    def managePermissions() {
        Map resp = [allowed: true]
        //TODO: Improvement Needed
        resp = HookManager.hook("beforeManageUserPermission", resp, params)
        if(!resp.allowed) {
            String name = g.message(code: resp.deniedPolicy.permission)
            String message = g.message(code: "not.allowed.to.do", args: [name])
            render([view: "/admin/forbidden", model: [message: message]])
            return
        }
        Long id = params.long("id") ?: 0
        boolean ecommerce = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce").toBoolean()
        def permissionTypes = params.type ? [params.type] : Permission.types;
        def ecommercePermissionTypes = DomainConstants.ECOMMERCE_PERMISSION_CHECKLIST
        ecommercePermissionTypes.each {
            if (!ecommerce && it.value) {
                permissionTypes.remove(it.key)
            }
        }
        String firstPermissionType = permissionTypes[0];
        boolean permittedForFirstView = true
        if(!params.type) {
            permittedForFirstView = roleService.isPermitted(session.admin, new RestrictionPolicy(type: firstPermissionType, permission: "edit.permission"), [type: firstPermissionType])
        }
        def permissions = Permission.createCriteria().list {
            eq("type", firstPermissionType)
            if(params.for == "owner" || params.for == "entity") {
                eq "applicableOnEntity", true
            }
        }

        def allowedPermissions
        def deniedPermissions
        if(permittedForFirstView) {
            String convertedFor = params.for == "operator" ? "user" : params.for
            switch(convertedFor) {
                case "role":
                case "user":
                    Class domain = params.for == "operator" ? UserPermission : RolePermission
                    allowedPermissions = domain.createCriteria().list {
                        projections {
                            property("permission")
                        }
                        eq("${convertedFor}.id", id)
                        inList("permission.id", permissions.id)
                        eq("isAllowed", true)
                    }
                    deniedPermissions = domain.createCriteria().list {
                        projections {
                            property("permission")
                        }
                        eq("${convertedFor}.id", id)
                        inList("permission.id", permissions.id)
                        eq("isAllowed", false)
                    }
                    break;
                case "owner":
                    allowedPermissions = permissions.id ? OwnerPermission.createCriteria().list {
                        projections {
                            property("permission")
                        }
                        inList("permission.id", permissions.id)
                    } : []
                    break;
                case "entity":
                    allowedPermissions = permissions.id ? EntityPermission.createCriteria().list {
                        projections {
                            property("permission")
                        }
                        inList("permission.id", permissions.id)
                        eq "entityId", id
                        eq "isAllowed", true
                        eq "user.id", params.user?.toLong(0) ?: session.admin
                    } : []
                    deniedPermissions = permissions.id ? EntityPermission.createCriteria().list {
                        projections {
                            property("permission")
                        }
                        inList("permission.id", permissions.id)
                        eq "entityId", id
                        eq "isAllowed", false
                        eq "user.id", params.user?.toLong(0) ?: session.admin
                    } : []
            }
        }

        String viewName = "managePermissions"
        if(params.for == "owner") {
            viewName = "manageOwnerPermissions"
        }
        List<Long> ids = params.list("id")*.toLong();
        render(view: "/admin/role/$viewName", model: [ids: ids, users: params.for == "entity" ? Operator.findAll() : null, types: permissionTypes, type: params.type ?: permissionTypes[0], allowedPermissions: allowedPermissions, deniedPermissions: deniedPermissions, permissions: permissions, permittedForFirstView: permittedForFirstView])
    }

    @Restrictions([
        @Restriction(permission = "page.edit.permission", params_match_key = ["type", "for"], params_match_value = ["page", "owner"]),
        @Restriction(permission = "page.edit.permission", params_match_key = ["type", "for"], params_match_value = ["page", "entity"]),
        @Restriction(permission = "page.edit.permission", params_match_key = "type", params_match_value = "page"),

        @Restriction(permission = "product.edit.permission", params_match_key = ["type", "for"], params_match_value = ["product", "owner"]),
        @Restriction(permission = "product.edit.permission", params_match_key = ["type", "for"], params_match_value = ["product", "entity"]),
        @Restriction(permission = "product.edit.permission", params_match_key = "type", params_match_value = "product"),

        @Restriction(permission = "category.edit.permission", params_match_key = ["type", "for"], params_match_value = ["category", "owner"]),
        @Restriction(permission = "category.edit.permission", params_match_key = ["type", "for"], params_match_value = ["category", "entity"]),
        @Restriction(permission = "category.edit.permission", params_match_key = "type", params_match_value = "category"),

        @Restriction(permission = "section.edit.permission", params_match_key = ["type", "for"], params_match_value = ["section", "owner"]),
        @Restriction(permission = "section.edit.permission", params_match_key = ["type", "for"], params_match_value = ["section", "entity"]),
        @Restriction(permission = "section.edit.permission", params_match_key = "type", params_match_value = "section"),

        @Restriction(permission = "article.edit.permission", params_match_key = ["type", "for"], params_match_value = ["article", "owner"]),
        @Restriction(permission = "article.edit.permission", params_match_key = ["type", "for"], params_match_value = ["article", "entity"]),
        @Restriction(permission = "article.edit.permission", params_match_key = "type", params_match_value = "article"),

        @Restriction(permission = "navigation.edit.permission", params_match_key = ["type", "for"], params_match_value = ["navigation", "owner"]),
        @Restriction(permission = "navigation.edit.permission", params_match_key = ["type", "for"], params_match_value = ["navigation", "entity"]),
        @Restriction(permission = "navigation.edit.permission", params_match_key = "type", params_match_value = "navigation"),

        @Restriction(permission = "administration.edit.permission"),
        @Restriction(permission = "asset_library.edit.permission", params_match_key = "type", params_match_value = "asset_library"),
        @Restriction(permission = "customer.edit.permission", params_match_key = "type", params_match_value = "customer"),
        @Restriction(permission = "order.edit.permission", params_match_key = "type", params_match_value = "order"),
        @Restriction(permission = "operator.edit.permission", params_match_key = "type", params_match_value = "user"),
        @Restriction(permission = "role.edit.permission", params_match_key = "type", params_match_value = "role"),
        @Restriction(permission = "trash.edit.permission", params_match_key = "type", params_match_value = "trash")
    ])
    @License(required = "allow_acl_feature")
    def savePermissions() {
        Map resp = [allowed: true]
        resp = HookManager.hook("beforeSaveUserPermission", resp, params)
        if(!resp.allowed) {
            String name = g.message(code: resp.deniedPolicy.permission)
            throw new ApplicationRuntimeException("not.allowed.to.do", [name])
        }
        if (roleService.updatePermission(params)) {
            render([status: "success", message: g.message(code: "permission.update.successfully")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "permission.update.failed")] as JSON);
        }
    }

    @License(required = "allow_acl_feature")
    def updateUsers() {
        def users = params.list("users").collect {it.toLong(0)};
        if (roleService.updateUsers(users, params.long("id"))) {
            render([status: "success", message: g.message(code: "operators.for.role.update.successfully")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "operators.for.role.update.failed")] as JSON);
        }
    }

    @License(required = "allow_acl_feature")
    def delete() {
        Long id = params.long("id");
        try {
            if (roleService.deleteRole(id, params.at1_reply, params.at2_reply)) {
                render([status: "success", message: g.message(code: "role.delete.success")] as JSON);
            } else {
                render([status: "error", message: g.message(code: "role.delete.failed")] as JSON);
            }
        } catch (AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    @License(required = "allow_acl_feature")
    def deleteSelected() {
        List<Long> ids = params.list("ids").collect { it.toLong() }
        int deleteCount = roleService.deleteSelected(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.roles.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.roles.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "role")])] as JSON)
        }
    }
}
