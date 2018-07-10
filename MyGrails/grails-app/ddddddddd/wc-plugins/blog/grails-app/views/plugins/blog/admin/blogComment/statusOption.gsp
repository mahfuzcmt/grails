<form class="edit-popup-form create-edit-form" action="${app.relativeBaseUrl()}blogAdmin/changeCommentStatus" method="post">
<div class="form-section">
    <div class="form-row">
            <label><g:message code="status"/></label>
            <g:select name="status"
                      from="${[g.message(code: "select.option"), g.message(code: "approve"), g.message(code: "pending"), g.message(code: "reject")]}"
                      keys="${["", "approved", "pending", "rejected"]}" value="" validation="required"/>
    </div>

    <div class="form-row">
        <button type="submit" class="submit-button"><g:message code="${"save"}"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>
</form>