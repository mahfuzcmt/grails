<%@ page import="com.webcommander.conversion.MassConversions; com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil; com.webcommander.util.StringUtil" %>
<div class="shipping-condition-editor" validation-attr="condition-validation">
    <table class="shipping-condition-table">
        <tbody>
            <tr>
                <th colspan="2"><g:message code="range"/></th>
                <th><label><g:message code="shipping.charge"/><span class="suggestion"> e.g. 10 or 10%</span></label></th>
                <th><g:message code="handling.cost"/></th>
                <th class="display-sbw"><g:message code="packet.weight"/></th>
                <th class="actions-column"><g:message code="action"/></th>
            </tr>
        <g:set var="isWeight" value="${rate.policyType == "sbw"}"/>
        <g:set var="unit_weight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")}"/>
        <g:set var="isByQty" value="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_QUANTITY}"/>
        <g:if test="${rate.policyType == "sbw" || rate.policyType == "sbq" || rate.policyType == "sba"}">
            <g:each in="${rate.conditions}" var="condition">
                <g:include view="admin/shipping/rate/conditionRowTemplate.gsp" model="[condition: condition, isWeight: isWeight]"/>
            </g:each>
        </g:if>
            <tr class="last-row">
                <td class="form-row range" colspan="2">
                    <g:set var="ref1" value="${StringUtil.uuid}"/>
                    <g:set var="ref2" value="${StringUtil.uuid}"/>
                    <div class="twice-input-row">
                        <span class="display-sba label dolar">${currencySymbol}</span>
                        <input type="text" class="small from td-full-width" condition-validation="required number maxlength[9] ${isByQty ? "digits" : "price"}" maxlength="9" restrict="decimal" id="${ref2}"/><span>
                        to</span><input type="text" class="small to td-full-width" condition-validation="required number compare[${ref2}, number, gte] maxlength[9] ${isByQty ? "digits" : "price"}" maxlength="9" restrict="decimal" id="${ref1}" depends="#${ref2}">
                    </div>
                    <span class="display-sbw note"><g:message code="${unitWeight}"/>
                </td>
                <td class="form-row">
                    <input type="text" class="small shipping-cost td-full-width" error-on="inline" condition-validation="required percent_number maxlength[9]" maxlength="9">
                </td>
                <td class="form-row">
                    <input type="text" class="small handling-cost td-full-width" error-on="inline" condition-validation="required percent_number maxlength[9]" maxlength="9">
                </td>
                <td class="form-row display-sbw">
                    <input type="text" class="small packet-weight td-full-width" error-on="inline" condition-validation='skip@if{self::hidden} required number maxlength[9]' maxlength="9" restrict="decimal">
                    <span class="note"><g:message code="${unitWeight}"/></span>
                </td>
                <td class="actions-column"><button type="button" class="submit-button add-condition">+ <g:message code="add"/></button></td>
            </tr>
        </tbody>
    </table>
</div>