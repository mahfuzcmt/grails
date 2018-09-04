<div class="header">
    <span class="item-group entity-count title">
        ${product.name.encodeAsBMHTML()} (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>

<div class="app-tab-content-container body">
    <table class="content">
        <colgroup>
            <col class="name-column">
            <col class="code-column">
            <col class="customer-first-name-column">
            <col class="email-address-column">
            <col class="post-code-column">
            <col class="value-column">
            <col class="status-column">
            <col class="purchase-date-column">
            <col class="expire-date-column">
            <col class="action-column">
        </colgroup>
        <tr>
            <th><g:message code="product.name"/></th>
            <th><g:message code="code"/></th>
            <th><g:message code="customer.first.name"/></th>
            <th><g:message code="email.address"/></th>
            <th><g:message code="post.code"/></th>
            <th><g:message code="value"/></th>
            <th class="status-column"><g:message code="activation.status"/></th>
            <th><g:message code="purchase.date"/></th>
            <th><g:message code="expire.date"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:each in="${giftCards}" var="card">
            <tr>
                <td>${card.productName}</td>
                <td>${card.code}</td>
                <td>${card.firstName}</td>
                <td>${card.email}</td>
                <td>${card.postCode}</td>
                <td>${card.amount}</td>
                <td class="status-column"><span class="status ${card.isActive ? "positive" : "negative"}"></span></td>
                <td>${card.created.toAdminFormat(true, false, session.timezone)}</td>
                <td>${card.availableTo?.toAdminFormat(true, false, session.timezone) ?: "N/A"}</td>
                <td class="actions-column">
                    <span class="action-navigator collapsed" entity-id="${card.id}" entity-product-id="${card.productId}" entity-name="${card.productName.encodeAsBMHTML()}"></span>
                </td>
            </tr>
        </g:each>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>