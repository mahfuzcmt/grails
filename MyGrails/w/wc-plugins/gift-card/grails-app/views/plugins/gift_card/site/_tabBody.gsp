<%@ page import="com.webcommander.util.AppUtil" %>
<g:set var="currencySymbol" value="${AppUtil.baseCurrency.symbol}"/>
<div class='gift-card-short-info'>
    <div class="check-panel">
        <div class="form-row required">
            <label class="title"><g:message code="gift.card.check.balance.description"/> <span class="tool-tip gift-card">(?)</span></label>
            <input type="text" class="medium gift-card-code" name="code" validation="skip@if{self::hidden} required" placeholder="${g.message(code: 'enter.gift.card.code')}">
            <span class='button check-balance'><g:message code="check.balance"/></span>
        </div>
    </div>
    <div class="table-head">
        <span class="title"><g:message code="gift.card.history"/></span>
    </div>
    <div class="usage-details">
        <g:if test="${cardUsageList}">
            <table>
                <tr>
                    <th><g:message code="order.no"/></th>
                    <th><g:message code="usage.amount"/></th>
                    <th><g:message code="usage.date"/></th>
                    <th><g:message code="balance"/></th>
                </tr>
                <g:each in="${cardUsageList}" var="cardUsage">
                    <tr>
                        <td>${cardUsage.giftCard.code}</td>
                        <td>${currencySymbol}${cardUsage.amount.toPrice()}</td>
                        <td>${cardUsage.created.toSiteFormat(false, false, session.timezone)}</td>
                        <td>${currencySymbol}${cardUsage.giftCard.getAvailableBalance().toPrice()}</td>
                    </tr>
                </g:each>
            </table>
        </g:if>
        <g:else>
            <span class="no-use"><g:message code="you.have.not.used.any.gift.card.yet"/></span>
        </g:else>
    </div>
</div>