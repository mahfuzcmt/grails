package com.webcommander.plugin.comment_in_checkout.app

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class CommentAppTagLib {
    static namespace = "commentApp"

    def addComment = { attrs, body ->
        out << body()
        String comment = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, "comment_in_checkout")?: ""
        out << "<div class='checkout-comment'>${comment}</div>"
    }
}
