<%@ page import="com.webcommander.plugin.discount.NameConstants; com.webcommander.webcommerce.ShippingClass; com.webcommander.admin.Zone; com.webcommander.plugin.discount.Constants" %>
<div class="shipping-discount-details-wrap">
    <div class="double-input-row">
        <div class="form-row">
            <label><g:message code="zone"/></label>
            <g:select name="shippingDetails.zone" from="${zones}" optionValue="name" optionKey="id" value="${discountDetails.zone?.id}" noSelection="${['':g.message([code: "all"])]}"/>
        </div><div class="form-row">
            <label><g:message code="shipping.class"/></label>
            <ui:domainSelect prepend="${['': g.message(code: "all")]}" name="shippingDetails.shippingClass" domain="${ShippingClass}" value="${discountDetails.shippingClass?.id}"/>
        </div>
    </div>
    <div validation="skip@if{self::hidden} skip@if{this::input[name='shippingDetails.type']:checked} fail" message_template="<g:message code="choose.an.option"/>" validate-on="call-only">
        <div class="amount-discount-details" data-type="${Constants.SHIPPING_DETAILS_TYPE.FREE_SHIPPING}">
            <div class="form-row">
                <input type="radio" name="shippingDetails.type" value="${Constants.SHIPPING_DETAILS_TYPE.FREE_SHIPPING}" ${discountDetails.type == Constants.SHIPPING_DETAILS_TYPE.FREE_SHIPPING ? "checked" : ""} toggle-target="free-shipping-dependents">
                <label><g:message code="free.shipping"/></label>
            </div>
        </div>
        <div class="amount-discount-details" data-type="${Constants.SHIPPING_DETAILS_TYPE.SHIPPING_CAP}">
            <div class="form-row">
                <input type="radio" name="shippingDetails.type" value="${Constants.SHIPPING_DETAILS_TYPE.SHIPPING_CAP}" ${discountDetails.type == Constants.SHIPPING_DETAILS_TYPE.SHIPPING_CAP ? "checked" : ""} toggle-target="shipping-price-cap-dependents">
                <label><g:message code="price.cap"/></label>
            </div>
            <div class="dependents shipping-price-cap-dependents">
                <div class="form-row">
                    <label><g:message code="amount"/></label>
                    <input type="text" name="shippingDetails.capAmount" value="${discountDetails.capAmount?.toAdminPrice()}" validation="skip@if{self::hidden} required price" restrict="decimal" maxlength="10">
                </div>
            </div>
        </div>
        <div class="amount-discount-details" data-type="${Constants.SHIPPING_DETAILS_TYPE.DISCOUNT_AMOUNT}">
            <div class="form-row">
                <input type="radio" name="shippingDetails.type" value="${Constants.SHIPPING_DETAILS_TYPE.DISCOUNT_AMOUNT}" ${discountDetails.type == Constants.SHIPPING_DETAILS_TYPE.DISCOUNT_AMOUNT ? "checked" : ""} toggle-target="shipping-discount-amount-dependents">
                <label><g:message code="discount.amount"/></label>
            </div>
            <div class="dependents shipping-discount-amount-dependents" validation="skip@if{self::hidden} skip@if{this::input[name='shippingDetails.amountType']:checked} fail" message_template="<g:message code="choose.an.option"/>" validate-on="call-only">
                <div class="form-row">
                    <input type="radio" name="shippingDetails.amountType" value="${Constants.SHIPPING_DETAILS_AMOUNT_TYPE.SINGLE}" ${discountDetails.amountType == Constants.SHIPPING_DETAILS_AMOUNT_TYPE.SINGLE ? "checked" : ""} toggle-target="shipping-single-amount-dependents">
                    <label><g:message code="single.amount"/></label>
                </div>
                <div class="dependents shipping-single-amount-dependents">
                    <div class="form-row">
                        <label><g:message code="discount.amount"/></label>
                        <div class="field-group">
                            <div class="field">
                                <input type="text" name="shippingDetails.singleAmount" value="${discountDetails.singleAmount?.toAdminPrice()}" restrict="decimal" validation="required@if{self::visible} price gt[0]" maxlength="9">
                            </div>
                            <div class="field">
                                <ui:namedSelect class="addon" name="shippingDetails.singleAmountType" key="${NameConstants.DISCOUNT_AMOUNT_TYPE}" value="${discountDetails.singleAmountType}"/>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="form-row">
                    <input type="radio" name="shippingDetails.amountType" value="${Constants.SHIPPING_DETAILS_AMOUNT_TYPE.TIERED}" ${discountDetails.amountType == Constants.SHIPPING_DETAILS_AMOUNT_TYPE.TIERED ? "checked" : ""} toggle-target="shipping-tier-amount-dependents">
                    <label><g:message code="tiered.discount"/></label>
                </div>
                <div class="dependents shipping-tier-amount-dependents">
                    <table class="tier-table">
                        <tr>
                            <th><g:message code="minimum.amount" /></th>
                            <th><g:message code="discount.value" /></th>
                            <th></th>
                        </tr>
                        <g:each in="${discountDetails?.tiers}" var="tier" status="i">
                            <tr rowid="${i}" class="tier-details-row">
                                <td><div class="field">
                                    <input type="text" name="shippingDetails.amountTier.${i}.minimumAmount" validation="required@if{self::visible} price gt[0]" restrict="decimal" maxlength="9" value="${tier.minimumAmount?.toAdminPrice()}">
                                </div></td>
                                <td>
                                    <div class="field-group">
                                        <div class="field">
                                            <input type="text" name="shippingDetails.amountTier.${i}.amount"  validation="required@if{self::visible} price gt[0]" restrict="decimal" maxlength="9" value="${tier.amount?.toAdminPrice()}">
                                        </div>
                                        <div class="field">
                                            <ui:namedSelect class="addon raw" name="shippingDetails.amountTier.${i}.amountType" key="${NameConstants.DISCOUNT_AMOUNT_TYPE}" value="${tier.amountType}"/>
                                        </div>
                                    </div>
                                </td>
                                <td>
                                    <span class="tool-icon remove"></span>
                                </td>
                            </tr>
                        </g:each>
                        <tr class="last-row"><td colspan="3">
                            <span class="link-btn add-row">+<g:message code="add.new.tier"/></span>
                        </td></tr>
                        <tr class="template hidden">
                            <td><div class="field"><input type="text" data-name="minimumAmount" data-validation="required@if{self::visible} price gt[0]" restrict="decimal" maxlength="9"></div></td>
                            <td><div class="field-group">
                                <div class="field">
                                    <input type="text" data-name="amount"  data-validation="required@if{self::visible} price gt[0]" restrict="decimal" maxlength="9">
                                </div>
                                <div class="field">
                                    <ui:namedSelect class="addon raw" data-name="amountType" key="${NameConstants.DISCOUNT_AMOUNT_TYPE}" />
                                </div>
                            </div></td>
                            <td>
                                <span class="tool-icon remove"></span>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
    </div>

</div>