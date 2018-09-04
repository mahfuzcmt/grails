<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants;" %>
<div id="abondoned-cart-list">
    <g:if test="${carts.size()}">
        <table>
            <colgroup>
                <col class="id-col">
                <col class="order-date-col">
                <col class="total-amount-col">
                <col class="action-col">
            </colgroup>
            <thead>
            <tr>
                <th><g:message code="cart.no"/></th>
                <th><g:message code="created"/></th>
                <th><g:message code="total.amount"/></th>
                <th><g:message code="actions"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${carts}" var="cart">
                <tr>
                    <td><div class="wrapper" data-label="<g:message code="cart.no"/>:">${cart.id}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="created"/>:">${cart.created.toSiteFormat(true, false, session.timezone)}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="total.amount"/>:">${cart.total().toCurrency().toPrice()}</div></td>
                    <td>
                        <div class="wrapper" data-label="<g:message code="actions"/>:">
                            <span class="action-icon details" title="${g.message(code: 'view.details')}" cart-id="${cart.id}"></span>
                            <span class="action-icon add-to-cart" title="${g.message(code: 'add.to.cart')}" cart-id="${cart.id}"></span>
                            <span class="action-icon delete" title="${g.message(code: 'remove')}" cart-id="${cart.id}"></span>
                        </div>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </g:if>
    <g:else>
        <div class="no-data">No abandoned cart found.</div>
    </g:else>

</div>