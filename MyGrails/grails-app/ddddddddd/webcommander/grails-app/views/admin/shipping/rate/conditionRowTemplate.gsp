<%@ page import="com.webcommander.conversion.MassConversions; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants;" %>
<g:set var="unitWeight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")}"/>
<tr class='condition-row'>
    <td class='range-disp' colspan='2'>
        <span class='from'>
            <g:set var="from" value="${isWeight ? MassConversions.convertSIToMass(unitWeight, condition.fromAmount).toWeight(false) : condition.fromAmount.toAdminPrice()}"/>
            <g:set var="to" value="${isWeight ? MassConversions.convertSIToMass(unitWeight, condition.toAmount).toWeight(false) : condition.toAmount.toAdminPrice()}"/>
            <input type='hidden' name='from' value='${from}'>
            <span class='value'>${from}</span>
        </span>&nbsp;-&nbsp;
        <span class='to'>
            <input type='hidden' name='to' value='${to}'>
            <span class='value'>${to}</span>
        </span>
    </td>
    <td>
        <span class="shippingCost">
            <input type='hidden' name='shippingCost' value='${params.newRow? params.shippingCost: condition.getDisplayShippingCost()}'>
            <span class='value'>${params.newRow? params.shippingCost: condition.getDisplayShippingCost(true)}</span>
        </span>
    </td>
    <td>
        <span class="handlingCost">
            <input type='hidden' name='handlingCost' value='${params.newRow? params.handlingCost: condition.getDisplayHandlingCost()}'>
            <span class='value'>${params.newRow? params.handlingCost: condition.getDisplayHandlingCost(true)}</span>
        </span>
    </td>
    <td class='display-sbw' style="${isWeight ? '' : 'display:none'}">
        <span class="packetWeight">
            <g:set var="packWeight" value="${params.newRow ? MassConversions.convertSIToMass(unitWeight, params.packetWeight).toWeight(false) : condition.packetWeight}"/>
            <input type='hidden' name='packetWeight' value='${packWeight}'>
            <span class='value'>${packWeight}</span>
        </span>

    </td>
    <td class='actions-column'>
        <span class="action-navigator collapsed"></span>
    </td>
</tr>
