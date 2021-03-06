<%@ page import="com.webcommander.util.AppUtil; com.webcommander.conversion.MassConversions; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<tr class="row section-list rate-list rate-row" entity-id="${rate.id}" entity-name="${rate.name}">
    <input type="hidden" class="rate-id" value="${rate.id}"/>
    <g:set var="policyType" value="${NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS}"/>
    <g:set var="isByWeight" value="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT}"/>
    <g:set var="unitWeight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")}"/>
    <g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
    <g:set var="condition" value="${rate.conditions? rate.conditions[0] : null}"/>
    <td class="select-column"><input type="checkbox" entity-id="${rate.id}"></td>
    <td class="rate-name">${rate.name.encodeAsBMHTML()}</td>
    <td class="shipping-policy"><span class="hidden filter-text">${rate?.policyType}</span><g:message code="${policyType[rate?.policyType]}"/></td>
    <g:set var="prefix" value="${""}"/>
    <g:set var="suffix" value="${""}"/>
    <g:if test="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT}">
        <g:set var="suffix" value="${g.message(code: unitWeight)}"/>
    </g:if>
    <g:elseif test="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_PRICE}">
        <g:set var="prefix" value="${currencySymbol}"/>
    </g:elseif>
    <g:if test="${rate?.policyType == DomainConstants.SHIPPING_POLICY_TYPE.FREE_SHIPPING}">
        <td class="rate-condition"><g:message code="free"/></td>
        <td class="handling-cost">${condition?.getDisplayHandlingCost(true)}</td>
    </g:if>
    <g:elseif test="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE}">
        <td class="shipping-cost">${condition?.getDisplayShippingCost(true)}</td>
        <td class="handling-cost">${condition?.getDisplayHandlingCost(true)}</td>
    </g:elseif>
    <g:elseif test="${rate.policyType == DomainConstants.SHIPPING_POLICY_TYPE.API}">
        <td class="shipping-cost"></td>
        <td class="handling-cost">${condition?.getDisplayHandlingCost(true)}</td>
    </g:elseif>
    <g:elseif test="${rate.policyType != DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE && rate.policyType != DomainConstants.SHIPPING_POLICY_TYPE.FREE_SHIPPING && rate.policyType != DomainConstants.SHIPPING_POLICY_TYPE.API}">
        <td class="shipping-handling-cost">
            <g:each in="${rate.conditions}" var="condition">
                <g:set var="from" value="${isByWeight ? MassConversions.convertSIToMass(unitWeight, condition.fromAmount).toWeight(false) : condition.fromAmount?.toAdminPrice()}"/>
                <g:set var="to" value="${isByWeight ? MassConversions.convertSIToMass(unitWeight, condition.toAmount).toWeight(false) : condition.toAmount?.toAdminPrice()}"/>
                <g:set var="packet" value="${isByWeight && condition.packetWeight ? MassConversions.convertSIToMass(unitWeight, condition.packetWeight)?.toWeight(false) : ""}"/>
                <div class="shipping-cost-range">
                    <span class="cost">${condition?.getDisplayShippingCost(true)}</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <span class="condition"><g:message code="range"/>: (${prefix + " " + from + " " + suffix + " - " + prefix + " " + to + " " + suffix})</span>
                </div>
            </g:each>
        </td>
        <td class="handling-cost">
            <g:each in="${rate.conditions}" var="condition">
                <div class="cost">${condition.getDisplayHandlingCost(true)}</div>
            </g:each>
        </td>
    </g:elseif>
    <td class="column actions-column">
        <span class="action-navigator collapsed" entity-id="${rate?.id}" entity-name="${rate?.name.encodeAsBMHTML()}"></span>
    </td>
</tr>