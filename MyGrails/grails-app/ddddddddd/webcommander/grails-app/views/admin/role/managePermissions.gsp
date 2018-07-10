<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<form class="edit-popup-form" action="${app.relativeBaseUrl()}role/savePermissions" method="post">
    <g:set var="ecommerce" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce")}"/>
    <g:each in="${ids}" var="id">
        <input type="hidden" name="id" value="${id}">
    </g:each>
    <input type="hidden" name="for" value="${params.for}">
    <g:if test="${params.for == 'entity'}">
        <input type="hidden" name="entityType" value="${type}">
        <g:set var="type" value="${params.user ?: session.admin}"/>
        <g:select name="type" class="permission-user-list" from="${users}" optionKey="id" optionValue="fullName" value="${type}"/>
    </g:if>
    <g:else>
        <g:select name="type" class="permission-type-list" from="${types.collect {g.message(code: (ecommerce == 'false') ? it.replace("customer","member") : it)}}" keys="${types}" value="${type}"/>
    </g:else>
    <g:if test="${permittedForFirstView}">
        <table class="permission-entry-list">
            <colgroup>
                <col style="width: 50%">
                <col style="width: 25%">
                <col style="width: 25%">
            </colgroup>
            <tr>
                <th><g:message code="permission"/></th>
                <th><input type="checkbox" class="multiple" name="allowAll" ${allowedPermissions.size() == permissions.size() ? 'checked="checked"' : ''}> <g:message code="allow"/></th>
                <th><input type="checkbox" class="multiple" name="denyAll" ${deniedPermissions.size() == permissions.size() ? 'checked="checked"' : ''}> <g:message code="deny"/></th>
            </tr>
            <g:each var="permission" in="${permissions}">
                <tr>
                    <td><g:message code="${permission.label}"/></td>
                    <td>
                        <input type="hidden" name="${type}.permissionId" value="${permission.id}">
                        <input type="checkbox" class="multiple allow-group" uncheck-value="off" name="${type}.allow" ${allowedPermissions.contains(permission) ? 'checked="checked"' : ''}>
                    </td>
                    <td>
                        <input type="checkbox" class="multiple deny-group" uncheck-value="off" name="${type}.deny" ${deniedPermissions.contains(permission) ? 'checked="checked"' : ''}>
                    </td>
                </tr>
            </g:each>
        </table>
    </g:if>
    <g:else>
        <div class="permission-entry-list">
            <div class="forbidden-action-message">
                <g:message code="not.allowed.to.do" args="${[g.message(code: 'change.permission')]}"/>
            </div>
        </div>
    </g:else>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>