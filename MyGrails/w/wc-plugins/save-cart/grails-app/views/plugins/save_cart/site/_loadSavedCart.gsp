<div id="saved-cart-listing">
    <g:if test="${carts}">
        <table>
            <colgroup>
                <col class="name-column">
                <col class="date-column">
                <col class="actions-column">
            </colgroup>
            <thead>
                <tr>
                    <th><g:message code="name"/></th>
                    <th><g:message code="date"/></th>
                    <th><g:message code="products"/></th>
                    <th><g:message code="actions"/></th>
                </tr>
            </thead>

            <tbody>
                <g:each in="${carts}" var="cart">
                    <tr>
                        <td><div class="wrapper" data-label="<g:message code="name"/>:">${cart.name.encodeAsBMHTML()}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="date"/>:">${cart.created.toZone(session.timezone)}</div></td>
                        <td><div class="wrapper" data-label="<g:message code="item"/>:">${cart.cartItems.size()}</div></td>
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
        <div class="no-data"><g:message code="save.cart.not.found"/> </div>
    </g:else>
</div>