package com.webcommander.plugin.comment_in_checkout.admin

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class CommentAdminTagLib {
    static namespace = "commentAdmin"

    def addCommentField = { attrs, body ->
        out << body()
        String comment = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.CHECKOUT_PAGE, "comment_in_checkout")?: ""
        out << "<div class='form-row'>" +
                "            <label>${g.message([code: "comment.in.checkout"])}:<span class='suggestion'>e.g. provide a checkout comment</label>" +
                "            <textarea name='checkout_page.comment_in_checkout' class='large' validation='maxlength[5000]' maxlength='5000'>${comment}</textarea>" +
                "        </div>"
    }
}
