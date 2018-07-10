<form class="edit-popup-form create-edit-form" action="${app.relativeBaseUrl()}user/changeApi" method="post">
<div class="form-section">
    <div class="form-row">
        <label><g:message code="api.access.only"/></label>
        <input type="checkbox" class="single" name="isAPIAccessOnly" uncheck-value="false">
    </div>

    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${"save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
</form>