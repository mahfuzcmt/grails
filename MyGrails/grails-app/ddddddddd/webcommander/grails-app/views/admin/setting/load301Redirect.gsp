<div class="redirects-301-container">
    <table class="url-mappings">
        <colgroup>
            <col>
            <col>
            <col style="width: 80px">
        </colgroup>
        <thead>
        <tr>
            <th><g:message code="old.url"/></th>
            <th><g:message code="new.url"/></th>
            <th><g:message code="action"/> </th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${mappings}" var="mapping">
            <tr mapping-id="${mapping.id}">
                <td class="editable old-url">${mapping.oldUrl}</td>
                <td class="editable new-url">${mapping.newUrl}</td>
                <td><span class="tool-icon remove"></span></td>
            </tr>
        </g:each>
        <tr class="last-row multi-conditions">
            <td><div class="form-row without-label mandatory"><input type="text" class="old-url td-full-width" validation="required partial_url" placeholder="<g:message code="old.url"/>"></div></td>
            <td><div class="form-row without-label mandatory"><input type="text" class="new-url td-full-width" validation="required url" placeholder="<g:message code="new.url"/>"></div></td>
            <td><span class="tool-icon add add-row"></span></td>
        </tr>
        </tbody>
    </table>
</div>