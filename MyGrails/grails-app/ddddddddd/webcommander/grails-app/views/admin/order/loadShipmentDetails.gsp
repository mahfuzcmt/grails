<div class="header">
    <span class="item-group entity-count title">
        <g:message code="shipment.details"/> (<span class="count">${count}</span>)
    </span>
    <div class="toolbar toolbar-right">
        <div class="tool-group action-tool action-menu collapsed">
            <span class="tool-text"><g:message code="actions"/></span><span class="action-dropper"></span>
        </div>
        <div class="tool-group">
            <span class="toolbar-item switch-menu collapsed"><i></i></span>
            <span class="toolbar-item reload" title="<g:message code="reload"/>"><i></i></span>
        </div>
    </div>
</div>
<div class="app-tab-content-container">
    <table class="content">
        <colgroup>
            <col style="width: 25%">
            <col style="width: 25%">
            <col style="width: 30%">
            <col style="width: 20%">
            <col style="width: 25%">
        </colgroup>
        <tr>
            <th><g:message code="name"/></th>
            <th><g:message code="shipment.method"/></th>
            <th><g:message code="shipping.date"/></th>
            <th><g:message code="shipped.quantity"/></th>
            <th><g:message code="track.info"/></th>
        </tr>
        <g:each in="${shipmentItems}" var="item" status="i">
            <tr>
                <td>${item.orderItem.productName.encodeAsBMHTML()}</td>
                <td><g:message code="${item.shipment.method}"/></td>
                <td>${item.shipment.shippingDate.toAdminFormat(true, false, session.timezone)}</td>
                <td>${item.quantity}</td>
                <td>${item.shipment.trackingInfo}</td>
            </tr>
        </g:each>
    </table>
</div>
<div class="footer">
    <ui:perPageCountSelector/>
    <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
</div>