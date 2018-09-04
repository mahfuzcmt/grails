<%@ page import="com.webcommander.models.ProductInCartBase; com.webcommander.models.ProductInCart" %>
<form class="edit-popup-form quote-edit-popup" action="${app.relativeBaseUrl()}quoteAdmin/save">
    <input type="hidden" name="customer" value="${quote.customer.id}">
    <input type="hidden" name="quote" value="${quote.id}">
    <input type="hidden" name="billing" value="">
    <input type="hidden" name="shipping" value="">

    <div class="basic-info">
        <div class="info-row">
            <label><g:message code="quote.id"/>:</label>
            <span class="value">${quote.id}</span>
        </div>
        <div class="info-row">
            <label><g:message code="quote.date"/>:</label>
            <span class="value">${quote.created.toAdminFormat(true, false, session.timezone)}</span>
        </div>
        <div class="info-row">
            <label><g:message code="discount"/>:</label>
            <span class="value">${quote.totalDiscount.toPrice()}</span>
        </div>
        <div class="info-row">
            <label><g:message code="total.tax"/>:</label>
            <span class="value">${quote.totalTax.toPrice()}</span>
        </div>
        <div class="info-row">
            <label><g:message code="total.shipping"/>:</label>
            <span class="editable-group">
                <span class="value">${quote.shippingCost.toPrice()}</span>
                <input type="text" name="shippingCost" value="${quote.shippingCost.toPrice()}" restrict="decimal" validation="number min[0] maxprecision[9,6]">
                <span class="tool-icon edit action"><i></i></span>
            </span>
        </div>
        <div class="info-row">
            <label><g:message code="total.handling.cost"/>:</label>
            <span class="editable-group">
                <span class="value">${quote.handlingCost.toPrice()}</span>
                <input type="text" name="handlingCost" value="${quote.handlingCost.toPrice()}" restrict="decimal" validation="number min[0] maxprecision[9,6]">
                <span class="tool-icon edit action"><i></i></span>
            </span>
        </div>
        <div class="info-row">
            <label><g:message code="total.shipping.tax"/>:</label>
            <span class="editable-group">
                <span class="value">${quote.shippingTax.toPrice()}</span>
                <input type="text" name="shippingTax" value="${quote.shippingTax.toPrice()}" restrict="decimal" validation="number min[0] maxprecision[9,6]">
                <span class="tool-icon edit action"><i></i></span>
            </span>
        </div>
        <div class="info-row">
            <label><g:message code="total.amount"/>:</label>
            <span class="value grand-total">${quote.grandTotal.toPrice()}</span>
        </div>
    </div>
    <table class="details-table items">
        <tr>
            <th class="name-column"><g:message code="product"/></th>
            <th class="price-column"><g:message code="price"/></th>
            <th class="quantity-column"><g:message code="quantity"/></th>
            <th class="discount-column"><g:message code="discount"/></th>
            <th class="tax-column"><g:message code="tax"/></th>
            <th class="total-column"><g:message code="total.amount"/></th>
            <th class="total-column"><g:message code="action"/></th>
        </tr>
        <g:each in="${quote.quoteItems}" var="item">
            <tr class="item">
                <td>${item.object.name.encodeAsBMHTML()} ${item.variations ? "(" + item.variations.join(", ").encodeAsBMHTML() + ")" : ""}</td>
                <td>
                    <span class="editable-group">
                        <span class="value">${item.price.toPrice()}</span>
                        <input type="text" class="price" name="items.${item.id}.price" value="${item.price.toPrice()}" restrict="decimal" validation="required number gt[0] maxprecision[9,6]">
                        <span class="tool-icon action edit"><i></i></span>
                    </span>
                </td>
                <td class="quantity-adjust">
                    <%
                        Integer min = 1, step = 1, max;
                        if(item.object instanceof ProductInCart) {
                            max = item.object.product.supportedMaxOrderQuantity
                            min = max == 0 ? 0 : item.object.product.supportedMinOrderQuantity
                            step = item.object.product.multipleOfOrderQuantity
                        }
                    %>
                    <input type="text" name="items.${item.id}.quantity" value="${item.quantity}" restrict="numeric" class="spinner quantity" error-position="none" validation="required number max[${max}] min[${min}]" spin-max="${max}" spin-min="${min}" spin-step="${step}">
                </td>
                <td>
                    <span class="editable-group">
                        <span class="value">${item.discount.toPrice()}</span>
                        <input type="text" class="discount" name="items.${item.id}.discount" value="${item.discount.toPrice()}" restrict="decimal" validation="number min[0] maxprecision[9,6]">
                        <span class="tool-icon action edit"><i></i></span>
                    </span>
                </td>
                <td>
                    <span class="editable-group">
                        <span class="value">${item.tax.toPrice()}</span>
                        <input type="text" class="tax" name="items.${item.id}.tax" value="${item.tax.toPrice()}" restrict="decimal" validation="number min[0] maxprecision[9,6]">
                        <span class="tool-icon action edit"><i></i></span>
                    </span>
                </td>
                <td> <span class="total">${item.totalAmount.toPrice()}</span></td>
                <td>
                    <span class="tool-icon remove" item-id="${item.id}"></span>
                </td>
            </tr>
        </g:each>
    </table>
    <div class="requote-button-line">
        <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="update"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
    <div class="button-line">
        <button type="button" class="change-billing"><g:message code="change.billing"/></button>
        <button type="button" class="change-shipping"><g:message code="change.shipping"/> </button>
    </div>
</form>