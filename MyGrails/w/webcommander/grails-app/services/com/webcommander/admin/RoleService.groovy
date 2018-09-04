package com.webcommander.admin

import com.webcommander.acl.*
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CacheManager
import com.webcommander.manager.HookManager
import com.webcommander.models.RestrictionPolicy
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.hibernate.SessionFactory

@Initializable
@Transactional
class RoleService {
    CommonService commonService
    SessionFactory sessionFactory

    private static final List<String> RESTRICTED_NAMES = ["Admin", "Moderator", "Basic Operator"]

    private static RoleService instance = null

    static void initialize() {
        HookManager.register("role-delete-at2-count", { response, id ->
            Integer count = Operator.createCriteria().count{
                roles {
                    eq("id", id)
                }
            }
            if (count) {
                response."operators" = count
            }
            return response
        })

        HookManager.register("role-delete-at2-list", { response, id ->
            List<Operator> users = Operator.createCriteria().list {
                roles {
                    eq("id", id)
                }
            }
            if (users.size() > 0) {
                response."operators" = users.collect { it.fullName }
            }
            return response
        })

        AppEventManager.on("before-role-delete", { id ->
            Operator.where {
                roles {
                    eq "id", id
                }
            }.list().each { Operator user ->
                user.roles.removeAll {it.id == id}
                user.merge(flush: true)
            }
            RolePermission.where {
                role == Role.proxy(id)
            }.deleteAll()
            CacheManager.removeCache(NamedConstants.CACHE.SCOPE_APP, NamedConstants.CACHE.ACCESS_CONTROL)
        })

        AppEventManager.on("before-operator-delete", { id ->
            EntityPermission.where {
                user == Operator.proxy(id)
            }.deleteAll()
        })
        AppEventManager.on("before-page-delete", { id ->
            EntityPermission.deleteEntity("page", id)
        })
        AppEventManager.on("before-product-delete", { id ->
            EntityPermission.deleteEntity("product", id)
        })
        AppEventManager.on("before-category-delete", { id ->
            EntityPermission.deleteEntity("category", id)
        })
        AppEventManager.on("before-article-delete", { id ->
            EntityPermission.deleteEntity("article", id)
        })
        AppEventManager.on("before-section-delete", { id ->
            EntityPermission.deleteEntity("section", id)
        })
    }

    static getInstance() {
        if (!instance) {
            instance = Holders.applicationContext.getBean("roleService")
        }
        return instance
    }

    Boolean save(Map params) {
        Long id = params.id.toLong(0);
        Role role = id ? Role.get(id) : new Role()
        checkRoleNameForConflict(params.name, id)
        role.name = params.name
        role.description = params.description
        if (params.id) {
            if (params.name in RESTRICTED_NAMES) {
                throw new ApplicationRuntimeException("edit.info.role.restricted")
            }
            role.merge();
        } else {
            role.save()
        }
        return !role.hasErrors()
    }

