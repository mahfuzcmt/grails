package com.webcommander.plugin.subscription_from_checkout.app

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class SubscriptionAppTagLib {
    static namespace = "subscriptionApp"

    def confirmSubscription = { attrs, body ->
        out << body()
        if(AppUtil.getConfig("checkout_page", "enable_newsletter_subscription") == "true") {
            out << "<div><input type='checkbox' class='single' name='subscribe' value='true'> &nbsp;" +
                    " <span><strong>${AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, "subscribe_news_letter")}</strong></span></div>"
        }
    }

    def checkoutPageSettings = { attrs, body ->
        out << body()
        out << "<div class='form-row'><input type='checkbox' class='single' name='checkout_page.enable_newsletter_subscription' value='true' uncheck-value='false' ${AppUtil.getConfig("checkout_page", "enable_newsletter_subscription") == "true" ? "checked" : ""}>" +
                "<span>${g.message(code: "enable.newsletter.subscription")}</span></div>"
    }



}
