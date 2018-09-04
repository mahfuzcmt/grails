<table class="tax-by-month-report">
    <colgroup>
        <col class="year-column">
        <col class="month-column">
        <col class="total-amount-column">
    </colgroup>
    <tr>
        <th><g:message code="year"/></th>
        <th><g:message code="month"/></th>
        <th><g:message code="total.taxes"/></th>
    </tr>
    <g:if test="${taxList.size() == 0}">
        <tr class="table-no-entry-row">
            <td colspan="3"><g:message code="no.records.found"/></td>
        </tr>
    </g:if>
    <g:each in="${taxList}" var="tax">
        <tr>
            <td>${tax.year}</td>
            <td>${tax.month}</td>
            <td>${tax.total}</td>
        </tr>
    </g:each>
</table>