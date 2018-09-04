<table class="order-by-status-report">
    <colgroup>
        <col class="order-status-column">
        <col class="total-sales-column">
        <col class="total-discount-column">
        <col class="total-shipping-column">
        <col class="total-tax-column">
        <col class="order-count-column">
    </colgroup>
    <tr>
        <th><g:message code="status"/></th>
        <th><g:message code="total.sales"/></th>
        <th><g:message code="total.discounts"/></th>
        <th><g:message code="total.shipping"/></th>
        <th><g:message code="total.taxes"/></th>
        <th><g:message code="order.count"/></th>
    </tr>
    <g:if test="${orders.size() > 0}">
        <g:each in="${orders}" var="order">
            <tr>
                <td>${g.message(code: order.status)}</td>
                <td>${order.sale}</td>
                <td>${order.discount}</td>
                <td>${order.shipping}</td>
                <td>${order.tax}</td>
                <td>${order.count}</td>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr class="table-no-entry-row"> <td colspan="6"><g:message code="no.order.found"/></td></tr>
    </g:else>

</table>
