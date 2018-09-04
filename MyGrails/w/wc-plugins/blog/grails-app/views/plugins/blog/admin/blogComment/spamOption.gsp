<form class="edit-popup-form create-edit-form" action="${app.relativeBaseUrl()}blogAdmin/changeSpam" method="post">
<div class="form-section">
    <div class="form-row">
            <label><g:message code="status"/></label>
            <g:select name="spam"
                      from="${[g.message(code: "select.option"), g.message(code: "mark.as.spam"), g.message(code: "unmark.as.spam")]}"
                      keys="${["", "true", "false"]}" value="" validation="required"/>
    </div>

    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${"save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
</form>