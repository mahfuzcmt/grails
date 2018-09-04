<form class="edit-popup-form" action="${app.relativeBaseUrl()}pageAdmin/saveEntityPermissions" method="post">
    <input type="hidden" name="id" value="${params.id}">
    <g:select name="user" class="permission-user-list" from="${users.collect {it.fullName}}"/>
    <table class="permission-entry-list">
        <colgroup>
            <col style="width: 50%">
            <col style="width: 25%">
            <col style="width: 25%">
        </colgroup>
        <tr>
            <th><g:message code="permission"/></th>
            <th><g:message code="allow"/></th>
            <th><g:message code="deny"/></th>
        </tr>
        <g:each var="permission" in="${permissions}">
            <tr>
                <td><g:message code="${permission.label}"/></td>
                <td>
                    <input type="hidden" name="permissionId" value="${permission.id}">
                    <input type="checkbox" class="single" uncheck-value="off" name="allow" ${allowedPermissions.contains(permission) ? 'checked="checked"' : ''}>
                </td>
                <td>
                    <input type="checkbox" class="single" uncheck-value="off" name="deny" ${deniedPermissions.contains(permission) ? 'checked="checked"' : ''}>
                </td>
            </tr>
        </g:each>
    </table>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>