<%@ page import="com.webcommander.util.StringUtil; com.webcommander.conversion.MassConversions; com.webcommander.util.AppUtil; com.webcommander.constants.*;" %>
<tr class="new-rate-row">
    <td colspan="${classEnabled ? '6' : '5'}">
        <div class="add-rate-form">
        <form action="${app.relativeBaseUrl()}shippingAdmin/saveShippingPolicy" method="post" class="edit-popup-form create-edit-form create-new-rate-form">
            <input type="hidden" name="id" value="${rate.id}"/>
            <input type="hidden" name="shipping-rule-id" value="${shipping_rule_id}"/>
            <div class="double-input-row">
                <div class="form-row mandatory">
                    <label><g:message code="rate.name"/><span class="suggestion">e.g. Shipping Policy 1</span></label>
                    <input type="text" class="unique large" name="name" value="${rate.name.encodeAsBMHTML()}" unique-action="isShippingPolicyUnique" validation="required maxlength[100]" maxlength="100">
                </div>
                <div class="form-row chosen-wrapper">
                    <label><g:message code="method"/><span class="suggestion">e.g. Free Shipping</span></label>
                    <ui:namedSelect class="shipping-policy-type medium" toggle-target="display"  name="policyType" key="${NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS}" value="${rate.policyType}"/>
                </div>
            </div>
            <div class="double-input-row">
                <div class="form-row cumulative-panel display-fr display-sbw display-sbq display-sba">
                    <input type="checkbox" class="single" name="isCumulative" value="true" ${rate.isCumulative ? "checked='checked'" : ""}>
                    <span><g:message code="cumulative"/></span>
                </div><div class="form-row display-sba">
                <input type="checkbox" class="single" name="isPriceEnterWithTax" value="true" ${rate.isPriceEnterWithTax ? "checked='checked'" : ""} uncheck-value="false">
                <span><g:message code="price.entered.with.tax"/></span>
            </div>
            </div>
            <div class="form-row mandatory display-fr">
                <label><g:message code="shipping.cost"/><span class="suggestion">e.g. 10 or 10%</span></label>
                <input type="text" name="singleShippingCost" value="${rate.conditions[0]?.getDisplayShippingCost()}" class="small shipping-cost" validation="skip@if{self::hidden} required percent_number maxlength[9]" maxlength="9">
            </div>
            <div class="form-row display-fr display-fs">
                <label><g:message code="handling.cost"/><span class="suggestion">e.g. 1.0</span></label>
                <input type="text" name="singleHandlingCost" value="${rate.conditions[0]?.getDisplayHandlingCost()}" class="small handling-cost" validation="percent_number maxlength[9]" maxlength="9">
            </div>

            <div class="multi-conditions display-sbw display-sbq display-sba">
                <g:set var="isByQty" value="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_QUANTITY}"/>
                <g:set var="isByWeight" value="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT}"/>
                <g:set var="unitWeight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")}"/>
                <g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
                <div class="rate-condition-wrap block-error">
                    <div class="shipping-rate-selection" validation="skip@if{self::hidden} skip@if{this::input[name='from']} fail" validate-on="call-only" message_template="<g:message code='least.one.condition.required.create.shipping'/>">
                        <g:include view="/admin/shipping/rate/shippingConditionEditor.gsp" model="[rate: rate, currencySymbol: currencySymbol, unitWeight: unitWeight]"/>
                    </div>
                </div>

                <div class="addition-condition">
                    <div class="form-row">
                        <input type="checkbox" class="single" name='additional-condition' toggle-target="additional-value" value="true" uncheck-value="false" ${rate.isAdditional ? "checked" : ""}>
                        <span><g:message code="enable.additional.rule"/></span>
                    </div>

                    <div class="double-input-row additional-value">
                        <div class="form-row mandatory">
                            <label><g:message code="for.each.additional"/></label>
                            <div class="has-inline-label additional">
                                <span class="display-sba label dolar">${currencySymbol}</span>
                                <input type="text" name="additional-amount" restrict="${isByQty ? 'numeric' : 'decimal'}" maxlength="9" validation="skip@if{self::hidden} required gt[0] maxlength[9] ${isByQty ? 'digits' : 'number'} ${isByQty ? '' : 'price'}" value="${rate.isAdditional ?
                                        (isByWeight ? MassConversions.convertSIToMass(unitWeight, rate.additionalAmount).toWeight(false) : rate.additionalAmount.toAdminPrice()) : ""}" class="additional-amount">
                                <span class="display-sbw note"><g:message code="${unitWeight}"/></span>
                            </div>
                        </div><div class="form-row mandatory">
                        <label><g:message code="add"/></label>
                        <div class="has-inline-label add">
                            <span class="label dolar">${currencySymbol}</span>
                            <input validation='skip@if{self::hidden} required gt[0] maxlength[9] price' type="text" restrict="decimal" maxlength="9" name="additional-cost" value="${rate.isAdditional ? rate.additionalCost.toAdminPrice() : ""}">
                        </div>
                    </div>
                    </div>
                </div>
            </div>
            <div class="single-condition display-api">
                <plugin:hookTag hookPoint="shippingRateApiBlock">
                    <div class="form-row">
                        <label><g:message code="handling.cost"/><span class="suggestion">e.g. 1.0</span></label>
                        <input type="text" class="medium" name="apiHandlingCost" restrict="decimal" maxlength="9" value="${rate.conditions[0]?.getDisplayHandlingCost()}">
                    </div>
                    <div class="form-row chosen-wrapper">
                        <label><g:message code="select.api"/></label>
                        <ui:namedSelect class="medium" toggle-target="ca" name="api" key="${NamedConstants.SHIPPING_API}" value="${rate.conditions[0]?.apiType}"/>
                    </div>
                    <div class="form-row ca-auspost chosen-wrapper">
                        <label><g:message code="service.type"/></label>
                        <ui:namedSelect class="medium" toggle-target="ecv" name="apiService" key="${NamedConstants.SHIPPING_API_SERVICE_TYPE}" value="${rate.conditions[0]?.apiServiceType}"/>
                    </div>
                    <div class="form-row ecv-rprpec ecv-rprpdcec ecv-ppsec">
                        <label><g:message code="extra.cover.value"/></label>
                        <input type="text" name="extraCoverValue" value="${rate.conditions[0]?.extraCover}" class="medium" restrict="decimal" condition-validation="maxlength[9]" maxlength="9"/>
                    </div>
                    <div class="form-row chosen-wrapper">
                        <label><g:message code="packing.algorithm"/></label>
                        <ui:namedSelect class="medium packing-algorithm" name="packingAlgorithm" key="${NamedConstants.PACKING_ALGORITHM}" value="${rate.conditions[0]?.packingAlgorithm}"/>
                    </div>
                </plugin:hookTag>

            </div>
            <div class="form-row btn-row">
                <button type="submit" class="submit-button edit-popup-form-submit"><g:message code="save"/></button>
                <button type="button" class="cancel-button"><g:message code="cancel"/></button>
            </div>
        </form>
    </div></td>
</tr>

