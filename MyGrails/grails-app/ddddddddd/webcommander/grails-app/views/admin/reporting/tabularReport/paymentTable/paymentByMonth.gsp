<%@ page import="com.webcommander.util.DateUtil" %>
<table class="content">
    <colgroup>
        <col class="year-column">
        <col class="month-column">
        <col class="trcount-column">
        <col class="refund-column">
        <col class="paid-column">
    </colgroup>
    <tr>
        <th><g:message code="year"/></th>
        <th><g:message code="month"/></th>
        <th><g:message code="transaction.count"/></th>
        <th><g:message code="total.refunds"/></th>
        <th><g:message code="total.payments"/></th>
    </tr>
    <g:if test="${payments.size() == 0}">
        <tr class="table-no-entry-row">
            <td colspan="5"><g:message code="no.records.found"/></td>
        </tr>
    </g:if>
    <g:each in="${payments}" var="payment">
        <tr>
            <td>${payment.year}</td>
            <td>${DateUtil.MONTHS_FULL_LOWER[payment.month - 1]}</td>
            <td>${payment.trcount}</td>
            <td>${payment.refund}</td>
            <td>${payment.paid}</td>
        </tr>
    </g:each>
</table>
