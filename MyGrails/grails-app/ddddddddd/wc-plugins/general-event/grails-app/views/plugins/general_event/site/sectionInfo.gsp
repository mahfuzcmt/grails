<%@ page import="com.webcommander.util.AppUtil" %>
<div class="section-info-view">
    <span class="section-name">${section.name.encodeAsBMHTML()}</span>
    <g:if test="${showCart}">
        <span class="section-price price">
            <span class="price-amount">${section.ticketPrice.toCurrency().toPrice()}</span>
            <input type="hidden" name="section" value="${section.id}">
        </span>
        <span class="cart-row">
            <label></label>
            <input type="number" class="ticket-quantity-selector text-type" value="${1}" spin-min="${1}" spin-max="${spinMax}" spin-step="${'1'}">
            <license:allowed id="event">
                <span class="section-ticket-add-to-cart-button button et_pdp_add_to_cart" et-category="button"><g:message code="add.to.cart"/></span>
            </license:allowed>
            <license:otherwise>
                <div class="form-row service-disabled">
                    <g:message code="purchase.ticket.temporarily.disabled"/>
                </div>
            </license:otherwise>
        </span>
    </g:if>
</div>