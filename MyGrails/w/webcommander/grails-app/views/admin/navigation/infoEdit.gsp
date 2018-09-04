<form class="edit-popup-form create-edit-form" action="${app.relativeBaseUrl()}navigation/save" method="post">
    <input name="id" type="hidden" value="${navigation?.id}">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="navigation.info"/></h3>
            <div class="info-content"><g:message code="section.text.navigation.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="double-input-row">
                <div class="mandatory form-row">
                    <label><g:message code="name"/><span class="suggestion"><g:message code="suggestion.navigation.name"/></span></label>
                    <input  name="name" type="text" ${navigation.name == "Main Menu" ? "readonly" : ""} class="medium unique" validation="required maxlength[100]" value="${navigation?.name.encodeAsBMHTML()}" maxlength="100">
                </div><div class="form-row">
                    <label><g:message code="restricted.item"/></label>
                    <g:select name="restrictedItem" from="${[g.message(code: "show"), g.message(code:  "hide")]}" keys="${["show", "hide"]}" value="${navigation.hideRestrictedItem ? "hide" : "show"}"/>
                </div>
            </div>

            <div class="form-row trash-row" style="display: none;">
                <label><g:message code="what.to.do"/></label>
                <span><a onclick="return false" class="trash-duplicate-restore fake-link"><g:message code="restore"/></a> <g:message code="restore.and.close.window"/> <g:message code="or"/></span>
                <span><input type="checkbox" name="deleteTrashItem.name" class="trash-duplicate-delete multiple"> <g:message code="delete.and.save"/></span>
            </div>
            <div class="form-row">
                <button type="submit" class="submit-button"><g:message code="${navigation.id ? "update" : "save"}"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </div>
    </div>
</form>