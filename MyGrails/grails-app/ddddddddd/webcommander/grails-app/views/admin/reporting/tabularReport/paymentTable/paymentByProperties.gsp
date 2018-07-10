<table class="payment-by-properties-report">
    <colgroup>
        <col class="payment-method-column">
        <col class="tr-count-column">
        <col class="refund-column">
        <col class="paid-column">
    </colgroup>
    <tr>
        <th><g:message code="payment.method"/></th>
        <th><g:message code="transaction.count"/></th>
        <th><g:message code="total.refunds"/></th>
        <th><g:message code="total.payments"/></th>
    </tr>
    <g:if test="${payments.size() == 0}">
        <tr class="table-no-entry-row">
            <td colspan="4"><g:message code="no.records.found"/></td>
        </tr>
    </g:if>
    <g:each in="${payments}" var="payment">
        <tr>
            <td><g:message code="${payment.method}"/></td>
            <td>${payment.trcount}</td>
            <td>${payment.refund}</td>
            <td>${payment.paid}</td>
        </tr>
    </g:each>
</table>