<div class="gift-registry-list">
    <table>
        <thead>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="event.name"/></th>
            <th><g:message code="products"/></th>
            <th><g:message code="event.date"/></th>
            <th><g:message code="action"/></th>
        </tr>
        </thead>
        <tbody>
            <g:each in="${giftRegistries}" var="giftRegistry">
                <tr>
                    <td><div class="wrapper" data-label="<g:message code="name"/>:">${giftRegistry.name.encodeAsBMHTML()}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="event.name"/>:">${giftRegistry.eventName.encodeAsBMHTML()}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="products"/>:">${giftRegistry.giftItems.size()}</div></td>
                    <td><div class="wrapper" data-label="<g:message code="event.date"/>:">${giftRegistry.eventDate.toZone(session.timezone)}</div></td>
                    <td>
                        <div class="wrapper" data-label="<g:message code="actions"/>:">
                            <a href="${app.relativeBaseUrl()}gift-registry/products/${giftRegistry.id}"><span class="action-icon details" giftRegistry-id="${giftRegistry.id}" title="<g:message code="details"/>"></span></a>
                            <span class="action-icon view-products" giftRegistry-id="${giftRegistry.id}" title="<g:message code="view.products"/>"></span>
                            <span class="action-icon edit" giftRegistry-id="${giftRegistry.id}" title="<g:message code="edit"/>"></span>
                            <span class="action-icon share" giftRegistry-id="${giftRegistry.id}" title="<g:message code="share" args="${[""]}"/>"></span>
                            <span class="action-icon status" giftRegistry-id="${giftRegistry.id}" title="<g:message code="status"/>"></span>
                            <span class="action-icon remove delete" giftRegistry-id="${giftRegistry.id}" title="<g:message code="remove"/>"></span>
                        </div>
                    </td>
                </tr>
            </g:each>
            <g:if test="${giftRegistries.size() < 1}">
                <tr>
                    <td colspan="5">
                        <span class="no-data"><g:message code="no.gift.registry.found"/> </span>
                    </td>
                </tr>
            </g:if>
        </tbody>
    </table>
    <span class="button create-gift-registry"><g:message code="create.new.gift.registry"/></span>
</div>