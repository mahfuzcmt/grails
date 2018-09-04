<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<table class="order-by-status-report">
    <colgroup>
        <col class="customer-name-column">
        <col class="customer-email-column">
        <col class="customer-type-column">
        <col class="customer-sex-column">
        <col class="total-sales-column">
        <col class="total-discount-column">
        <col class="total-shipping-column">
        <col class="total-tax-column">
        <col class="order-count-column">
    </colgroup>
    <tr>
        <th><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
        <th><g:message code="customer.email"/></th>
        <th><g:message code="customer.type"/></th>
        <th><g:message code="customer.sex"/></th>
        <th><g:message code="total.sales"/></th>
        <th><g:message code="total.discounts"/></th>
        <th><g:message code="total.shipping"/></th>
        <th><g:message code="total.taxes"/></th>
        <th><g:message code="order.count"/></th>
    </tr>
    <g:if test="${orders.size() > 0}">
        <g:each in="${orders}" var="order">
            <tr>
                <td>${order.cname.encodeAsBMHTML()}</td>
                <td>${order.cemail}</td>
                <td>${g.message(code: order.ctype)}</td>
                <td>${g.message(code: order.csex)}</td>
                <td>${order.sale}</td>
                <td>${order.discount}</td>
                <td>${order.shipping}</td>
                <td>${order.tax}</td>
                <td>${order.count}</td>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr class="table-no-entry-row"> <td colspan="9"><g:message code="no.order.found"/></td></tr>
    </g:else>
</table>