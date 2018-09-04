<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.NamedConstants" %>
<g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
<div class="rate-body">
    <form class="search-form search-block">
        <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
    </form>
    <div class="body rate-table">
        <table>
            <colgroup>
                <col class="rate-column">
                <col class="cost-column">
                <col class="cost-column">
                <col class="actions-column">
            </colgroup>
            <tbody>
            <g:set var="policyType" value="${NamedConstants.SHIPPING_POLICY_TYPE_MESSAGE_KEYS}"/>
            <g:each in="${rates}" var="rate">
                <tr>
                    <td class="thumb-left">
                        <span class="rate-title">${rate.name.encodeAsBMHTML()}</span><br>
                        <span class="blocklist-subitem-summary-view"><g:message code="${policyType[rate.policyType]}"/></span>
                    </td>
                    <td class="cost-column shipping-cost">${rate.conditions[0]?.getDisplayShippingCost(true)}</td>
                    <td class="cost-column handling-cost">${rate.conditions[0]?.getDisplayHandlingCost()}</td>
                    <td class="select-column">
                        <button class="submit-button add-rate select-item" type="button" entity-rate_id="${rate.id}" entity-rate_name="${rate.name.encodeAsBMHTML()}"><g:message code="select"/></button>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="button-line">
        <button type="submit" class="submit-button create-new-rate"><g:message code="create.new.rate"/></button>
        <button type="button" class="cancel-button"><g:message code="cancel"/></button>
    </div>
</div>