<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;" %>
<g:set var="currencySymbol" value="${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}"/>
<div id="order-list">
    <g:if test="${orders.size()}">
        <table>
            <colgroup>
                <col class="id-col">
                <col class="payment-status-col">
                <col class="shipping-status-col">
                <col class="order-date-col">
                <col class="total-amount-col">
                <col class="action-col">
            </colgroup>
            <thead>
            <tr>
                <th><g:message code="order.id"/></th>
                <th><g:message code="payment.status"/></th>
                <th><g:message code="shipping.status"/></th>
                <th><g:message code="order.date"/></th>
                <th><g:message code="total.amount"/></th>
                <th><g:message code="actions"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${orders}" var="order">
                <tr>
                    <td><div class="wrapper" data-label="<g:message code="order.id"/>:">${order.id}</div></td>
                    <td class="${order.paymentStatus}">
                        <div class="wrapper" data-label="<g:message code="payment.status"/>:">
                            <g:message code="${NamedConstants.ORDER_PAYMENT_STATUS [order.paymentStatus]}"/>
                        </div>
                    </td>
                    <td class="${order.shippingStatus}">
                        <div class="wrapper" data-label="<g:message code="shipping.status"/>:"><g:message code="${NamedConstants.SHIPPING_STATUS[order.shippingStatus]}"/></div>
                    </td>
                    <td><div class="wrapper" data-label="<g:message code="order.date"/>:">${order.created.toSiteFormat(true, false, session.timezone)}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="total.amount"/>:"><span class="currency-symbol">${currencySymbol}</span>${orderTotals[order].toCurrency().toPrice()}</div></td>
                    <td class="actions-column">
                        <div class="wrapper" data-label="<g:message code="actions"/>:">
                            <div class="floating-popup">
                                <div class="floating-action-dropper"></div>
                                <div class="popup-body context-menu" data-id="${order.id}">
                                    <div class="action-item close-popup" data-action="details-pending"><g:message code="view.details"/></div>
                                    <div class="action-item close-popup" data-action="order-comment-pending"><g:message code="comments"/></div>
                                    <g:if test="${order.orderStatus != DomainConstants.ORDER_STATUS.COMPLETED && order.paymentStatus != DomainConstants.ORDER_PAYMENT_STATUS.PAID && order.shippingStatus == DomainConstants.SHIPPING_STATUS.AWAITING}">
                                        <div class="action-item close-popup" data-action="payment-pending"><g:message code="pay.now"/></div>
                                        <div class="action-item close-popup" data-action="cancel-pending"><g:message code="cancel.order"/></div>
                                    </g:if>
                                </div>
                            </div>
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="no-data">No pending orders found.</div>
    </g:else>
</div>