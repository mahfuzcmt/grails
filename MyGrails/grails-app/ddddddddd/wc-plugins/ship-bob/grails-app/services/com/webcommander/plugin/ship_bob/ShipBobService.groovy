package com.webcommander.plugin.ship_bob

import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.models.AddressData
import com.webcommander.models.Cart
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.HttpUtil
import com.webcommander.webcommerce.Address
import com.webcommander.webcommerce.Order
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders

@Transactional
class ShipBobService {
    final static API_URL = "http://shipbobapiv2.shipbob.com/api/"

    private static ShipBobService _instance
    static ShipBobService getInstance() {
        return _instance ?: (_instance = Holders.applicationContext.getBean(ShipBobService))
    }

    static {
        AppEventManager.on("paid-for-cart", { Collection<Cart> carts ->
            carts.each {
                instance.placeNewWebOrder(it.orderId)
            }
        })
        AppEventManager.on("paid-for-order", { Order order ->
            instance.placeNewWebOrder(order.id)
        })
    }

    void placeNewWebOrder(Long orderId) {
        try {
            Order order = Order.get(orderId)
            Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIP_BOB)
            ShipBobTrack track
            if(configs.is_enabled != "true" || order.shipping == null || (track = ShipBobTrack.findByOrder(order))) { return }
            Address shippingAddress = order.shipping
            Map data = [
                    UserToken: configs.api_key,
                    Name: shippingAddress.firstName + (shippingAddress.lastName ? (" " + shippingAddress.lastName) : ""),
                    StreetAddressLine1: shippingAddress.addressLine1,
                    StreetAddressLine2: shippingAddress.addressLine2 ?: "",
                    City: shippingAddress.city ?: "",
                    State: shippingAddress.state?.name ?: "",
                    Country: shippingAddress.country.code,
                    Zipcode: shippingAddress.postCode,
                    Email: shippingAddress.email,
                    PhoneNumber: shippingAddress.phone,
                    ShippingOption: configs.shipping_option.toInteger(1),
                    InventoryDetails: []
            ]
            Boolean hasShipable = false
            order.items.each {
                if(!it.isShippable) { return }
                String sku = it.itemNumber
                if(sku == null) {
                    throw new ApplicationRuntimeException("item.not.available", [it.productName])
                }
                hasShipable = true
                Map itemData = [
                        "Sku": sku,
                        "Name": it.productName,
                        "Quantity": it.quantity
                ]
                data.InventoryDetails.add(itemData)
            }
            if(!hasShipable) { return }
            String responseText = HttpUtil.doPostRequest(API_URL + "Orders/PlaceNewWebOrder", (data as JSON).toString(), ['Content-Type' : 'application/json'])
            Map response = JSON.parse(responseText)
            if(response.Success == true) {
                new ShipBobTrack(order: order, payload: response.PayLoad).save()
            }
        } catch (ApplicationRuntimeException ex){
            log.error(ex.message)
        } catch(Throwable ignore) {
            log.error(ignore.message)
        }

    }
}
