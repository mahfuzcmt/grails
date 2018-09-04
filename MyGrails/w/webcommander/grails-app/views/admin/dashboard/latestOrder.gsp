<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.util.AppUtil" %>
<div class="dashlet-latest-statistics">
    <g:if test="${DomainConstants.ECOMMERCE_DASHLET_CHECKLIST["latest_order"] && (AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')}">
        <div class="order-data-table">
            <table class="content latest-order">
                <colgroup>
                    <col class="order-no-column">
                    <col class="name-column">
                    <col class="status-column">
                    <col class="date-column">
                    <col class="amount-column">
                </colgroup>
                <tr>
                    <th><g:message code="order.no"/></th>
                    <th><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
                    <th class="status-column"><g:message code="status"/></th>
                    <th><g:message code="order.date"/></th>
                    <th><g:message code="amount"/></th>
                </tr>
                <g:if test="${components.size() > 0}">
                    <g:each in="${components}" var="order">
                        <tr>
                            <td>${order.id}</td>
                            <td>${order.customerName.encodeAsBMHTML()}</td>
                            <g:set var="tooltipNotification" value="${[completed: 'Completed', cancelled: 'Cancelled', pending: 'Pending']}"/>
                            <td class="status-column"><span title="${tooltipNotification[order.orderStatus]}" class="status ${order.orderStatus == DomainConstants.ORDER_STATUS.COMPLETED ? DomainConstants.STATUS.POSITIVE : ( order.orderStatus == DomainConstants.ORDER_STATUS.CANCELLED ? DomainConstants.STATUS.NEGATIVE : DomainConstants.STATUS.DIPLOMATIC )}"></span></td>
                            <td>${order.created.toAdminFormat(true, false, session.timezone)}</td>
                            <td>${AppUtil.baseCurrency.symbol} ${order.grandTotal.toAdminPrice()}</td>
                        </tr>
                    </g:each>
                </g:if>
                <g:else>
                    <tr class="table-no-entry-row"><td colspan="5" ><g:message code="no.order.found"/></td></tr>
                </g:else>
            </table>
        </div>
    </g:if>
</div>