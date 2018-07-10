<div class="table-view">
    <div class="header">
        <span class="item-group entity-count title">
            <g:message code="counter"/> (<span class="count">${count}</span>)
        </span>
    </div>
    <table class="content">
        <colgroup>
            <col style="width: 40%">
            <col style="width: 40%">
            <col style="width: 20%">
        </colgroup>
        <tr>
            <th><g:message code="first.name"/></th>
            <th><g:message code="last.name"/></th>
            <th><g:message code="links"/></th>
        </tr>
        <g:if test="${usageList}">
            <g:each in="${usageList}" var="usage">
                <tr>
                    <td>${usage?.customer?.firstName.encodeAsBMHTML()}</td>
                    <td>${usage?.customer?.lastName.encodeAsBMHTML()}</td>
                    <td><span class="re-icon-link customer-link" data-customerId="${usage.customerId}" data-customerName="${usage?.customer?.firstName.encodeAsBMHTML()}"></span></td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="2"><g:message code="no.records.found"/></td>
            </tr>
        </g:else>
    </table>
    <div class="footer">
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>