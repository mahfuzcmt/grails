<form class="edit-popup-form" action="${app.relativeBaseUrl()}role/savePermissions" method="post">
    <input type="hidden" name="for" value="${params.for}">
    <input type="hidden" name="type" value="${type}">
    <table class="permission-entry-list">
        <colgroup>
            <col style="width: 62%">
            <col style="width: 38%">
        </colgroup>
        <tr>
            <th><g:message code="permission"/></th>
            <th><input type="checkbox" class="multiple" name="allowAll" ${allowedPermissions.size() == permissions.size() ? 'checked="checked"' : ''}> <g:message code="allow"/></th>
        </tr>
        <g:each var="permission" in="${permissions}">
            <tr>
                <td><g:message code="${permission.name}"/></td>
                <td>
                    <input type="hidden" name="${type}.permissionId" value="${permission.id}">
                    <input type="checkbox" class="multiple allow-group" uncheck-value="off" name="${type}.allow" ${allowedPermissions.contains(permission) ? 'checked="checked"' : ''}>
                </td>
            </tr>
        </g:each>
    </table>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>