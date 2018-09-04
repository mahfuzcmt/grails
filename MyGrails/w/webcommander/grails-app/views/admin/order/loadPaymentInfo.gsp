<%@ page import="com.webcommander.webcommerce.PaymentGateway" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="payment.information"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-tool action-menu">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper collapsed"></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="payment-details">
    <span class="details-title"><g:message code="payment.details"/></span>
    <table>
        <tr>
            <th><g:message code="status"/></th>
            <th><g:message code="order.id"/></th>
            <th><g:message code="order.total"/></th>
            <th><g:message code="amount.due"/></th>
            <th><g:message code="amount.received"/></th>
        </tr>
        <tr>
            <td><g:message code="${order.paymentStatus}"/></td>
            <td>${order.id}</td>
            <td>${order.getGrandTotal().toAdminPrice()}</td>
            <td>${order.getDue().toAdminPrice()}</td>
            <td>${order.getPaid().toAdminPrice()}</td>
        </tr>
    </table>
</div>

<g:if test="${payments}">
    <div class="payment-log">
        <span class="log-title"><g:message code="payment.log"/></span>
        <table class="content log-table">
            <colgroup>
                <col style="width: 10%">
                <col style="width: 10%">
                <col style="width: 15%">
                <col style="width: 19%">
                <col style="width: 20%">
                <col style="width: 10%">
                <col style="width: 10%">
                <col style="width: 6%">
            </colgroup>
            <tr>
                <th><g:message code="payment.date"/></th>
                <th><g:message code="payment.gateway"/></th>
                <th><g:message code="track.info"/></th>
                <th><g:message code="gateway.response"/></th>
                <th><g:message code="payer.info"/></th>
                <th><g:message code="status"/></th>
                <th><g:message code="amount"/></th>
                <th class="actions-column"><g:message code="actions"/></th>
            </tr>
            <g:set var="status" value="${[awaiting: 'positive-minus', refunded: 'negative-plus', failed: 'negative', success: 'positive', pending: 'negative']}"/>
            <g:each in="${payments}" var="payment">
                <tr class="${payment.status}">
                    <td>${payment.payingDate.toAdminFormat(true, false, session.timezone)}</td>
                    <td><g:message code="${PaymentGateway.findByCode(payment.gatewayCode).name}"/></td>
                    <td>${payment.trackInfo.encodeAsBMHTML()}</td>
                    <td>${payment.gatewayResponse}</td>
                    <td>${payment.payerInfo.encodeAsBMHTML()}</td>
                    <td class="status-column"><span class="status ${status[payment.status]}" title="${g.message(code: "${payment.status}")}"></span></td>
                    <td>${payment.amount.toAdminPrice()}</td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed" entity-id="${payment.id}" entity-orderId="${payment.order.id}"></span>
                    </td>
                </tr>
            </g:each>
        </table>
    </div>
</g:if>