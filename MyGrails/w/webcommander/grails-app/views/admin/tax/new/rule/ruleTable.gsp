<table class="content">
    <tr>
        <g:if test="${showSelectCol}"><th class="select-column"></th></g:if>
        <th><g:message code="rule.name"/></th>
        <th><g:message code="rate"/></th>
        <th><g:message code="zone"/></th>
        <g:if test="${!hideActionCol}"><th class="action-column"></th></g:if>
    </tr>
    <g:each in="${ruleList}" var="rule">
        <tr>
            <g:if test="${showSelectCol}"><td class="select-column"><input type="radio" name="ruleRadio" entity-id="${rule.id}"></td></g:if>
            <td>${rule.name}</td>
            <td>${rule.code?.rate ? (rule.code?.rate + ' %' ) : ''}</td>
            <td>${rule.zones?.collect{it.name}?.join(", ")}</td>
            <g:if test="${!hideActionCol}">
                <td class="column actions-column">
                    <span class="action-navigator collapsed" entity-id="${rule.id}" entity-name="${rule.name.encodeAsBMHTML()}"></span>
                </td>
            </g:if>
        </tr>
    </g:each>
</table>