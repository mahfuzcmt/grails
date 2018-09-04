<%@ page import="com.webcommander.plugin.myob.MyobService" %>
<div class="myob-status-detail">
    <div class="operation-aborted" style="display: none;">
        <label><g:message code="item.${task.meta.operation}.aborted"/></label>
    </div>
    <g:each in="${task.meta.taskItems}" var="item">
        <g:if test="${((task.meta.operation == "import") && (item != "order")) || ((task.meta.operation == "export") && (item != "tax")) }">
            <div class="myob-status ${item}">
                <label>
                    <strong><g:message code="item.operation" args="${[g.message(code: item), g.message(code: task.meta.operation)]}"/> : <span class="${item}-progress-count"></span> </strong>
                    <g:message code="processed"/> <span class="${item}-record-complete"></span> <g:message code="out.of"/> <span class="${item}-record-total"></span>
                    <g:message code="success"/> <span class="${item}-success-count">${task.meta."${item}SuccessCount"}</span>
                    <g:message code="warning"/> <span class="${item}-warning-count">${task.meta."${item}WarningCount"}</span>
                    <g:message code="error"/> <span class="${item}-error-count">${task.meta."${item}ErrorCount"}</span>
                </label>
                <span class="${item}-progress"></span>
            </div>
        </g:if>
    </g:each>
    <div class="myob-status total">
        <label>
            <strong><g:message code="total.operation" args="${[g.message(code: task.meta.operation)]}"/> : <span class="total-progress-count"></span></strong>
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