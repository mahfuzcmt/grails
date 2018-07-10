<form class="edit-popup-form create-edit-form" action="${app.relativeBaseUrl()}navigation/saveRestrictedItemOption" method="post">
<div class="form-section">
    <div class="form-row">
            <label><g:message code="restricted.item"/></label>
            <g:select name="restrictedItem"
                      from="${[g.message(code: "select.option"), g.message(code: "show"), g.message(code: "hide")]}"
                      keys="${["", "show", "hide"]}" value="" validation="required"/>
    </div>

    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${"save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
</form>