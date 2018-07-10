package com.webcommander.plugin.referboard

import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.models.Cart
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders

@Transactional
class ReferboardService {
    private static ReferboardService _instance

    static getInstance() {
        return _instance ?: (_instance = Holders.applicationContext.getBean(ReferboardService))
    }

    static {
        AppEventManager.on("order-create order-update", { Long orderId ->
            try {
                instance.saveTrackingInfo(orderId)
            } catch (Exception ignore) {}
        });

        AppEventManager.on("paid-for-cart", { Collection<Cart> carts ->
            carts.each {
                instance.sendOrderToReferboard(it.orderId)
            }
        })

        AppEventManager.on("paid-for-order", { Order order->
            instance.sendOrderToReferboard(order.id)
        })
    }

    void saveTrackingInfo(Long orderId) {
        Map sessionData = AppUtil.session.referboard
        if(sessionData) {
            Order order = Order.get(orderId)
            Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REFERBOARD)
            Map postbackData = [
                'id'            : sessionData['rf_product'],
                'currency'      : AppUtil.baseCurrency.code,
                'cid'           : sessionData['rf_user'],
                'rkey'          : configs.api_key,
                'email'         : order.customer?.userName ?: order.billing.email,
                'buyer_ip'      : sessionData['rf_buyer_ip'] ?: (AppUtil.request.ip),
                'buyer_history' : "0",
                'tid'           : orderId.toString(),
                'extra'         : 'freetext'
            ];
            ReferboardTrackingInfo info = ReferboardTrackingInfo.findOrCreateByOrder(order)
            info.jsonData = postbackData as JSON
            info.save()
        }
    }

    void sendOrderToReferboard(Long orderId) {
        try {
            Order order = Order.get(orderId)
            ReferboardTrackingInfo info = ReferboardTrackingInfo.findByOrder(order)
            if(info) {
                Map referboardData = JSON.parse(info.jsonData)
                referboardData['amount'] = order.grandTotal.toPrice()
                ReferboardLib.sendTransactionData(referboardData)
                info.delete()
            }
        } catch (Exception ignore) {
            log.error(ignore.message)
        }
    }
}
