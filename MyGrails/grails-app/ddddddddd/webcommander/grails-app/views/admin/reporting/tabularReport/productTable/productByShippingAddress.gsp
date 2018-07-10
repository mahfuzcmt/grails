<table class="product-by-shipping-address-report">
    <colgroup>
        <col class="country-column">
        <col class="region-column">
        <col class="city-column">
        <col class="units-sold-column">
        <col class="gross-sales-column">
    </colgroup>
    <tr>
        <th><g:message code="shipping.country"/></th>
        <th><g:message code="shipping.region"/></th>
        <th><g:message code="shipping.city"/></th>
        <th><g:message code="units.sold"/></th>
        <th><g:message code="gross.sales"/></th>
    </tr>
    <g:if test="${products.size() > 0}">
        <g:each in="${products}" var="product">
        <tr>
            <td>${product.country}</td>
            <td>${product.region}</td>
            <td>${product.city}</td>
            <td>${product.sold}</td>
            <td>${product.gross}</td>
        </tr>
    </g:each>
    </g:if>
    <g:else>
        <tr class="table-no-entry-row"> <td colspan="5"><g:message code="no.product.found"/></td></tr>
    </g:else>

</table>
