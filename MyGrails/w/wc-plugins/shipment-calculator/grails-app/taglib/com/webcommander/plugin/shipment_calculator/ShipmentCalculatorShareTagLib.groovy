package com.webcommander.plugin.shipment_calculator

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class ShipmentCalculatorShareTagLib {
    static namespace = "shipmentCalculator"
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    def calculatorInCheckout = { attrs, body ->
        out << body()
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "shipment_calculator_checkout_page");
        if (config.toBoolean(false) && params.section == "shipping") {
            out << " &nbsp; &nbsp; &nbsp; <span class='shipment-calculator button et_pdp_shipping_calculator' et-category='button' page='checkout'>${g.message(code: "shipment.calculator")}</span>";
        }
    }

    def calculatorInCart = { attrs, body ->
        out << body()
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "shipment_calculator_cart_details_page");
        if (config.toBoolean(false)) {
            app.enqueueSiteJs(src: "plugins/shipment-calculator/js/shipment-calculator.js", scriptId: "shipment-calculator")
            out << "<a class='shipment-calculator button et_pdp_shipping_calculator' et-category='button' page='cart'>${g.message(code: "shipment.calculator")}</a>";
        }
    }

    def calculatorInAddressEditor = { attrs, body ->
        out << body()
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SHIPPING, "shipment_calculator_checkout_page");
        if (config.toBoolean(false) && params.section == "shipping") {
            out << "&nbsp;&nbsp;<input type='button' class='shipment-calculator button et_pdp_shipping_calculator' et-category='button' page='checkout' value='${g.message(code: "shipment.calculator")}'>";
        }
    }

    def config = { attrs, body ->
        def html = g.include(view: "/plugins/shipment_calculator/admin/config.gsp", model: [shippingSettings: attrs.shippingSettings]).toString();
        out << html;
    }
}
