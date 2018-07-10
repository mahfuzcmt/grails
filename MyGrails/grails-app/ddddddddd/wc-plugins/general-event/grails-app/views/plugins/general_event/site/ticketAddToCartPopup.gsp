<div class="event-add-to-cart-popup">
    <div class="header">
        <span class="close-popup close-icon"></span>
        <span class="status-bar-ticket-name"></span>
        <span class="status-message"><g:message code="choose.desired.seat"/> - ${section.name.encodeAsBMHTML()}</span>
    </div>
    <div class="body">
        <g:if test="${errorCode}">
            <div class="message-block cart-add-error error-message"><g:message code="${errorCode}" args="[orderedQuantity, availableTickets]"/></div>
        </g:if>
        <g:else>
            <g:include view="/plugins/general_event/site/seatView.gsp" model="[section: section, lockedTickets: lockedTickets]"/>
            <div class="popup-bottom footer">
                <span class="final-ticket-add-to-cart button"><g:message code="add.to.cart"/></span>
            </div>
        </g:else>
    </div>
</div>