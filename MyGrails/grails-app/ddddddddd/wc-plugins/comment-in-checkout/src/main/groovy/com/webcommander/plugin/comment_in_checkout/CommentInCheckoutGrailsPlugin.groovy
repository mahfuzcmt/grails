package com.webcommander.plugin.comment_in_checkout

import com.webcommander.plugin.WebCommanderPluginBase
import com.webcommander.plugin.PluginMeta
import com.webcommander.constants.NamedConstants


class CommentInCheckoutGrailsPlugin extends WebCommanderPluginBase {

    def title = "Comment In Checkout"
    def author = "Sadikullah Zobair"
    def authorEmail = "zobair@bitmascot.com"
    def description = '''Displays Comment on top of Confirm Order Section in Checkout Page'''
    def profiles = ['web']
    def documentation = "https://www.webcommander.com/plugin/comment-in-checkout";
    {
        _plugin = new PluginMeta(identifier: "comment-in-checkout", name: title, pluginType: NamedConstants.WC_BEHAVIOUR_TYPE.E_COMMERCE)
        hooks = [
                confirmOrderStart   : [taglib: "commentApp", callable: "addComment"],
                checkoutPageConfirmSectionSettings: [taglib: "commentAdmin", callable: "addCommentField"]
        ]
    }

}
