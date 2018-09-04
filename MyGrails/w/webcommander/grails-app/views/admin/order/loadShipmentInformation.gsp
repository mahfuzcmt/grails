<%@ page import="com.webcommander.webcommerce.Product" %>
<div class="header">
    <span class="item-group entity-count title">
        <g:message code="shipment.info"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-tool action-menu collapsed">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper"></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 26%">
            <col style="width: 26%">
            <col style="width: 16%">
            <col style="width: 16%">
            <col style="width: 16%">
        </colgroup>
        <tr>
            <th><g:message code="sku"/></th>
            <th><g:message code="name"/></th>
            <th><g:message code="ordered.quantity"/></th>
            <th><g:message code="shipped.quantity"/></th>
            <th><g:message code="item.total"/></th>
        </tr>
        <g:each in="${orderItem}" var="item" status="i">
            <g:set var="productSku" value="${Product.findByIdAndIsInTrash(item.productId, false)?.sku}"></g:set>
            <tr>
                <td>${productSku.encodeAsBMHTML()}</td>
                <td>${item.productName.encodeAsBMHTML()}</td>
                <td>${item.quantity}</td>
                <g:set var="delivered" value="${shippedQuantity[i] == null ? 0 : shippedQuantity[i].deliveredQuantity }"/>
                <td>${delivered}</td>
                <td>${item.getTotalPriceConsideringConfiguration().toAdminPrice()}</td>
            </tr>
        </g:each>
    </table>
</div>
<g:include view="admin/order/shipmentLog.gsp"/>