package com.webcommander.plugin.loyalty_point.controllers.site

import com.webcommander.admin.Customer
import com.webcommander.authentication.annotations.License
import com.webcommander.authentication.annotations.RequiresCustomer
import com.webcommander.manager.CartManager
import com.webcommander.models.CartItem
import com.webcommander.models.DownloadableProductInCart
import com.webcommander.models.ProductInCart
import com.webcommander.plugin.loyalty_point.LoyaltyPointService
import com.webcommander.plugin.loyalty_point.PointHistory
import com.webcommander.plugin.loyalty_point.constants.DomainConstants
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Product
import grails.converters.JSON

class LoyaltyPointController {
    LoyaltyPointService loyaltyPointService;

    @RequiresCustomer
    def customerProfile() {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        Customer customer = Customer.proxy(session.customer);
        render(template:  "/plugins/loyalty_point/site/customerProfile", model: [configs: configs, totalPoints: loyaltyPointService.getCustomerLoyaltyPoint(customer)]);
   }

    @RequiresCustomer
    def referral() {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT);
        Customer customer = Customer.proxy(session.customer);
        render(template: "/plugins/loyalty_point/site/referral", model: [configs: configs, totalPoints: loyaltyPointService.getCustomerLoyaltyPoint(customer), customer: customer]);
    }

    @RequiresCustomer
    def inviteFriend() {
        Customer customer = Customer.proxy(session.customer);
        render(view: "/plugins/loyalty_point/site/inviteFriend", model: [customer: customer])
    }

    @RequiresCustomer
    def sendInvite() {
        Customer customer = Customer.proxy(session.customer);
        try {
            loyaltyPointService.sendMailToFriend(params, customer)
            def successPopup = g.include(view: "/plugins/loyalty_point/site/inviteFriendSuccess.gsp", model: [email: params.receiver])
            render([status: "success", html: successPopup.toString()] as JSON);
        } catch (e) {
            render([status: "error", message: g.message(code: "email.could.not.be.sent")] as JSON);
        }
    }

    @RequiresCustomer
    def history() {
        Customer customer = Customer.proxy(session.customer);
        List<PointHistory> historyList = PointHistory.where {customer == customer}.list();
        render(view: "/plugins/loyalty_point/site/history", model: [historyList: historyList, totalPoints: loyaltyPointService.getCustomerLoyaltyPoint(customer)]);
    }

    @RequiresCustomer
    @License(required = "allow_loyalty_program_feature")
    def claimRewards() {
        Customer customer = Customer.proxy(session.customer);
        render(view: "/plugins/loyalty_point/site/claim", model: [totalPoints:loyaltyPointService.getCustomerLoyaltyPoint(customer)]);
    }

    @RequiresCustomer
    @License(required = "allow_loyalty_program_feature")
    def convert(){
        render loyaltyPointService.convert(params) as JSON
    }

    def validateReferralCode() {
        render (loyaltyPointService.validateReferralCode(params) as JSON)
    }

    def updateCartLoyaltyPoint() {
        def configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.LOYALTY_POINT)
        if (configs.is_enabled == "true" && configs.earning_enabled == "true" && configs.show_in_cart == "true") {
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
            String message = g.message(code: "loyalty.point.cart.details.text", args: [totalLoyaltyPoint])
            render([message: message] as JSON)
        }
    }
}
