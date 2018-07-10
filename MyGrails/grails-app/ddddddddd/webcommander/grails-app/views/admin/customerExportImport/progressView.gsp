<div class="import-status-detail">
    <div class="customer-import-aborted" style="display: none;">
        <label><g:message code="item.import.aborted"/></label>
    </div>
    <div class="import-status total">
        <label>
            <strong>Total Import : <span class="progress-count"></span></strong>
            <g:message code="processed"/> <span class="record-complete">${complete}</span>
            <g:message code="out.of"/> <span class="record-total">${total}.</span>
            <g:message code="success"/> <span class="success-count">${totalSuccessCount}</span>
            <g:message code="warning"/> <span class="warning-count">${totalWarningCount}</span>
            <g:message code="error"/> <span class="error-count">${totalErrorCount}</span>
        </label>
        <span class="progress"></span>
    </div>
</div>
<div class="log-summary">
    <%-- will load import summary --%>
</div>