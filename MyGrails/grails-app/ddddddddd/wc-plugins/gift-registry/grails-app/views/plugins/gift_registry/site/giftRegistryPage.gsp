<%
    app.enqueueSiteJs(src: "plugins/gift-registry/js/gift-registry-details.js", scriptId: "gift-registry-details")
%>
<div class="gift-registry-details">
    <input type="hidden" name="giftRegistryId" value="${registry.id}">
    <div class="name-row">
        <label><g:message code="name"/>:</label>
        <span>${registry.name.encodeAsBMHTML()}</span>
    </div>
    <div class="event-name-row">
        <label><g:message code="event.name"/>:</label>
        <span>${registry.eventName.encodeAsBMHTML()}</span>
    </div>
    <div class="event-date-row">
        <label><g:message code="event.date"/>:</label>
        <span>${registry.eventDate.toZone(session.timezone)}</span>
    </div>
    <g:if test="${registry.eventDetails}">
        <div class="event-details-row">
            <label><g:message code="event.details"/>:</label>
            <span>${registry.eventDetails.encodeAsHTML()}</span>
        </div>
    </g:if>
    <g:if test="${totalCount == 0}">
        <span><g:message code="no.product.added.in.gift.registry"/> </span>
    </g:if>
    <g:else>
        <g:include view="widget/productListings.gsp" model="[productList: productList, config: config, totalCount: totalCount, offset: offset, max: max, url_prefix: 'prwd']"/>
    </g:else>
</div>