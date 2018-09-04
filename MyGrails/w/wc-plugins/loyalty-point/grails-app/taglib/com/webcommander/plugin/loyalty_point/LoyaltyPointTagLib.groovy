package com.webcommander.plugin.loyalty_point

import com.webcommander.admin.Customer
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.LicenseManager
import com.webcommander.models.Cart
import com.webcommander.models.CartItem
import com.webcommander.models.DefaultPaymentMetaData
import com.webcommander.models.DownloadableProductInCart
import com.webcommander.models.ProductData
import com.webcommander.models.ProductInCart
import com.webcommander.plugin.loyalty_point.constants.DomainConstants
import com.webcommander.plugin.loyalty_point.constants.NamedConstants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.Product;

class LoyaltyPointTagLib {
    LoyaltyPointService loyaltyPointService;
    static String loyalty_point_js = "plugins/loyalty-point/js/site-js/loyalty-point.js"

    static namespace = "loyaltyPoint"

    def editLoyaltyPoint = { Map attrs, body ->
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        LoyaltyPoint loyaltyPoint = attrs.loyaltyPoint ?: new LoyaltyPoint(product: attrs.target == "product" ? Product.proxy(attrs.targetId) : null, category: attrs.target == "category" ? Category.proxy(attrs.targetId) : null);
        out <<  "<input type='hidden' name='loyaltyPoint.target' value='${attrs.target}'>" +
                "<div class='form-row mandatory'>" +
                "<label>${g.message(code: "loyalty.points")}<span class='suggestion'> e.g. 100</span></label> " +
                "<input type='text' class='medium' name='loyaltyPoint.point' value='${loyaltyPoint.point}' restrict='numeric' validation='required max[999999] digits'>" +
                "</div>";
    }

