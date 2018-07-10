package com.webcommander.plugin.save_cart

import com.webcommander.constants.DomainConstants
import com.webcommander.manager.LicenseManager
import com.webcommander.util.AppUtil


class SaveCartTagLib {
    static namespace = "saveCart"
    SaveCartService saveCartService

    def saveCartButtonInCart = { attrs, body ->
        out << body()
        def config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SAVE_CART, "enabled");
        if (config.toBoolean(false) && (!LicenseManager.isProvisionActive() || LicenseManager.license('allow_save_cart_feature'))) {
            app.enqueueSiteJs(src: "plugins/save-cart/js/shared/cart-page.js", scriptId: "save-cart-page")
            out << "<a href='${app.relativeBaseUrl()}saveCart/beforeSave' ><span class='save-cart button et_cartp_save_cart' et-category='button' page='checkout'>${g.message(code: "save.cart")}</span></a>";
        }
    }

    def customerProfileSaveCarts = { Map attrs, body ->
        out << body()
        if ((!LicenseManager.isProvisionActive() || LicenseManager.license('allow_save_cart_feature'))) {
            app.enqueueSiteJs(src: "plugins/save-cart/js/shared/save-cart-customer-profile.js", scriptId: "save-cart-customer-profile")
            if(params.clearSaveOperation) {
                session.save_cart = false
            }
            if(session.save_cart){
                out << g.render(template: "/plugins/save_cart/site/saveCartInit",  model: [])
            }
            else {
                List<SavedCart> carts = saveCartService.getCarts(session.customer)
                out << g.render(template: "/plugins/save_cart/site/loadSavedCart",  model: [carts: carts])
            }
        }
    }

    def customerProfilePluginsJS = { Map attrs, body ->
        out << body()
        if ((!LicenseManager.isProvisionActive() || LicenseManager.license('allow_save_cart_feature'))) {
            app.enqueueSiteJs(src: "plugins/save-cart/js/shared/save-cart-customer-profile.js", scriptId: "save-cart-customer-profile")
        }
    }

    def customerProfileTabBody = { Map attrs, body ->
        out << body()
        if(pageScope.variables.viewConfig["saved_cart"] != "true") {
            return
        }
        def configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.SAVE_CART);
        if (configs.enabled == "true" && (!LicenseManager.isProvisionActive() || LicenseManager.license('allow_save_cart_feature'))) {
            out << "<div id='bmui-tab-save-cart'></div>"
        }
    }

    def myAccountPageSetting = { attrs, body ->
        out << body()
        Map var = pageScope.variables
        out << """<div class="form-row">
                <input type="checkbox" class="single" name="${var.configType}.saved_cart" value="true"
                       uncheck-value="false" ${var.config["saved_cart"] == "true" ? "checked" : ""}>
                <span>${g.message code: "my.saved.cart"}</span>
            </div>"""
    }
}
