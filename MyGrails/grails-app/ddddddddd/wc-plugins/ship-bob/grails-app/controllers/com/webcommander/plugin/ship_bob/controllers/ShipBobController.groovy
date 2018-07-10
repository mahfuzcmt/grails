package com.webcommander.plugin.ship_bob

import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.RequiresAdmin
import com.webcommander.constants.DomainConstants
import com.webcommander.rest.ApiHelper
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.OrderItem
import com.webcommander.webcommerce.OrderService
import grails.converters.JSON
import grails.util.TypeConvertingMap
import org.apache.commons.httpclient.HttpStatus

class ShipBobController {
    OrderService orderService

    @License(required = "allow_ship_bob_feature")
    @RequiresAdmin
    def config() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIP_BOB);
        render(view: "/plugins/ship_bob/admin/config", model: [configs: configs])
    }

    def trackingImport() {
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIP_BOB);
        ApiHelper.setRequestParams(request, params)
        if(params.UserToken != configs.api_key) {
            response.status = HttpStatus.SC_UNAUTHORIZED
            render([status: "error"] as JSON)
            return
        }
        ShipBobTrack track = ShipBobTrack.findByPayload(params.OrderId.toString())
        if(track == null) {
            response.status = HttpStatus.SC_UNAUTHORIZED
            render([status: "error"] as JSON)
            return
        }
        Order order = track.order
        List shipmentInfo = orderService.getShipmentInfoForOrder(order)
        List orderItem = []
        List quantity = []
        order.items.each {OrderItem item ->
            if(!item.isShippable) { return }
            Integer deliveredQuantity = shipmentInfo.find { it.orderItemId == item.id }?.deliveredQuantity ?: 0
            Integer remainingQuantity = item.quantity - deliveredQuantity
            if(remainingQuantity < 1) { return }
            orderItem.add(item.id)
            quantity.add(remainingQuantity)
        }
        if(orderItem.size()) {
            TypeConvertingMap data = new TypeConvertingMap([
                    method: "others",
                    order: order.id,
                    trackingInfo: "Carrier: " + params.Carrier  + ", TrackingId: " + params.TrackingId,
                    orderItem: orderItem,
                    quantity: quantity,
                    shippingDate: new Date().toDatePickerFormat(true, TimeZone.default)
            ])
            orderService.saveShipment(data)
        }
        response.status = HttpStatus.SC_OK
        render([status: "success"] as JSON)
    }
}
