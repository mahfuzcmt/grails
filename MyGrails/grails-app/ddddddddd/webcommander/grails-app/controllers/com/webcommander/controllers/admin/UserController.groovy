package com.webcommander.controllers.admin

import com.webcommander.admin.Operator
import com.webcommander.admin.Role
import com.webcommander.admin.UserService
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.Restriction
import com.webcommander.common.AuthenticationService
import com.webcommander.common.CommonService
import com.webcommander.license.blocker.OperatorLicense
import com.webcommander.manager.LicenseManager
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.throwables.ApplicationRuntimeException
import grails.converters.JSON

class UserController {
    CommonService commonService
    UserService userService
    ProvisionAPIService provisionAPIService
    AuthenticationService authenticationService

    @Restriction(permission = "operator.view.list")
    def loadAppView() {
        Integer count
        params.max = params.max ?: "10"
        List users
        if(LicenseManager.isProvisionActive()) {
            users = provisionAPIService.entityList()
            count = users.size()
        } else {
            count = userService.getUsersCount(params)
            users = commonService.withOffset(params.max, params.offset, count) { max, offset, _count ->
                params.offset = offset
                userService.getUsers(params)
            }
        }
        render(view: "/admin/user/appView", model: [count: count, users: users])
    }

    def viewInfo() {
        Operator user = Operator.get(params.int("id") ?: 0);
        if (!user) {
            response.sendError(500, "operator.not.found")
            return;
        }
        render(view: "/admin/user/infoView", model: [user: user])
    }

    @Restriction(permission = "operator.edit", entity_param = "id", domain = Operator)
    def editInfo() {
        def isLogged = false;
        Operator user = Operator.get(params.int("id") ?: 0);
        if (!user) {
            response.sendError(500, "operator.not.found")
            return;
        }
        if(session.admin == user.id){
            isLogged = true;
        }
        render(view: "/admin/user/infoEdit", model: [user: user, isLogged: isLogged])
    }

    @Restriction(permission = "operator.create")
    @License(required = "operator_limit", checker = OperatorLicense)
    def create() {
        render(view: "/admin/user/createUser", model: [get: 0])
    }

