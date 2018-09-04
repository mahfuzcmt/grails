<table class="product-by-status-report">
    <colgroup>
        <col class="product-type-column">
        <col class="category-column">
        <col class="name-column">
        <col class="manufacturer-column">
        <col class="brand-column">
        <col class="price-column">
        <col class="units-sold-column">
        <col class="gross-sales-column">
    </colgroup>
    <tr>
        <th><g:message code="type"/></th>
        <th><g:message code="category"/></th>
        <th><g:message code="name"/></th>
        %{--<th><g:message code="manufacturer"/></th>
        <th><g:message code="brand"/></th>--}%
        <th><g:message code="price"/></th>
        <th><g:message code="units.sold"/></th>
        <th><g:message code="gross.sales"/></th>
    </tr>
    <g:if test="${products.size() > 0}">
        <g:each in="${products}" var="product">
            <tr>
                <td>${g.message(code: product.type)}</td>
                <td>${product.category.encodeAsBMHTML()}</td>
                <td>${product.name.encodeAsBMHTML()}</td>
                %{--<td>${product.manufacturer.encodeAsBMHTML()}</td>
                <td>${product.brand.encodeAsBMHTML()}</td>--}%
                <td>${product.price}</td>
                <td>${product.sold}</td>
                <td>${product.gross}</td>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr class="table-no-entry-row"> <td colspan="8"><g:message code="no.product.found"/></td></tr>
    </g:else>

</table>