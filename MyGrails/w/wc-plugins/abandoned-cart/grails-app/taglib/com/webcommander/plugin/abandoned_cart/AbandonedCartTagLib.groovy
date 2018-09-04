package com.webcommander.plugin.abandoned_cart

import com.webcommander.admin.Customer
import com.webcommander.manager.LicenseManager

class AbandonedCartTagLib {
    static namespace = "abandonedCart"

    def customerProfileAbandonedCarts = { attrs, body ->
        out << body()
        if(LicenseManager.isAllowed("product_limit")) {
            app.enqueueSiteJs(src: "plugins/abandoned-cart/js/shared/customer-profile.js", scriptId: 'abandoned-customer-profile')
            Customer customer = Customer.get(session.customer)
            List<AbandonedCart> abandonedCarts = AbandonedCart.findAllByCustomer(customer)
            out << g.render(template: "/plugins/abandoned_cart/site/abandonedCart",  model: [carts: abandonedCarts])
        }
    }

    def customerProfilePluginsJS  = { attrs, body ->
        out << body()
        if(LicenseManager.isAllowed("product_limit")) {
            app.enqueueSiteJs(src: "plugins/abandoned-cart/js/shared/customer-profile.js", scriptId: 'abandoned-customer-profile')
        }
    }

    def customerProfileTabBody = { attrs, body ->
        out << body()
        if(pageScope.variables.viewConfig["abandoned_cart"] != "true") {
            return
        }
        out << '<div id="bmui-tab-abandoned-cart"></div>'
    }

    def myAccountPageSetting = { attrs, body ->
        out << body()
        Map var = pageScope.variables
        out << """<div class="form-row">
                <input type="checkbox" class="single" name="${var.configType}.abandoned_cart" value="true"
                       uncheck-value="false" ${var.config["abandoned_cart"] == "true" ? "checked" : ""}>
                <span>${g.message code: "my.abandoned.cart"}</span>
            </div>"""
    }
}
