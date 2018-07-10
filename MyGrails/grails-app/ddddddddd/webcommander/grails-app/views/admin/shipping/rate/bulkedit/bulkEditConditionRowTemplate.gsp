<%@ page import="com.webcommander.util.StringUtil; com.webcommander.conversion.MassConversions; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants;" %>
<g:set var="unit_weight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")}"/>
<g:set var="unitWeight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")}"/>
<tr class='condition-row'>
    <td class='range-disp' colspan='2'>
        <g:set var="ref" value="${StringUtil.uuid}"/>
        <div class='form-row'>
            <g:set var="from" value="${isWeight ? MassConversions.convertSIToMass(unitWeight, condition.fromAmount).toWeight(false) : condition.fromAmount.toAdminPrice()}"/>
            <g:set var="to" value="${isWeight ? MassConversions.convertSIToMass(unitWeight, condition.toAmount).toWeight(false) : condition.toAmount.toAdminPrice()}"/>
            <input class="small" condition-validation="required number maxlength[9] ${isByQty ? "digits" : "price"} gte[0]" maxlength="9" restrict="decimal" type='text' name='from' value='${from}' id="${ref}">
        </div>&nbsp;-&nbsp;
        <div class='form-row'>
            <input class="small" condition-validation="required number compare[${ref}, number, gte] maxlength[9] ${isByQty ? "digits" : "price"}" maxlength="9" restrict="decimal" type='text' name='to' value='${to}'>
        </div>
    </td>
    <td>
        <div class="form-row"><input type='text' class="small" name='shippingCost' condition-validation="required percent_number maxlength[9] gte[0]"  value='${params.newRow? params.shippingCost: condition.getDisplayShippingCost()}'></div>
    </td>
    <td class='display_${rate?.id ?: rateId}-sbw weight_only' style="${isWeight ? '' : 'display:none'}">
        <div class="form-row"><g:set var="packWeight" value="${isWeight ? MassConversions.convertSIToMass(unitWeight, condition.packetWeight).toWeight(false) : condition.packetWeight}"/>
            <input type='text' class="small" name='packetWeight' condition-validation='skip@if{self::hidden} required number maxlength[9] gt[0]' value='${packWeight}' restrict="decimal">
        </div>
    </td>
    <td>
        <div class="form-row"><input type='text' class="small" name='handlingCost' condition-validation="required percent_number maxlength[9] gte[0]" value='${params.newRow? params.handlingCost: condition.getDisplayHandlingCost()}' placeholder="${g.message(code: 'handling.cost')}"></div>
    </td>
</tr>