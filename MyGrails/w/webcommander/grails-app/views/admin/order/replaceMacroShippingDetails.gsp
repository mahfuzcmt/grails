<table style="border-collapse:collapse; width:700px;" border="0" cellspacing="0" cellpadding="0" align="left">
    <tr>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="product.name"/></th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="shipment.method"/> </th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="shipped.quantity"/></th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="track.info"/></th>
    </tr>
    <g:each in="${order.shipments}" var="shipment" status="i">
        <g:each in="${shipment.shipmentItem}" var="item">
            <tr>
                <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${item.orderItem.productName.encodeAsBMHTML()}</td>
                <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="${shipment.method}"/> </td>
                <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${item.quantity}</td>
                <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${shipment.trackingInfo}</td>
            </tr>
        </g:each>
    </g:each>
</table>