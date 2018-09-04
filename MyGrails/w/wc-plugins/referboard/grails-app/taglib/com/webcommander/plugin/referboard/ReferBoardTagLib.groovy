package com.webcommander.plugin.referboard

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Payment

class ReferBoardTagLib {
    static namespace = "referBoard"

    def layoutHead = { attr, body ->
       try {
           out << body()
           Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.REFERBOARD)
           if(configs.is_enabled != "true" ) {
               return
           }
           if(request.isAutoPage != true && (!params.page || params.page == AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.GENERAL, "landing_page"))) {
               out << "<script type='text/javascript' src='//www.referboard.com/js/referButton/cookie_track.js?api=${configs.api_key}'></script> "
           }
           if(request.isAutoPage && request.page.name == DomainConstants.AUTO_GENERATED_PAGES.PAYMENT_SUCCESS_PAGE) {
               Map sessionData = AppUtil.session.referboard
               AppUtil.session.referboard = null
               Payment payment = pageScope.payment
               if(!sessionData || payment.status != DomainConstants.PAYMENT_STATUS.SUCCESS) { return }
               Order order = payment.order;
               Map postbackData = [
                       'id'            : sessionData['rf_product'],
                       'currency'      : AppUtil.baseCurrency.code,
                       'cid'           : sessionData['rf_user'],
                       'rkey'          : configs.api_key,
                       'email'         : order.customer?.userName ?: order.billing.email,
                       'buyer_ip'      : sessionData['rf_buyer_ip'] ?: (AppUtil.request.ip),
                       'tid'           : order.id,
                       'extra'         : 'freetext'
               ];
               out << g.include(view:  "/plugins/referboard/_transactionConfirm.gsp", model: [postbackData: postbackData, order: order])
           }
       } catch (Exception ignore) {}
    }
}
