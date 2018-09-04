<div class="import-status-detail">
    <div class="item-import-aborted" style="display: none;">
        <label><g:message code="item.import.aborted"/></label>
    </div>
    <div class="import-status category">
        <label>
            <strong>Category Import : <span class="category-progress-count"></span></strong>
            <g:message code="processed"/> <span class="category-record-complete"></span> <g:message code="out.of"/> <span class="category-record-total"></span>
            <g:message code="success"/> <span class="category-success-count">${task.meta.categorySuccessCount}</span>
            <g:message code="warning"/> <span class="category-warning-count">${task.meta.categoryWarningCount}</span>
            <g:message code="error"/> <span class="category-error-count">${task.meta.categoryErrorCount}</span>
        </label>
        <span class="category-progress"></span>
    </div>
    <div class="import-status product">
        <label>
            <strong>Product Import : <span class="product-progress-count"></span></strong>
            <g:message code="processed"/> <span class="product-record-complete"></span> <g:message code="out.of"/> <span class="product-record-total"></span>
            <g:message code="success"/> <span class="product-success-count">${task.meta.productSuccessCount}</span>
            <g:message code="warning"/> <span class="product-warning-count">${task.meta.productWarningCount}</span>
            <g:message code="error"/> <span class="product-error-count">${task.meta.productErrorCount}</span>
        </label>
        <span class="product-progress"></span>
    </div>
    <div class="import-status total">
        <label>
            <strong>Total Import : <span class="total-progress-count"></span></strong>
            <g:message code="processed"/> <span class="total-record-complete"></span> <g:message code="out.of"/> <span class="total-record-total"></span>
            <g:message code="success"/> <span class="total-success-count">${totalSuccessCount}</span>
            <g:message code="warning"/> <span class="total-warning-count">${totalWarningCount}</span>
            <g:message code="error"/> <span class="total-error-count">${totalErrorCount}</span>
        </label>
        <span class="total-progress"></span>
    </div>
</div>
<div class="log-summary">
    <%-- will load import summary --%>
</div>