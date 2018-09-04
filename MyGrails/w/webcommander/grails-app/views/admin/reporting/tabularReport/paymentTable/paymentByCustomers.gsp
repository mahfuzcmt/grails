<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants" %>
<table class="payment-by-customers-report">
    <colgroup>
        <col class="customer-name-column">
        <col class="customer-email-column">
        <col class="type-column">
        <col class="sex-column">
        <col class="trcount-column">
        <col class="refund-column">
        <col class="paid-column">
    </colgroup>
    <tr>
        <th><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
        <th><g:message code="customer.email"/></th>
        <th><g:message code="customer.type"/></th>
        <th><g:message code="customer.sex"/></th>
        <th><g:message code="transaction.count"/></th>
        <th><g:message code="total.refunds"/></th>
        <th><g:message code="total.payments"/></th>
    </tr>
    <g:if test="${payments.size() == 0}">
        <tr class="table-no-entry-row">
            <td colspan="7"><g:message code="no.records.found"/></td>
        </tr>
    </g:if>
    <g:each in="${payments}" var="payment">
        <tr>
            <td>${payment.name}</td>
            <td>${payment.email}</td>
            <td><g:message code="${payment.type ? 'company' : 'individual'}"/></td>
            <td><g:message code="${payment.sex}"/></td>
            <td>${payment.trcount}</td>
            <td>${payment.refund}</td>
            <td>${payment.paid}</td>
        </tr>
    </g:each>
</table>