    Boolean updatePermission(Map params) {
        if(params.for == "operator") {
            params.for = "user"
        }
        Class permDomain = Class.forName("com.webcommander.acl.${params.for.capitalize()}Permission")

        List<Long> ids = params.list("id")*.toLong();

        ids.each { userId ->
            params.list("type").each { type ->
                List allowList = params.list(type + ".allow").collect{ it.toBoolean() }
                List denyList = params.list(type + ".deny").collect{ it.toBoolean() }
                List permissionIdList = params.list(type + ".permissionId").collect{ it.toLong() }
                //Long id = params.id.toLong(0)

                switch(params.for) {
                    case "role":
                    case "user":
                        Class domain = params.for == "user" ? Operator : Role
                        if(permissionIdList) {
                            permDomain.createCriteria().list {
                                eq(params.for + ".id", userId)
                                inList("permission.id", permissionIdList)
                            }*.delete()
                        }
                        for(int h = 0; h < permissionIdList.size(); h++) {
                            if(allowList[h] == true) {
                                def newEntry = permDomain.newInstance(isAllowed: true, permission: Permission.proxy( permissionIdList[h]), "${params.for}": domain.proxy( userId ))
                                newEntry.save()
                            }
                            if(denyList[h] == true) {
                                def newEntry = permDomain.newInstance(isAllowed: false, permission: Permission.proxy( permissionIdList[h]), "${params.for}": domain.proxy( userId ))
                                newEntry.save()
                            }
                        }
                        break;
                    case "owner":
                        if(permissionIdList) {
                            OwnerPermission.createCriteria().list {
                                inList("permission.id", permissionIdList)
                            }*.delete()
                        }
                        for(int h = 0; h < permissionIdList.size(); h++) {
                            if(allowList[h] == true) {
                                new OwnerPermission(permission: Permission.proxy(permissionIdList[h])).save()
                            }
                        }
                        break;
                    case "entity":
                        if(permissionIdList) {
                            EntityPermission.createCriteria().list {
                                eq("user.id", type.toLong(0))
                                inList("permission.id", permissionIdList)
                            }*.delete()
                        }
                        for(int h = 0; h < permissionIdList.size(); h++) {
                            if(allowList[h] == true) {
                                new EntityPermission(entityId: userId, isAllowed: true, user: type, permission: Permission.proxy( permissionIdList[h])).save()
                            }
                            if(denyList[h] == true) {
                                new EntityPermission(entityId: userId, isAllowed: false, user: type, permission: Permission.proxy( permissionIdList[h])).save()
                            }
                        }
                }
            }
        }

        CacheManager.removeCache(NamedConstants.CACHE.SCOPE_APP, NamedConstants.CACHE.ACCESS_CONTROL)
        return true
    }

