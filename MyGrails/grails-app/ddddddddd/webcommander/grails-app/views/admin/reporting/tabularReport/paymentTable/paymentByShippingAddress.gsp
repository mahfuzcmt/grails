<table class="content">
    <colgroup>
        <col class="country-column">
        <col class="state-column">
        <col class="city-column">
        <col class="trcount-column">
        <col class="refund-column">
        <col class="paid-column">
    </colgroup>
    <tr>
        <th><g:message code="shipping.country"/></th>
        <th><g:message code="shipping.region"/></th>
        <th><g:message code="shipping.city"/></th>
        <th><g:message code="transaction.count"/></th>
        <th><g:message code="total.refunds"/></th>
        <th><g:message code="total.payments"/></th>
    </tr>
    <g:if test="${payments.size() == 0}">
        <tr class="table-no-entry-row">
            <td colspan="6"><g:message code="no.records.found"/></td>
        </tr>
    </g:if>
    <g:each in="${payments}" var="payment">
        <tr>
            <td>${payment.country}</td>
            <td>${payment.region}</td>
            <td>${payment.city}</td>
            <td>${payment.trcount}</td>
            <td>${payment.refund}</td>
            <td>${payment.paid}</td>
        </tr>
    </g:each>
</table>
