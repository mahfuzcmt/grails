package com.webcommander.plugin.facebook.controllers.admin.design

import grails.web.Action

/**
 * Created by sanjoy on 6/18/2014.
 */
class WidgetController {
    @Action
    def facebookShortConfig() {
        render(view:  "/plugins/facebook/admin/editFacebookWidgetShort", model: [noAdvance: true])
    }
}