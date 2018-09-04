<div class="body create-edit-form">
    <div class="double-input-row">
        <div class="form-row chosen-wrapper">
            <label><g:message code="filter"/></label>
            <g:select class="tool-group filter-option smaller first-operand" name="filterKey" from="${["": g.message(code: "no.filter")] + tableFiltersList.collectEntries({[(it): g.message(code: it)]})}"
                      optionKey="${{it.key}}" optionValue="${{it.value}}" value="${params.filterKey}"/>
        </div><div class="form-row">
            <label><g:message code="equal"/></label>
            <input class="second-operand" type="text" name="filterValue" value="${params.filterValue}">
        </div>
    </div>
    <div class="button-line">
        <button class="submit-button" type="button"><g:message code="ok"/></button>
    </div>
</div>