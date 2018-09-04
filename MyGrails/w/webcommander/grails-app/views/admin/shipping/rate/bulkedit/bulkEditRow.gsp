<%@ page import="com.webcommander.util.StringUtil; com.webcommander.conversion.MassConversions; com.webcommander.util.AppUtil; com.webcommander.constants.*;" %>
<tr class="rate-bulk-edit-form">
    <g:set var="isByQty" value="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_QUANTITY}"/>
    <g:set var="isByWeight" value="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT}"/>
    <g:set var="unitWeight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")}"/>
    <g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
    <input type="hidden" name="id" value="${rate.id}"/>
    <input type="hidden" name="shipping-rule-id" value="${shipping_rule_id}"/>
    <td class="form-row mandatory">
        <input type="text" class="small unique" name="name" value="${rate.name.encodeAsBMHTML()}" unique-action="isShippingPolicyUnique" validation="required maxlength[100]" maxlength="100">
    </td>
    <td class="form-row chosen-wrapper">
        <ui:namedSelect class="small shipping-policy-type" toggle-target="display_${rate.id}"  name="policyType" key="${NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS}" value="${rate.policyType}"/>
        <div class="form-row cumulative-panel display_${rate.id}-fr display_${rate.id}-sbw display_${rate.id}-sbq display_${rate.id}-sba">
            <input type="checkbox" class="small single" name="isCumulative" value="true" ${rate.isCumulative ? "checked='checked'" : ""}>
            <span><g:message code="cumulative"/></span>
        </div>
        <div class="form-row display_${rate.id}-sba">
            <input type="checkbox" class="small single" name="isPriceEnterWithTax" value="true" ${rate.isPriceEnterWithTax ? "checked='checked'" : ""} uncheck-value="false">
            <span><g:message code="price.entered.with.tax"/></span>
        </div>
    </td>
    <td class="form-row mandatory">
        <input type="text" name="singleShippingCost" value="${rate.conditions[0]?.getDisplayShippingCost()}" class="small shipping-cost display_${rate.id}-fr" validation="skip@if{self::hidden} required percent_number maxlength[9]" maxlength="9">
        <div class="rate-condition-wrap block-error multi-conditions display_${rate.id}-sbw display_${rate.id}-sbq display_${rate.id}-sba">
            <div class="shipping-rate-selection" validation="skip@if{self::hidden} skip@if{this::input[name='from']} fail" validate-on="call-only" message_template="<g:message code='least.one.condition.required.create.shipping'/>">
                <g:include view="/admin/shipping/rate/bulkedit/bulkEditConditionEditor.gsp" model="[rate: rate, currencySymbol: currencySymbol, unitWeight: unitWeight]"/>
            </div>
        </div>
        <div class="addition-condition">
            <div class="form-row">
                <input type="checkbox" class="small single" name='additional-condition' toggle-target="additional-value_${rate.id}" value="true" uncheck-value="false" ${rate.isAdditional ? "checked" : ""}>
                <span><g:message code="enable.additional.rule"/></span>
            </div>
            <div class="double-input-row additional-value additional-value_${rate.id}">
                <div class="form-row mandatory">
                    <label><g:message code="for.each.additional"/></label>
                    <div class="has-inline-label additional">
                        <span class="display_${rate.id}-sba label dolar">${currencySymbol}</span>
                        <span class="display_${rate.id}-sbw note"><g:message code="${unitWeight}"/></span>
                        <input type="text" class="small" name="additional-amount" restrict="${isByQty ? 'numeric' : 'decimal'}" maxlength="9" validation="skip@if{self::hidden} required gt[0] maxlength[9] ${isByQty ? 'digits' : 'number'} ${isByQty ? '' : 'price'}" value="${rate.isAdditional ?
                                (isByWeight ? MassConversions.convertSIToMass(unitWeight, rate.additionalAmount).toWeight(false) : rate.additionalAmount.toAdminPrice()) : ""}" class="additional-amount">
                    </div>
                </div><div class="form-row mandatory">
                <label><g:message code="add"/></label>
                        <div class="has-inline-label add">
                            <span class="label dolar">${currencySymbol}</span>
                            <input class="small" validation='skip@if{self::hidden} required gt[0] maxlength[9] price' type="text" restrict="decimal" maxlength="9" name="additional-cost" value="${rate.isAdditional ? rate.additionalCost.toAdminPrice() : ""}">
                        </div>
                </div>
            </div>
        </div>
    </td>
    <td class="form-row display_${rate.id}-fr display_${rate.id}-fs">
        <input class="small" type="text" name="singleHandlingCost" value="${rate.conditions[0]?.getDisplayHandlingCost()}" class="small handling-cost" validation="percent_number maxlength[9]" maxlength="9">
    </td>
    %{--<td colspan="2" class="display_${rate.id}-sbw display_${rate.id}-sbq display_${rate.id}-sba">

    </td>--}%
</tr>