    def productWidget = { Map attrs, body ->
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
            Long detailsId = HookManager.hook("resolveLoyaltyPointVariation", 0, attrs.productData)
            def loyaltyPoint = LoyaltyPoint.findByVariationDetailsId(detailsId)
            Long point= loyaltyPoint ? loyaltyPoint.point : 0
            out << " <div class='info-row loyalty-point' page='product'>" +
                    "<label>${g.message(code: 'loyalty.point')}</label> " +
                    "<span class='value'>${point ?: (attrs.productId ? loyaltyPointService.findLoyaltyPoint(Product.proxy(attrs.productId)) : "")}</span>" +
                    "</div>";
        }
    }

    def convertTo = {Map attrs, body ->
        Map convertOptions = loyaltyPointService.getConversionOptions();
        attrs.from = convertOptions.values().message_key.collect{g.message(code: it)};
        attrs.keys = convertOptions.keySet();
        out << g.select(attrs);
    }

    def productLoyaltyPointImageView = { Map attrs, body ->
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
            def productData = attrs.product;
            Product product = Product.proxy(productData.id);
            Long point = loyaltyPointService.findLoyaltyPoint(product);
            out << body()
            out << "<span class='loyalty-point'>${g.message(code: "loyalty.point")} : ${point}</span>"
        } else {
            out << body()
        }
    }

    def productEditField = { Map attrs, body ->
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.point_policy != DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
            Product product = Product.proxy(attrs.productId);
            LoyaltyPoint loyaltyPoint = LoyaltyPoint.where { product == product }.get()
            out << editLoyaltyPoint([loyaltyPoint: loyaltyPoint, target: "product", targetId: attrs.productId]);
        }
    }

    def variationProductEditField = { Map attrs, body ->
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true") {
            Product product = Product.proxy(attrs.productId)
            Map detailsMap = attrs.detailsMap;
            def loyaltyPoint = LoyaltyPoint.findByVariationDetailsId(detailsMap.id) ?: new LoyaltyPoint();
            if (configs.is_enabled == "true" && configs.point_policy != DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
                out <<  "<div class='form-row mandatory with-check-box'>" +
                        "<label>${g.message(code: "loyalty.points")}<span class='suggestion'> e.g. 100</span></label>" +
                        "<input type='text' class='medium' ${loyaltyPoint.point ? '' : 'disabled'} name='loyaltyPoint' " +
                        "value='${loyaltyPoint.point}' restrict='decimal' validation='number required max[999999]'> " +
                        "<input type='checkbox' class='multiple active-check' value='true' ${loyaltyPoint.point ? 'checked' : ''}>" +
                        "</div>";
            }
        }
    }

    def categoryEditField = { Map attrs, body ->
        out << body()
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.point_policy != DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
            Category category = Category.proxy(attrs.categoryId);
            LoyaltyPoint loyaltyPoint = LoyaltyPoint.where { category == category }.get();
            out << '<div class="section-separator"></div><div class="form-section no-min-height"><div class="form-section-info"><h3>' +
                    g.message(code: "loyalty.point") + '</h3></div><div class="form-section-container">'
            out << editLoyaltyPoint([loyaltyPoint: loyaltyPoint, target: "category", targetId: attrs.categoryId]);
            out << '</div></div>'
        }
    }

    def namedSelection = { Map attrs, body ->
        Map target = attrs.target;
        Map key = [:];
        target.each {
            key[it.value] = it.value;
        }
        attrs.remove("target");
        attrs.put("key", key)
        out << ui.namedSelect(attrs);
    }

    def customerProfileLoyaltyPoints= { Map attrs, body ->
        out << body()
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
        if (configs.is_enabled == "true" && (!LicenseManager.isProvisionActive() || LicenseManager.license('allow_loyalty_program_feature'))) {
            app.enqueueSiteJs(src: loyalty_point_js, scriptId: "loyalty-point")
            Customer customer = Customer.proxy(session.customer);
            out << g.render(template: "/plugins/loyalty_point/site/customerProfile", model: [configs: configs, totalPoints: loyaltyPointService.getCustomerLoyaltyPoint(customer)])
        }
    }
    def customerProfilerReferral= { Map attrs, body ->
        out << body()
        if ((!LicenseManager.isProvisionActive() || LicenseManager.license('allow_loyalty_program_feature'))) {
            def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
            Customer customer = Customer.proxy(session.customer)
            out << g.render(template: "/plugins/loyalty_point/site/referral", model: [configs: configs, totalPoints: loyaltyPointService.getCustomerLoyaltyPoint(customer), customer: customer])
        }
    }

    def customerProfilePluginsJS = { Map attrs, body ->
        out << body()
        if ((!LicenseManager.isProvisionActive() || LicenseManager.license('allow_loyalty_program_feature'))) {
            app.enqueueSiteJs(src: loyalty_point_js, scriptId: "loyalty-point")
        }
    }

    def customerProfileTabBody = { Map attrs, body ->
        out << body()
        if(pageScope.variables.viewConfig["loyalty_point"] != "true") {
            return
        }
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && (!LicenseManager.isProvisionActive() || LicenseManager.license('allow_loyalty_program_feature'))) {
            out << "<div id='bmui-tab-loyalty-point'></div>"
        }
    }

    def beforePaymentMessage = { Map attrs, body ->
        out << body()
        if(!session.customer || (LicenseManager.isProvisionActive() && !LicenseManager.license("allow_loyalty_program_feature"))) {
            return
        }
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.earning_enabled == "true" && configs.show_in_cart == "true") {
            app.enqueueSiteJs(src: loyalty_point_js, scriptId: "loyalty-point");
            List cartList = CartManager.getCart(session.id, false)?.cartItemList
            Long totalLoyaltyPoint = 0;
            cartList.each { CartItem cartItem ->
                if(cartItem.object instanceof ProductInCart || cartItem.object instanceof DownloadableProductInCart) {
                    Product product = Product.proxy(cartItem.object.product.id);
                    totalLoyaltyPoint += (loyaltyPointService.findPointByVariation(product, cartItem) ?: loyaltyPointService.findLoyaltyPoint(product)) * cartItem.quantity;
                } else if(configs.point_policy == DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
                    totalLoyaltyPoint += cartItem.baseTotal * cartItem.quantity;
                }
            }
            out << "<div class='loyalty-point'>"
            out << g.message(code: "loyalty.point.cart.details.text", args: [totalLoyaltyPoint])
            out << "</div>"
        }
    }

    def paymentSuccessAfterTable = { Map attrs, body ->
        if(!session.customer){
            return
        }
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.earning_enabled == "true") {
            Order order = attrs.order;
            PointHistory orderHistory = PointHistory.where {
                type == NamedConstants.POINT_HISTORY_TYPE.ORDER
                comment == String.valueOf(order.id)
            }.get();
            if(orderHistory){
                out << "<div class='loyalty-point'>" +
                        g.message(code: "loyalty.point.payment.success.text", args: [orderHistory.pointCredited]) +
                        "</div>"
            }
        }
    }

    def bulkEditAdvancedColGroup = { Map attrs, body ->
        out << body()
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if(configs.is_enabled == "true" && configs.point_policy != DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
            Integer width = 15
            List colWidth = pageScope.colWidth
            pageScope.colWidth.add(width)
            out << "<col style='width: ${pageScope.colWidth[colWidth.size()-1]}%'>"
        }
    }

    def bulkEditAdvancedHeader = { Map attrs, body ->
        out << body()
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.point_policy != DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
            out << "<th>${g.message(code: "loyalty.point")}</th>"
        }
    }

    def bulkEditAdvancedChangeAll = { Map attrs, body ->
        out << body()
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.point_policy != DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
            out << "<td class='editable custom-edit loyalty-point' restrict='numeric' extra-attr='loyalty-point' validation='digits'></td>"
        }
    }

    def bulkEditAdvancedDataColumn = { Map attrs, body ->
        out << body()
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.point_policy != DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
            Product product = attrs.product
            LoyaltyPoint loyaltyPoint = LoyaltyPoint.findByProduct(product);
            Long point = loyaltyPoint ? loyaltyPoint.point : 0
            out << "<td class='editable loyalty-point' restrict='numeric' validation='required digits'>" +
                    "<input type='hidden' name='${product.id}.loyaltyPoint.point' value='${point}'>" +
                    "<span class='value'>${point}</span>" +
                    "</td>" +
                    "<input type='hidden' name='${product.id}.loyaltyPoint.target' value='${attrs.target}'>"
        }
    }

    def customerRegistrationReferralField = { Map attrs, body ->
        out << body();
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);

        if(configs.enable_referral == "true") {
            app.enqueueSiteJs(src: loyalty_point_js, scriptId: "loyalty-point");
            out << """<div class="referral">
            <div class="form-row">
                <label>${g.message(code: 'how.do.you.know.about.us')} :</label>
                ${g.select([class: 'how-do-you-know', name: 'how_do_you_know', from: [ g.message(code: "with.selected"), g.message(code: "friends.family.member"), g.message(code: "facebook"), g.message(code: "google"), g.message(code: "news.paper"), g.message(code: "megazine"), g.message(code: "web.advertise")], keys: [ '', 'friends_family', 'facebook', 'google', 'newspaper', 'megazine', 'web_advertise'] ])}
            </div>

            <div class="form-row">
                <label>${g.message(code: 'reference.number')} :</label>
                <input type="text" class="large" name="reference_number" validation="maxlength[8] minlength[8] hexDigit" maxlength='8' minlength='8' value="${params.referralCode ?: ''}" ${params.referralCode ? 'readonly' : ''}/>
            </div>
        </div>""";
        }
    }

    def checkoutPaymentOption = {attr, body ->
        out << body()
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        Customer customer = Customer.proxy(AppUtil.loggedCustomer)
        if((configs.enable_referral == "false") || !customer) {
            return;
        }
        DefaultPaymentMetaData loyaltyPointPayment = pageScope.defaultPayments.find { it.identifier == "loyaltyPoint" }
        out << g.render(template: "/plugins/loyalty_point/checkout/paymentOption", model: [loyaltyPointPayment: loyaltyPointPayment, availableBalance: loyaltyPointService.getCustomerLoyaltyPointToCurrency(customer)])
    }

    def bulkEditAdvancedCategoryDataColumn = { Map attrs, body ->
        out << body()
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        if (configs.is_enabled == "true" && configs.point_policy != DomainConstants.POINT_POLICY.SPECIFIED_CONVERSION_RATE) {
            Category category = attrs.category
            LoyaltyPoint loyaltyPoint = LoyaltyPoint.findByCategory(category);
            Long point = loyaltyPoint ? loyaltyPoint.point : 0
            out << "<td class='editable loyalty-point' restrict='numeric' validation='required digits'>" +
                    "<input type='hidden' name='${category.id}.loyaltyPoint.point' value='${point}'>" +
                    "<span class='value'>${point}</span>" +
                    "</td>" +
                    "<input type='hidden' name='${category.id}.loyaltyPoint.target' value='${attrs.target}'>"
        }
    }

    def tablePreview = { attrs, body ->
        Map<String, Collection> data = attrs.data
        def rules = data.rules
        if(rules.size() > 0) {
            out << "<table><tr>"
            out << "<th>${g.message(code: 'name')}</th>"
            out << "<th>${g.message(code: 'adjustment')}</th>"
            out << "<th>${g.message(code: 'action')}</th>"
            out << "</tr>"
            rules.each {
                out << "<tr class='rule-data' rule-id=${it.id}>"
                out << "<td>${it.name}</td>"
                out << "<td>${NamedConstants.RULE_TYPE."${it.ruleType}" + it.point}</td>"
                out << "<td class='actions-column'>"
                out << "<span class='tool-icon choose-customer show-customer-group'></span>"
                out << "<span class='tool-icon edit'></span>"
                out << "<span class='tool-icon remove'></span>"
                out << "</td>"
                out << "</tr>"
            }
            out << '</table>'
        }
    }
    def myAccountPageSetting = { attrs, body ->
        out << body()
        Map var = pageScope.variables
        out << """<div class="form-row">
                <input type="checkbox" class="single" name="${var.configType}.loyalty_point" value="true"
                       uncheck-value="false" ${var.config["loyalty_point"] == "true" ? "checked" : ""}>
                <span>${g.message code: "loyalty.point"}</span>
            </div>"""
    }

}
