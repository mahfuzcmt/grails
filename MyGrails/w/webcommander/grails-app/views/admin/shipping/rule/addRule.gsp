<%@ page import="com.webcommander.conversion.MassConversions; com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<div class="rule-body">
    <form class="search-form search-block">
        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
    </form>
    <div class="rule-table">
        <table>
            <colgroup>
                <col class="zone-column">
                <col class="rate-column">
                <col class="actions-column">
            </colgroup>
            <tbody>
            <g:each in="${rules}" var="rule">
                <tr>
                    <td class="section-thumb">
                        <g:set var="countries" value="${rule.zone.countries}"/>
                        <span class="blocklist-subitem-summary-view">${rule.name.encodeAsBMHTML()}</span>
                        <div class="zone-thumb">
                            <g:if test="${countries.size()}">
                                <span class="flag-icon ${countries.size() > 1 ? "" : countries[0].code.toLowerCase()}"></span>
                            </g:if>
                            <span class="title">${rule.zone.name.encodeAsBMHTML()}</span>
                        </div>
                    </td>
                    <g:set var="policyType" value="${NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS}"/>
                    <g:set var="shippingPolicy" value="${rule.shippingPolicy}"/>
                    <g:set var="isByWeight" value="${shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT}"/>
                    <g:set var="unitWeight" value="${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "unit_weight")}"/>
                    <g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
                    <td class="shipping-rate">
                        <span class="title"><g:message code="${policyType[shippingPolicy?.policyType]}"/></span>
                        <g:set var="suffix"/>
                        <g:if test="${shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_WEIGHT}">
                            <g:set var="suffix" value="${g.message(code: unitWeight)}"/>
                        </g:if>
                        <g:elseif test="${shippingPolicy.policyType == DomainConstants.SHIPPING_POLICY_TYPE.SHIP_BY_PRICE}">
                            <g:set var="suffix" value="${currencySymbol}"/>
                        </g:elseif>
                        <g:else>
                            <g:set var="suffix" value="${""}"/>
                        </g:else>
                        <g:if test="${shippingPolicy.policyType != DomainConstants.SHIPPING_POLICY_TYPE.FLAT_RATE && shippingPolicy.policyType != DomainConstants.SHIPPING_POLICY_TYPE.FREE_SHIPPING && shippingPolicy.policyType != DomainConstants.SHIPPING_POLICY_TYPE.API}">
                            <span class="tool-icon info" title='<g:each in="${shippingPolicy.conditions}" var="condition">
                                <g:set var="from" value="${isByWeight ? MassConversions.convertSIToMass(unitWeight, condition.fromAmount).toWeight(false) : condition.fromAmount}"/>
                                <g:set var="to" value="${isByWeight ? MassConversions.convertSIToMass(unitWeight, condition.toAmount).toWeight(false) : condition.toAmount}"/>
                                <g:set var="packet" value="${isByWeight && condition.packetWeight ? MassConversions.convertSIToMass(unitWeight, condition.packetWeight)?.toWeight(false) : ""}"/>
                                <span>${from?.toAdminPrice() + " " + suffix + " - " + to?.toAdminPrice() + " " + suffix}</span><br>
                                <span class="title">${condition.getDisplayShippingCost(true) + " " + condition.getDisplayHandlingCost(true)}</span><br>
                            </g:each>'></span>
                        </g:if>
                    </td>
                    <td class="select-column">
                        <button class="submit-button add-rule" entity-rule_id="${rule.id}"><g:message code="select"/></button>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="button-line">
        <button type="button" class="submit-button create-rule"><g:message code="create.new.rule"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>