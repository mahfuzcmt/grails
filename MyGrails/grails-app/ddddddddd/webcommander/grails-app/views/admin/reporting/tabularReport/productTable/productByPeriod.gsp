<%@ page import="com.webcommander.util.DateUtil" %>
<table class="product-by-period-report">
    <colgroup>
        <col class="year-column">
        <col class="month-column">
        <col class="units-sold-column">
        <col class="gross-sales-column">
    </colgroup>
    <tr>
        <th><g:message code="year"/></th>
        <th><g:message code="month"/></th>
        <th><g:message code="units.sold"/></th>
        <th><g:message code="gross.sales"/></th>
    </tr>
    <g:if test="${products.size() > 0}">
        <g:each in="${products}" var="product">
            <tr>
                <td>${product.year}</td>
                <td>${g.message(code: DateUtil.MONTHS_FULL_LOWER[product.month - 1])}</td>
                <td>${product.sold}</td>
                <td>${product.gross}</td>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr class="table-no-entry-row"> <td colspan="4"><g:message code="no.product.found"/></td></tr>
    </g:else>

</table>