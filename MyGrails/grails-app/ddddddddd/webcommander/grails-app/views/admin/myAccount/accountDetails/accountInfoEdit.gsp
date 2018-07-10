<form action="${app.relativeBaseUrl()}myAccount/saveInfo" method="post" class="create-edit-form">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="my.account"/></h3>
            <div class="info-content"><g:message code="section.text.my.account.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="form-row">
                <label><g:message code="display.name"/></label>
                <input name="displayName" type="text" value="${accountDetails.displayName}" validation="required">
            </div>
            <div class="form-row">
                <label><g:message code="mobile"/></label>
                <input name="mobile" type="text" value="${accountDetails.mobile}" validation="phone">
            </div>
            <div class="form-row">
                <label><g:message code="phone"/></label>
                <input name="phone" type="text" value="${accountDetails.phone}" validation="phone">
            </div>
            <div class="form-row">
                <label><g:message code="email"/></label>
                <input name="emailAddress" type="text" value="${accountDetails.emailAddress}" validation="required email">
            </div>
            <div class="form-row">
                <label><g:message code="address"/></label>
                <input name="addressLine1" type="text" value="${accountDetails.addressLine1}" validation="required">
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="update"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>