<form action="${app.relativeBaseUrl()}productReviewAdmin/loadAppView" method="post" class="edit-popup-form create-edit-form">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="product.name"/></label>
            <input type="text" name="searchText" class="large"/>
        </div><div class="form-row">
            <label><g:message code="ask.by"/></label>
            <input type="text" name="askBy" class="large"/>
        </div>
    </div>
    <div class="form-row ask-question-status">
        <label><g:message code="status"/></label>
        <g:select name="status" from="${["": g.message(code: "none"), "pending": g.message(code: "reply.pending"), "replied": g.message(code: "replied")]}" optionKey="key" optionValue="value" class="large"/>
    </div>
    <div class="form-row datefield-between">
        <label><g:message code="question.date.between"/></label>
        <input type="text" class="datefield-from smaller" name="dateFrom"><span class="date-field-separator">-</span><input type="text" class="datefield-to smaller" name="dateTo"/>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button"><g:message code="search"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</form>