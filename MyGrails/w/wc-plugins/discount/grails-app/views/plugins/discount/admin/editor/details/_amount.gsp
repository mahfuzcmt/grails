<%@ page import="com.webcommander.plugin.discount.NameConstants; com.webcommander.plugin.discount.Constants" %>
<div class="amount-discount-details-wrap" validation="skip@if{self::hidden} skip@if{this::input[name='amountDetails.type']:checked} fail" message_template="<g:message code="choose.an.option"/>" validate-on="call-only">
    <div class="amount-discount-details" data-type="${Constants.AMOUNT_DETAILS_TYPE.SINGLE}">
        <div class="form-row">
            <input type="radio" name="amountDetails.type" value="${Constants.AMOUNT_DETAILS_TYPE.SINGLE}" ${discountDetails.type == Constants.AMOUNT_DETAILS_TYPE.SINGLE ? "checked" : ""} toggle-target="single-amount-dependents">
            <label><g:message code="single.amount"/></label>
        </div>
        <div class="dependents single-amount-dependents">
            <div class="form-row">
                <label><g:message code="discount.amount"/></label>
                <div class="field-group">
                    <div class="field">
                        <input type="text" name="amountDetails.singleAmount" value="${discountDetails.singleAmount?.toAdminPrice()}" restrict="decimal" validation="required@if{self::visible} price gt[0]" maxlength="9">
                    </div>
                    <div class="field">
                        <ui:namedSelect class="addon" name="amountDetails.singleAmountType" key="${NameConstants.DISCOUNT_AMOUNT_TYPE}" value="${discountDetails.singleAmountType}"/>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="amount-discount-details" data-type="${Constants.AMOUNT_DETAILS_TYPE.TIERED}">
        <div class="form-row">
            <input type="radio" name="amountDetails.type" value="${Constants.AMOUNT_DETAILS_TYPE.TIERED}" ${discountDetails.type == Constants.AMOUNT_DETAILS_TYPE.TIERED ? "checked" : ""} toggle-target="tiered-amount-dependents">
            <label><g:message code="tiered.discount"/></label>
        </div>
        <div class="dependents tiered-amount-dependents">
            <table class="tier-table">
                <tr>
                    <th><g:message code="minimum.amount" /></th>
                    <th><g:message code="discount.value" /></th>
                    <th></th>
                </tr>
                <g:each in="${discountDetails?.tiers}" var="tier" status="i">
                    <tr rowid="${i}" class="tier-details-row">
                        <td><div class="field">
                            <input type="text" name="amountDetails.amountTier.${i}.minimumAmount" validation="required@if{self::visible} price gt[0]" restrict="decimal" maxlength="9" value="${tier.minimumAmount?.toAdminPrice()}">
                        </div></td>
                        <td>
                            <div class="field-group">
                                <div class="field">
                                    <input type="text" name="amountDetails.amountTier.${i}.amount"  validation="required@if{self::visible} price gt[0]" restrict="decimal" maxlength="9" value="${tier.amount?.toAdminPrice()}">
                                </div>
                                <div class="field">
                                    <ui:namedSelect class="addon raw" name="amountDetails.amountTier.${i}.amountType" key="${NameConstants.DISCOUNT_AMOUNT_TYPE}" value="${tier.amountType}"/>
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

            <g:if test="${discount.discountDetailsType != Constants.DETAILS_TYPE.AMOUNT}">
            <div class="form-row">
                <label><g:message code="minimum.amount.on"/></label>
                <ui:namedSelect name="amountDetails.minimumAmountOn" key="${NameConstants.MINIMUM_AMOUNT_ON}" value="${discountDetails.minimumAmountOn}"/>
            </div>
            </g:if>

        </div>
    </div>
</div>