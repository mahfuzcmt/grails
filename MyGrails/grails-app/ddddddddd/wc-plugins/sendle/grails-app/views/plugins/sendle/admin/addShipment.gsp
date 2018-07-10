<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.plugin.sendle.constants.Constants " %>

<div class="shipping-sendle" style="display: none;">
    <div class="form-row mandatory">
        <label><g:message code="product.description"/><span class="suggestion">e.g. Kryptonite</span></label></label>
        <textarea name="productDescription" class="no-auto-size form-full-width" validation="maxlength[255]" maxlength="255"></textarea>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="receiver.instruction"/><span class="suggestion">e.g. Give directly to Clark</span></label></label>
        <textarea name="receiverInstruction" class="no-auto-size form-full-width" validation="maxlength[200]" maxlength="200">${orderComment ? "${(orderComment.toString()).substring(1, orderComment.toString().length() > 200 ? 200 : orderComment.toString().length() - 1)}" : ""}</textarea>
    </div>
    <div class="form-row mandatory">
        <label><g:message code="pickup.date.for.sendle"/><span class="suggestion">e.g. The date must be at least one non-holiday, business day in the future.</span></label></label>
        <input type="text" class="large datefield" no-previous="true" name="pickupDate" validate-on="call-only">
    </div>
</div>