    @Restriction(permission = "operator.create")
    @License(required = "operator_limit", checker = OperatorLicense)
    def save() {
        if(params.deleteTrashItem) {
            def field = params.deleteTrashItem.collect{it.key}[0];
            def value = params[field];
            userService.deleteTrashItemAndSaveCurrent(field, value)
        }
        if (userService.update(params, true)) {
            render([status: "success", message: g.message(code: "operator.save.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "operator.save.failed")] as JSON)
        }
    }

    @Restriction(permission = "operator.assign.roles", entity_param = "id", domain = Operator)
    @License(required = "allow_acl_feature")
    def manageRoles() {
        def roles = Role.list();
        def user = Operator.get(params.long("id"))
        render(view: "/admin/user/manageRoles", model: [user: user, roles: roles])
    }

    @Restriction(permission = "operator.assign.roles", entity_param = "id", domain = Operator)
    @License(required = "allow_acl_feature")
    def bulkManageRoles() {
        def roles = Role.list();
        render(view: "/admin/user/bulkManageRoles", model: [roles: roles])
    }

    @Restriction(permission = "operator.assign.roles", entity_param = "id", domain = Operator)
    @License(required = "allow_acl_feature")
    def updateRoles() {
        List<Long> roles = params.list("roles")*.toLong();
        List<Long> ids = params.list("id")*.toLong();
        if (userService.updateRoles(roles, ids)) {
            render([status: "success", message: g.message(code: "role.added.to.operator.successfully")] as JSON);
        } else {
            render([status: "error", message: g.message(code: "role.add.to.operator.failed")] as JSON);
        }
    }

    def isUnique() {
        render(commonService.responseForUniqueField(Operator, params.long("id"), params.field, params.value) as JSON)
    }

    @Restriction(permission = "operator.edit", entity_param = "id", domain = Operator)
    @License(required = "operator_limit", checker = OperatorLicense)
    def update() {
        params.id = params.long("id");
        if (!params.active && session.admin == params.id) {
            render([status: "error", message: g.message(code: 'cannot.make.inactive.oneself')] as JSON)
            return;
        }
        Operator operator = Operator.get(params.id)
        if (params.isChangePassword == "true" && !authenticationService.verifyUser(operator.email, params.oldPassword)) {
            render([status: "error", message: g.message(code: 'current.password.not.match')] as JSON)
            return;
        }
        if (userService.update(params, params.isChangePassword == "true")) {
            render([status: "success", message: g.message(code: 'operator.update.success')] as JSON)
        } else {
            render([status: "error", message: g.message(code: 'operator.update.failed')] as JSON)
        }
    }

    @Restriction(permission = "operator.remove", entity_param = "id", domain = Operator)
    def delete() {
        Long id = params.long("id")
        def error = false
        if (session.admin == id) {
            render([status: "alert", message: g.message(code: "cannot.delete.oneself")] as JSON)
            error = true
        }
        if(!error){
            Boolean result = LicenseManager.isProvisionActive() ? userService.deleteOperator(id) : userService.putUserInTrash(id)
            if (result) {
                render([status: "success", message: g.message(code: "operator.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "operator.delete.failed")] as JSON)
            }
        }

    }

    @Restriction(permission = "operator.remove", entity_param = "ids", domain = Operator)
    def deleteSelected(){
        def ids = params.list("ids").collect {it.toLong()}
        def error = false
        ids.each { id ->
            if(session.admin == id) {
                render([status: "error", message: g.message(code: "cannot.delete.oneself")] as JSON)
                error = true
            }
        }
        if(!error){
            if(!userService.putSelectedUsersInTrash(ids)){
                render([status: "success", message: g.message(code: "operators.delete.success")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "operators.delete.failed")] as JSON)
            }
        }
    }

    def advanceFilter() {
        render(view: "/admin/user/filter", model: [get: 0]);
    }

    def restoreFromTrash() {
        def field = params.field;
        def value = params.value;
        Long id = userService.restoreOperatorFromTrash(field, value);
        if(id){
            render([status: "success", message: g.message(code: "restored.successfully", args: [g.message(code: "operator")]), type: "operator", id: id] as JSON)
        }
    }

    def saveIsMatured() {
        userService.setIsMatured(params.check.toBoolean())
        render([status: "success"] as JSON)
    }

    def getIsMatured() {
        boolean isMatured = userService.getIsMatured()
        render([value: isMatured] as JSON)
    }

    /* SSO */
    def confirm() {
        try {
            def result = provisionAPIService.approveByToken(params.token.toString())
            if(result.newUser) {
                flash.param = [token: result.token]
                redirect(controller: "userAuthentication", action: "resetPassword")
                return;
            }
            flash.model = [type: "success", message: g.message(code: "successfully.approved")]
        } catch (Exception e) {
            throw new ApplicationRuntimeException(e.getMessage())
        }
    }

    def loadStatusOption() {
        render view: "/admin/user/statusOption";
    }

    def changeStatus() {
        List<Long> ids = params.list("id")*.toLong();
        Boolean status = params.status == "true";
        if(userService.changeStatus(ids, status)) {
            render([status: "success", message: g.message(code: "operator.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "operator.update.failed")] as JSON)
        }
    }

    def loadApiOption() {
        render view: "/admin/user/apiOption";
    }

    def changeApi() {
        List<Long> ids = params.list("id")*.toLong();
        Boolean apiAccess = params.isAPIAccessOnly == "on" ? true : false;
        if(userService.changeApi(ids, apiAccess)) {
            render([status: "success", message: g.message(code: "operator.update.success")] as JSON)
        }else {
            render([status: "success", message: g.message(code: "operator.update.failed")] as JSON)
        }
    }

}
