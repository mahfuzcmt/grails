<form action="${app.relativeBaseUrl()}user/update" method="post" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="operator.edit.info"/></h3>
            <div class="info-content"><g:message code="section.text.edit.operator"/></div>
        </div>
        <div class="form-section-container">
            <input type="hidden" name="id" value="${user.id}">
            <input type="hidden" name="isChangePassword" value="false"/>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="full.name"/><span class="suggestion">e.g. Peter Smith</span></label>
                    <input type="text" class="medium" value="${user.fullName.encodeAsBMHTML()}" name="fullName" validation="required rangelength[2, 200]" maxlength="200">
                </div><div class="form-row mandatory">
                    <label><g:message code="email"/><span class="suggestion">e.g. speter@abc.com</label>
                    <input type="text" class="medium unique" value="${user.email.encodeAsBMHTML()}" name="email" validation="required email" disabled>
                </div>
            </div>

            <div class="form-row trash-row" style="display: none;">
                <label><g:message code="what.to.do"/></label>
                <span><a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/> <g:message code="or"/></span>
                <span><input type="checkbox" name="deleteTrashItem.email" class="trash-duplicate-delete multiple"> <g:message code="delete.and.save"/></span>
            </div>
            <div class="double-input-row">
                <div class="form-row">
                    <label><g:message code="administrative.status"/><span class="suggestion">e.g. Active</span></label>
                    <g:select name="active" from="${['active', 'inactive'].collect {g.message(code: it)}}" keys="${['true', 'false']}" class="medium" value="${user.isActive}"/>
                </div><div class="form-row">
                    <label><g:message code="api.access.only"/></label>
                    <input type="checkbox" class="single" name="isAPIAccessOnly" uncheck-value="false" ${user.isAPIAccessOnly ? "checked" : ""} ${isLogged ? "disabled" : ""}>
                </div>
            </div>
            <g:if test="${isLogged}">
                <div class="change-password form-row">
                    <label>&nbsp</label>
                    <span class="link"><g:message code="change.password"/></span>
                </div>
            </g:if>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>

<div class="change-password-panel form-row mandatory" style="display: none">
    <label><g:message code="current.password"/></label>
    <input type="password" class="medium" name="oldPassword" validation="required">
</div>
<div class="change-password-panel double-input-row" style="display: none">
    <div class="mandatory form-row">
        <label><g:message code="new.password"/></label>
        <input type="password" class="medium new-password" name="password" validation="required rangelength[5,50]" id="create-user-popup-password">
    </div><div class="form-row">
        <label><g:message code="retype.password"/></label>
        <input type="password" class="medium match-password" validation="compare[create-user-popup-password, string, eq]" message_params="(password above)">
    </div>
</div>