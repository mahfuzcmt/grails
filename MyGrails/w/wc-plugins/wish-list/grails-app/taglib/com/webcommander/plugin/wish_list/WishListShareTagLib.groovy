package com.webcommander.plugin.wish_list

import com.webcommander.admin.Customer
import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class WishListShareTagLib {
    static namespace = "wishList"

    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g;
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    com.webcommander.ApplicationTagLib app

    def addToWishListForEditor = { attrs, body ->
        out << body()
        if(!AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.PRODUCT, "add_to_wish_list")?.toBoolean()) {
            return
        }
        out << "<span class='add-to-wish-list button et_pdp_add_to_wish_list' et-category='button' title='${g.message(code: "add.to.wish.list")}' >${g.message(code: "add.to.wish.list")}</span>";
    }

    def addToWishList = { attrs, body ->
        out << body()
        if(!pageScope.config.add_to_wish_list?.toBoolean()) {
            return
        }
        app.enqueueSiteJs(src: "plugins/wish-list/js/shared/wish-list.js", scriptId: "wish-list")
        out << "<span class='add-to-wish-list button et_pdp_add_to_wish_list ${attrs.product.isAvailable ? '':'disabled'}' et-category='button' productId='${attrs.productId ?: attrs.product.id}' title='${g.message(code: "add.to.wish.list")}' >${g.message(code: "add.to.wish.list")}</span>";
    }

    def customerProfileWishLists = { attrs, body ->
        out << body()
        Customer customer = Customer.get(session.customer)
        List<WishList> wishLists = WishList.findAllByCustomer(customer)
        out << g.render(template:  "/plugins/wish_list/site/loadWishList",  model: [wishLists: wishLists])
    }

    def customerProfilePluginsJS = { attrs, body ->
        out << body()
        app.enqueueSiteJs(src: "plugins/wish-list/js/shared/customer-profile.js", scriptId: "wish-list-customer-profile")
    }

    def customerProfileTabBody = { attrs, body ->
        out << body();
        if(pageScope.variables.viewConfig["wish_list"] != "true") {
            return
        }
        app.enqueueSiteJs(src: "plugins/wish-list/js/shared/customer-profile.js", scriptId: "wish-list-customer-profile")
        out << '<div id="bmui-tab-wish-list"></div>'
    }

    def productWidgetConfig = { attr, body ->
        String check = pageScope.config.add_to_wish_list?.toBoolean() ? " checked" : ""
        out << body()
        out << '<div class="sidebar-group">'
        out << '<div class="sidebar-group-body">'
        out << '<input type="checkbox" class="single" name="add_to_wish_list" value="true" uncheck-value="false"' + check + '>'
        out << '<label>' + g.message(code: "add.to.wish.list") + '</label>'
        out << '</div>'
        out << '</div>'
    }

    def productPageConfig = { attr, body ->
        String check = pageScope.productSettings.add_to_wish_list?.toBoolean() ? " checked" : ""
        out << '<div class="form-row">'
        out << '<input type="checkbox" class="single" name="product.add_to_wish_list" value="true" uncheck-value="false"' + check + '>'
        out << '<span>' + g.message(code: "add.to.wish.list") + '</span>'
        out << '</div>'
        out << body()
    }

    def categoryPageConfig = { attr, body ->
        String check = pageScope.config.add_to_wish_list?.toBoolean() ? " checked" : ""
        out << '<div class="form-row">'
        out << '<input type="checkbox" class="single" name="category_page.add_to_wish_list" value="true" uncheck-value="false"' + check + '>'
        out << '<span>' + g.message(code: "add.to.wish.list") + '</span>'
        out << '</div>'
        out << body()
    }

    def configSearchPage = { attr, body ->
        String check = pageScope.searchConfig.add_to_wish_list?.toBoolean() ? " checked" : ""
        out << '<div class="form-row">'
        out << '<input type="checkbox" class="single" name="search_page.add_to_wish_list" value="true" uncheck-value="false"' + check + '>'
        out << '<span>' + g.message(code: "add.to.wish.list") + '</span>'
        out << '</div>'
        out << body()
    }

    def customerProfilePageOverviewSettings = {attr, body ->
        out << body()
        out << """<div class="form-row">
                <input type="checkbox" class="single" name="${pageScope.customerProfile}.add_to_wish_list" value="true"
                       uncheck-value="false" ${attr.config["add_to_wish_list"] == "true" ? "checked" : ""}>
                <span>${g.message code: "add.to.wish.list" }</span>
            </div>"""
    }

    def myAccountPageSetting = { attrs, body ->
        out << body()
        Map var = pageScope.variables
        out << """<div class="form-row">
                <input type="checkbox" class="single" name="${var.configType}.wish_list" value="true"
                       uncheck-value="false" ${var.config["wish_list"] == "true" ? "checked" : ""}>
                <span>${g.message code: "my.wishlist"}</span>
            </div>"""
    }

}
