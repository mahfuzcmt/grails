package com.webcommander.plugin.gift_registry

import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class GiftRegistryTagLib {
    static namespace = "giftRegistry"

    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    def addToGiftRegistryForEditor = {attrs, body ->
        out << body()
        Map config = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT)
        if(!config.add_to_gift_registry?.toBoolean() || !config.add_to_cart?.toBoolean()) {
            return
        }
        out << "<span class='add-to-gift-registry button'  title='${g.message(code: "add.to.gift.registry")}' >${g.message(code: "add.to.gift.registry")}</span>";
    }

    def addToGiftRegistry = {attrs, body ->
            out << body()
            if(!pageScope.config.add_to_gift_registry?.toBoolean() || !pageScope.config.add_to_cart?.toBoolean()) {
                return
            }
            app.enqueueSiteJs(src: "plugins/gift-registry/js/gift-registry.js", scriptId: "gift-registry")
            out << "<span class='add-to-gift-registry button ${attrs.available ? '':'disabled'}' productId='${attrs.productId}' title='${g.message(code: "add.to.gift.registry")}' >${g.message(code: "add.to.gift.registry")}</span>";
        }

    def customerProfileGiftRegistry = { attrs, body ->
        out << body()
        Customer customer = Customer.get(session.customer)
        List<GiftRegistry> giftRegistries = GiftRegistry.findAllByCustomer(customer)
        out << g.render(template:  "/plugins/gift_registry/site/loadGiftRegistry", model: [giftRegistries: giftRegistries])
    }

    def customerProfilePluginsJS = { attrs, body ->
        out << body()
        app.enqueueSiteJs(src: "plugins/gift-registry/js/gift-registry-customer-profile.js", scriptId: "gift-registry-customer-profile")

    }

    def customerProfileTabBody = { attrs, body ->
        out << body();
        if(pageScope.variables.viewConfig["gift_registry"] != "true") {
            return
        }
        app.enqueueSiteJs(src: "plugins/gift-registry/js/gift-registry-customer-profile.js", scriptId: "gift-registry-customer-profile")
        out << '<div id="bmui-tab-gift-registry"></div>'
    }

    def renderGiftItemData = {attrs, body ->
        out << body();
        if (request.gift_registry_page) {
            out << "<input type='hidden' name='giftItemId' value='${attrs.product.giftItem.id}'>";
        }
    }

    def productPageConfig = { attr, body ->
        String check = pageScope.productSettings.add_to_gift_registry?.toBoolean() ? " checked" : ""
        out << '<div class="form-row">'
        out << '<input type="checkbox" class="single" name="product.add_to_gift_registry" value="true" uncheck-value="false"' + check + '>'
        out << '<span>' + g.message(code: "add.to.gift.registry") + '</span>'
        out << '</div>'
        out << body()
    }

    def myAccountPageSetting = { attrs, body ->
        out << body()
        Map var = pageScope.variables
        out << """<div class="form-row">
                <input type="checkbox" class="single" name="${var.configType}.gift_registry" value="true"
                       uncheck-value="false" ${var.config["gift_registry"] == "true" ? "checked" : ""}>
                <span>${g.message code: "gift.registry"}</span>
            </div>"""
    }

}
