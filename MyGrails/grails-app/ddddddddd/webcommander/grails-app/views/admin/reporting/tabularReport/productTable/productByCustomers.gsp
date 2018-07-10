<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<table class="product-by-status-report">
    <colgroup>
        <col class="customer-name-column">
        <col class="customer-email-column">
        <col class="customer-type-column">
        <col class="customer-sex-column">
        <col class="units-sold-column">
        <col class="gross-sales-column">
    </colgroup>
    <tr>
        <th><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
        <th><g:message code="customer.email"/></th>
        <th><g:message code="customer.type"/></th>
        <th><g:message code="customer.sex"/></th>
        <th><g:message code="units.sold"/></th>
        <th><g:message code="gross.sales"/></th>
    </tr>
    <g:if test="${products.size() > 0}">
        <g:each in="${products}" var="product">
            <tr>
                <td>${product.cname.encodeAsBMHTML()}</td>
                <td>${product.cemail}</td>
                <td>${g.message(code: product.ctype)}</td>
                <td>${g.message(code: product.csex)}</td>
                <td>${product.sold}</td>
                <td>${product.gross}</td>
            </tr>
        </g:each>
    </g:if>
    <g:else>
        <tr class="table-no-entry-row"> <td colspan="6"><g:message code="no.product.found"/></td></tr>
    </g:else>

</table>