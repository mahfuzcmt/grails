<span class="title"><g:message code="change.password"/></span>
<g:form class="edit-password-form" controller="customer" action="updatePassword">
    <div class="form-row mandatory">
        <label><g:message code="current.password"/>:</label>
        <input type="password" name="currentPassword" validation="required"/>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="new.password"/>:</label>
        <input type="password" name="newPassword" id="newPassword" class="password-strength-meter"  validation="required rangelength[6,50]"/>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="retype.password"/>:</label>
        <input type="password" name="retypePassword" validation="required compare[newPassword, string, eq]" message_params="(password above)"/>
    </div>
    <div class="form-row btn-row">
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
        <button type="submit" class="submit-button"><g:message code="update"/></button>
    </div>
</g:form>