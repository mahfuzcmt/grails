<%@ page import="com.webcommander.util.AppUtil; com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="orders"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-header" style="display: none">
            <g:select class="action-on-selection" name="action" from="${[ g.message(code: "with.selected"), g.message(code: "cancel"), g.message(code: "send.invoice")]}" keys="['', 'cancel', 'sendInvoice']"/>
        </div>
        <div class="tool-group chosen-wrapper">
            <ui:namedSelect prepend="${["": g.message(code: "none")]}" key="${NamedConstants.ORDER_STATUS}" class="order-status small" />
        </div>
        <form class="search-form tool-group">
            <input type="text" class="search-text" placeholder="<g:message code="search"/>"><button type="submit" class="icon-search"></button>
        </form>
        <div class="tool-group">
            <span class="toolbar-item add-filter" title="<g:message code="advance.search"/>"><i></i></span>
            <span class="toolbar-item remove-filter disabled" title="<g:message code="remove.search"/>"><i></i></span>
        </div>
        <div class="tool-group action-tool action-menu collapsed">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper"></span>
        </div>
        <div class="tool-group toolbar-btn create"><i></i><g:message code="create"/></div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content order-app-view">
        <colgroup>
            <col class="select-column">
            <col class="collapse-controller-column">
            <col style="width: 140px">
            <col>
            <col style="width: 20%">
            <col style="width: 10%">
            <col style="width: 10%">
            <col style="width: 10%">
            <col style="width: 10%">
            <col style="width: 7%">
        </colgroup>
        <tr>
            <th></th>
            <th class="select-column"><input type="checkbox" class="check-all multiple"></th>
            <th><g:message code="order.id"/></th>
            <th><g:message code="customer.name" args="${[(AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.ADMINISTRATION, "ecommerce") == 'true')?"Customer":"Member"]}"/></th>
            <th><g:message code="order.date"/></th>
            <th><g:message code="order.total"/></th>
            <th class="status-column"><g:message code="order.status"/></th>
            <th class="status-column"><g:message code="shipment.status"/></th>
            <th class="status-column"><g:message code="payment.status"/></th>
            <th class="actions-column"><g:message code="actions"/></th>
        </tr>
        <g:if test="${orders}">
            <g:each in="${orders}" var="order">
                <tr>
                    <td><span class="tool-icon collapsed toggle-cell"></span></td>
                    <td class="select-column"><input type="checkbox" class="multiple" entity-id="${order.id}"></td>
                    <td>${order.id}</td>
                    <td>${order.customerName.encodeAsBMHTML()}</td>
                    <td>${order.created.toAdminFormat(true, false, session.timezone)}</td>
                    <td>${order.grandTotal.toAdminPrice()}</td>
                    <td class="status-column order-status">
                        <span class="status ${order.orderStatus == 'completed' ? 'positive' : order.orderStatus == 'cancelled' ? 'negative' : 'diplomatic'}" title="${g.message(code: order.orderStatus )}"></span>
                    </td>
                    <td class="status-column"><span class="status ${order.shippingStatus == 'completed' ? 'positive' : order.shippingStatus == 'awaiting' ? 'negative' : 'diplomatic'}" title="${g.message(code: order.shippingStatus)}"> </span></td>
                    <g:set var="paymentStatus" value="${[unpaid: "negative", partial: "diplomatic", paid: "positive"]}"/>
                    <td class="status-column"><span class="status ${paymentStatus[order.paymentStatus]}" title="${g.message(code: order.paymentStatus)}"></span></td>
                    <td class="actions-column">
                        <span class="action-navigator collapsed ${order.orderStatus}" entity-id="${order.id}" entity-owner_id="${order.createdBy?.id}"></span>
                    </td>
                </tr>
                <tr class="toggle-table-row" style="display: none">
                    <td colspan="10">
                        <g:include view="/admin/order/orderDetailsRow.gsp" model="[order: order]"/>
                    </td>
                </tr>
            </g:each>
        </g:if>
        <g:else>
            <tr class="table-no-entry-row">
                <td colspan="9"><g:message code="no.order.created"/></td>
            </tr>
        </g:else>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" max="${params.max}" offset="${params.offset}"></paginator>
</div>