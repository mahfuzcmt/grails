<g:form class="edit-popup-form" controller="giftCardAdmin" action="adjustAmount" method="post">
    <input type="hidden" name="id" value="${params.id}">
    <div class="form-row">
        <g:select name="type" from="${[g.message(code: "add"), g.message(code: "deduct")]}" keys="${['add', 'deduct']}"/>
    </div>
    <div class="form-row">
        <label><g:message code="amount" /></label>
        <input type="text" name="amount" validation="price" restrict="decimal" maxlength="9">
    </div>
    <div class="form-row">
        <label><g:message code="note"/></label>
        <textarea validation="maxlength[200]" maxlength="200"></textarea>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</g:form>