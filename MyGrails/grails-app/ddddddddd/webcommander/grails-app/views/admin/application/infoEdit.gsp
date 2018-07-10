<form action="${app.relativeBaseUrl()}application/save" method="post" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="application.info"/></h3>
            <div class="info-content"><g:message code="section.text.application"/></div>
        </div>
        <div class="form-section-container">
            <input type="hidden" name="id" value="${client.id}">
            <div class="form-row">
                <label><g:message code="enabled"/></label>
                <input type="checkbox" name="enabled" class="single" ${client.enabled ? "checked" : ""}>
            </div>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="name"/></label>
                    <input type="text" name="name" value="${client.name}" maxlength="250" validation="required maxlength[250]">
                </div><div class="form-row mandatory">
                    <label><g:message code="display.name"/></label>
                    <input type="text" name="displayName" value="${client.displayName}" maxlength="250" validation="required maxlength[250]">
                </div>
            </div>
            <g:if test="${client.id}">
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="client.id"/></label>
                        <input type="text" value="${client.clientId}" readonly>
                    </div><div class="form-row">
                        <label><g:message code="client.secret"/></label>
                        <input type="text" value="${client.clientSecret}" readonly>
                    </div>
                </div>
            </g:if>
            <div class="form-row mandatory">
                <label><g:message code="redirect.uri"/></label>
                <input type="text" name="redirectUrl" value="${client.redirectUrl}" validation="required url">
            </div>
            <div class="form-row">
                <label><g:message code="description"/></label>
                <textarea name="description">${client.description}</textarea>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="save"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>