    private Closure getCriteriaClosure(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
        }
        return closure;
    }

    void checkRoleNameForConflict(String name, Long id = 0) {
        Integer count = Role.createCriteria().count {
            if (id) {
                ne("id", id)
            }
            eq("name", name)
        }
        if (count > 0) {
            throw new ApplicationRuntimeException("role.name.exists.pick.different");
        }
    }

    Integer getRolesCount(Map params) {
        Closure closure = getCriteriaClosure(params);
        return Role.createCriteria().get {
            and closure;
            projections {
                rowCount();
            }
        }
    }

    List<Role> getRoles(Map params) {
        Closure closure = getCriteriaClosure(params);
        def listMap = [max: params.max, offset: params.offset];
        return Role.createCriteria().list(listMap) {
            and closure
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    boolean isOperatorAssociatedWithRole(Role role) {
        if (role.users && role.users.size() > 0) {
            return true
        }
        return false
    }

    boolean deleteRole(Long id, String at1, String at2) {
        Role role = Role.get(id);
        if(isOperatorAssociatedWithRole(role)) {
            return false
        } else {
            TrashUtil.preProcessFinalDelete("role", id, at2 != null, at1 != null);
            AppEventManager.fire("before-role-delete", [id]);
            role.delete();
            AppEventManager.fire("role-delete", [id])
            return true;
        }
    }


    Integer deleteSelected (def ids) {
        Integer removeCount = 0;
        ids.each { id ->
            try {
                if(deleteRole(id, "yes", "include")) {
                    removeCount++;
                }
            } catch(Throwable ignored) {}
        }
        return removeCount
    }

    boolean updateUsers(List<Long> userIds, Long roleId) {
        List<Operator> users = Operator.createCriteria().list {
            roles {
                eq "id", roleId
            }
        }
        users.each { user ->
            def roles = user.roles
            def itera = roles.iterator()
            Role role;
            while(itera.hasNext()) {
                role = itera.next()
                if(role.id == roleId) {
                    itera.remove()
                    break;
                }
            }
        }
        Role role = Role.proxy(roleId);
        userIds.each {
            Operator user = Operator.proxy(it);
            user.addToRoles(role)
        }
        sessionFactory.cache.evictCollectionRegions()
        CacheManager.removeCache(NamedConstants.CACHE.SCOPE_APP, NamedConstants.CACHE.ACCESS_CONTROL)
        return true;
    }

    private Map cacheUserPermissions(Operator user) {
        def roles = user.roles;
        def permissions = [:]
        def rolePermissions = [];
        if(roles.size()) {
            rolePermissions = RolePermission.createCriteria().list {
                role {
                    inList("id", roles.id)
                }
            }
        }
        rolePermissions.addAll(UserPermission.createCriteria().list {
            eq("user", user)
        })
        rolePermissions.each { permission ->
            Permission _permission = permission.permission
            def key = _permission.type + "." + _permission.name;
            if(permissions.containsKey(key)) {
                permissions[key].general = permissions[key].general && permission.isAllowed
            } else {
                permissions[key] = [general: permission.isAllowed]
            }
        }
        OwnerPermission.createCriteria().list {
            projections {
                property("permission")
            }
        }.each { permission ->
            String key = permission.type + "." + permission.name
            if(!permissions[key]) {
                permissions[key] = [:]
            }
            permissions[key].owner = true
        }
        EntityPermission.createCriteria().list {
            eq("user", user)
        }.each { permission ->
            Permission _permission = permission.permission
            String key = _permission.type + "." + _permission.name
            if(!permissions[key]) {
                permissions[key] = [allowed_entity: [], denied_entity: []]
            }
            if(permissions[key].allowed_entity == null) {
                permissions[key].allowed_entity = []
                permissions[key].denied_entity = []
            }
            (permission.isAllowed ? permissions[key].allowed_entity : permissions[key].denied_entity).add(permission.entityId)
        }

        Map adminPermission = permissions['administration.edit.permission']
        if(adminPermission && !adminPermission.general) {
            permissions.findAll {it.key.endsWith(".edit.permission")}.each {
                it.value['general'] = false
            }
        }

        CacheManager.cache(NamedConstants.CACHE.SCOPE_APP, permissions, NamedConstants.CACHE.ACCESS_CONTROL, user.id + "")
        return permissions
    }

    Boolean isPermitted(Long operatorId, RestrictionPolicy policy, Map params) {
        Operator operator = Operator.findByIdAndIsInTrash(operatorId, false);
        if(operator == null) return false;
        if(operator.email == "implementer@webcommander") return true

        def permissions = getPermissions(operator)
        String key = policy.type + "." + policy.permission
        Map permission = permissions[key] ?: [:];
        boolean allowed = permission.general;
        def policyEntityParam = params[policy.entityParam]
        policyEntityParam = policyEntityParam == null ? [] : (policyEntityParam.class.isArray() ? policyEntityParam.collect {it.toLong(0)} : [policyEntityParam.toLong(0)])
        // permission.general can be false
        if(policyEntityParam?.size() && (permission.general == null || allowed)) {
            if(permission.allowed_entity && permission.allowed_entity.intersect(policyEntityParam).size() == policyEntityParam.size()) {
                allowed = true
            }
            if(permission.denied_entity && permission.denied_entity.intersect(policyEntityParam).size() != 0) {
                allowed = false
            }
        }
        if(!allowed) {
            if(permission.owner && policy.ownerField) {
                boolean isOwner = policy.domain.createCriteria().count {
                    policyEntityParam.count {eq("id", it)} > 0
                    eq(policy.ownerField + ".id", operatorId)
                } > 0;
                if(isOwner) {
                    allowed = true
                }
            }
        }
        return allowed;
    }

    def getPermissions(Long operatorId) {
        getPermissions(Operator.proxy(operatorId))
    }

    def getPermissions(Operator operator) {
        def permissions = CacheManager.get(NamedConstants.CACHE.SCOPE_APP, NamedConstants.CACHE.ACCESS_CONTROL, operator.id + "")
        if(permissions == null) {
            permissions = cacheUserPermissions(operator)
        }
        return permissions
    }
}