<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;" %>
<g:set var="currencySymbol" value="${session.currency?.symbol ?: AppUtil.baseCurrency.symbol}"/>
<div id="order-list">
    <g:if test="${orders.size()}">
        <table>
            <colgroup>
                <col class="id-col">
                <col class="order-date-col">
                <col class="total-amount-col">
                <col class="action-col">
            </colgroup>
            <thead>
            <tr>
                <th><g:message code="order.id"/></th>
                <th><g:message code="order.date"/></th>
                <th><g:message code="total.amount"/></th>
                <th><g:message code="actions"/></th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${orders}" var="order">
                    <tr>
                        <td><div class="wrapper" data-label="<g:message code="order.id"/>:">${order.id}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="order.date"/>:">${order.created.toSiteFormat(true, false, session.timezone)}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="total.amount"/>:"><span class="currency-symbol">${currencySymbol}</span>${orderTotals[order].toCurrency().toPrice()}</div></td>
                        <td class="actions-column">
                            <div class="wrapper" data-label="<g:message code="actions"/>:">
                                <div class="floating-popup">
                                    <div class="floating-action-dropper"></div>
                                    <div class="popup-body context-menu" data-id="${order.id}">
                                        <plugin:hookTag hookPoint="orderListViewActionInCustomerProfileCompletedOrder" attrs="[order: order]">
                                            <div class="action-item close-popup" data-action="details-completed"><g:message code="view.details"/></div>
                                            <div class="action-item close-popup" data-action="reorder-completed"><g:message code="reorder"/></div>
                                            <div class="action-item close-popup" data-action="order-comment-completed"><g:message code="comments"/></div>
                                        </plugin:hookTag>
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
        <div class="no-data"><g:message code="no.completed.orders.found"/></div>
    </g:else>
</div>