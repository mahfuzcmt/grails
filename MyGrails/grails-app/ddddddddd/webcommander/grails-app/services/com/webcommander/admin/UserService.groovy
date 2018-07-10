package com.webcommander.admin

import com.webcommander.acl.UserPermission
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CacheManager
import com.webcommander.manager.LicenseManager
import com.webcommander.sso.ProvisionAPIService
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import grails.gorm.transactions.Transactional

@Initializable
@Transactional
class UserService {
    CommonService commonService
    TrashService trashService
    CommanderMailService commanderMailService
    ProvisionAPIService provisionAPIService

    static void initialize() {
        AppEventManager.on("before-operator-delete", { id ->
            UserPermission.where {
                user == Operator.proxy(id)
            }.deleteAll()
        })
    }

    private Closure getFilterClosure(Map params) {
        def session = AppUtil.session;
        return {
            if (params.searchText) {
                String searchText = "%${params.searchText.trim().encodeAsLikeText()}%"
                or {
                    ilike("fullName", searchText)
                    ilike("email", searchText)
                }
            }
            if (params.fullName) {
                ilike("fullName", "%${params.fullName.trim().encodeAsLikeText()}%")
            }
            if (params.email) {
                ilike("email", "%${params.email.trim().encodeAsLikeText()}%")
            }
            if (params.status) {
                eq("isActive", params.status == "active")
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone)
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone)
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            ne("email", "implementer@webcommander")
            eq("isInTrash", false);

        }
    }

    private Closure getCriteriaClosureForTrash(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("fullName", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.deletedFrom) {
                Date date = params.deletedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.deletedTo) {
                Date date = params.deletedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            eq("isInTrash", true);
        }
        return closure;
    }

    List<Operator> getUsers(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return Operator.createCriteria().list(listMap) {
            and getFilterClosure(params)
            order(params.sort ?: "fullName", params.dir ?: "asc")
        }
    }

    int getUsersCount(Map params) {
        return Operator.createCriteria().count(getFilterClosure(params))
    }

    boolean isUnique(Long id, String field, String value) {
        int count = Operator.createCriteria().count {
            if (id) {
                ne("id", id)
            }
            eq(field, value)
        }
        if (count > 0) {
            throw new ApplicationRuntimeException("provided.field.value.exists", [field, value])
        }
    }

    @Transactional
    boolean update(Map toUpdate, Boolean isChangePassword) {
        isUnique(toUpdate.id, "email", toUpdate["email"])
        Operator user = toUpdate.id ? Operator.get(toUpdate.id) : new Operator();
        String oldEmail = user.email
        user.fullName = toUpdate.fullName
        user.email = toUpdate.email ?: user.email
        user.isActive = toUpdate.active.toBoolean()
        user.isAPIAccessOnly = toUpdate.isAPIAccessOnly.toBoolean()
        user.uuid = user.uuid ?: StringUtil.uuid
        Boolean isInsert = true
        if (isChangePassword) {
            user.password = toUpdate.id ? toUpdate.password.encodeAsMD5() : "change it".encodeAsMD5()
        }
        if(isChangePassword && toUpdate.id && LicenseManager.isProvisionActive()) {
            provisionAPIService.changePassword(oldEmail, user.password)
        }
        if (toUpdate.id) {
            isInsert = false
            user.save()
            if(user.email != oldEmail && LicenseManager.isProvisionActive()) {
                provisionAPIService.changeEmail(oldEmail, user.email)
            }
        } else {
            user.save();
        }
        if(!user.hasErrors() && isInsert) {
            Map response = LicenseManager.isProvisionActive() ? syncUser(user) : [:]
            try {
                commanderMailService.sendCreateUserMail(user, response)
            } catch(Throwable h) {
                log.error(h.message, h)
            }
        }
        return !user.hasErrors();
    }

    @Transactional
    boolean updatePassword(Long id, String password) {
        Operator operator = Operator.get(id);
        try {
            if(LicenseManager.isProvisionActive()) {
                provisionAPIService.changePassword(operator.email, password);
            }
            operator.password = password.encodeAsMD5();
            return true
        } catch (Exception e) { return false }
    }

    @Transactional
    boolean deleteOperator(Long id) {
        try {
            Operator user = Operator.proxy(id)
            AppEventManager.fire("before-operator-delete", [user.id])
            user.roles = []
            user.save()
            user.delete();
            if(LicenseManager.isProvisionActive()) {
                provisionAPIService.removeEntity(user.uuid)
            }
            return true;
        } catch (Throwable t) {
            throw new ApplicationRuntimeException("operator.delete.failed")
        }
    }

    boolean updateRoles(List<Long> ids, List<Long> userId) {
        Integer count = 0;
        userId.each {
            Operator user = Operator.get(it);
            user.roles.clear()
            ids.each {
                user.addToRoles(Role.proxy(it))
            }
            CacheManager.removeCache(NamedConstants.CACHE.SCOPE_APP, NamedConstants.CACHE.ACCESS_CONTROL)
            count++
        }
        return count;
    }

    public boolean putUserInTrash(Long id) {
        Operator user = Operator.get(id);
        return trashService.putObjectInTrash("operator", user, "exclude")
    }

    public boolean putSelectedUsersInTrash( def ids){
        boolean error = false;
        ids.each { id ->
            if(!putUserInTrash(id)){
                error = true;
            }
        }
        return error;
    }

    public Long countOperatorsInTrash() {
        return Operator.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    public Long countOperatorsInTrash(Map params){
        Closure closure = getCriteriaClosureForTrash(params);
        return Operator.createCriteria().count {
            and closure
        }
    }

    public Map getOperatorsInTrash(int offset, int max, String sort, String dir) {
        String currentSort = (sort == "name") ? "fullName" : sort
        return [Operator: Operator.createCriteria().list(offset: offset, max: max) {
            eq("isInTrash", true)
            order(currentSort ?: "fullName", dir?:"asc")
        }.collect {
            [id: it.id, name: it.fullName, updated: it.updated]
        }]
    }

    public Map getOperatorsInTrash(Map params) {
        String currentSort = (params.sort == "name") ? "fullName" : params.sort
        def listMap = [ offset: params.offset, max: params.max];
        return [Operator: Operator.createCriteria().list(listMap) {
            and getCriteriaClosureForTrash(params)
            order(currentSort, params.dir?:"asc")
        }.collect{
            [id: it.id, name: it.fullName, updated: it.updated]
        }]
    }

    public boolean restoreOperatorFromTrash(Long id) {
        Operator user = Operator.get(id);
        return trashService.restoreObjectFromTrash(user)
    }

    public Long restoreOperatorFromTrash(String field, String value) {
        Operator user = Operator.createCriteria().get {
            eq(field, value)
        }
        user.isInTrash = false;
        user.merge();
        return user.id;
    }

    @Transactional
    public boolean deleteTrashItemAndSaveCurrent(def field, def value) {
        Operator user = Operator.createCriteria().get {
            eq(field, value)
        }
        deleteOperator(user.id);
        return !user.hasErrors();
    }

    @Transactional
    public boolean setIsMatured(boolean check) {
        Operator operator = Operator.get(AppUtil.session.admin)
        operator.isMatured = check
        operator.save()
        return !operator.hasErrors();
    }

    public boolean getIsMatured() {
        Operator operator = Operator.get(AppUtil.session.admin)
        return operator.isMatured
    }

    private Map syncUser(def object) {
        try {
            def map = provisionAPIService.addEntity(object.email, object.uuid)
            if (!map.uuid.equals(object.uuid)) {
                Operator.withSession {
                    object.uuid = map.uuid
                    object.save()
                }
            }
            return map
        } catch (Exception e) {
           throw new RuntimeException(e.getMessage())
        }
    }

    def changeStatus(List<Long> ids, Boolean status) {
        Integer count = 0;
        ids.each {
            Operator user= Operator.get(it)
            user.isActive = status
            count++;
        }
        return count;
    }

    def changeApi(List<Long> ids, Boolean apiAccess) {
        Integer count = 0;
        ids.each {
            Operator user= Operator.get(it)
            user.isAPIAccessOnly = apiAccess
            count++;
        }
        return count;
    }
}